package ru.v0rt3x.perimeter.server.judas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.server.event.EventManager;
import ru.v0rt3x.perimeter.server.event.dao.EventType;
import ru.v0rt3x.perimeter.server.haproxy.HAProxyManager;
import ru.v0rt3x.perimeter.server.haproxy.dao.HAProxyBackend;
import ru.v0rt3x.perimeter.server.judas.dao.JudasTarget;
import ru.v0rt3x.perimeter.server.judas.dao.JudasTargetRepository;
import ru.v0rt3x.perimeter.server.service.ServiceManager;
import ru.v0rt3x.perimeter.server.service.dao.Service;
import ru.v0rt3x.perimeter.server.team.TeamManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class JudasManager {

    @Autowired
    private ServiceManager serviceManager;

    @Autowired
    private TeamManager teamManager;

    @Autowired
    private JudasTargetRepository targetRepository;

    @Autowired
    private HAProxyManager haProxyManager;

    @Autowired
    private EventManager eventManager;

    private static final Logger logger = LoggerFactory.getLogger(JudasManager.class);

    public JudasTarget getTarget(int port) {
        JudasTarget target = targetRepository.findByPort(port);

        if (Objects.isNull(target)) {
            target = setTarget(port, null);
        }

        return target;
    }

    public JudasTarget setTarget(int port, String host) {
        Service service = serviceManager.getService(port);

        JudasTarget target = targetRepository.findByPort(port);

        if (Objects.isNull(target)) {
            target = new JudasTarget();

            target.setPort(port);
            target.setProtocol("http");
        }

        if (Objects.nonNull(service)) {
            if (Objects.isNull(host))
                host = teamManager.getRandomTeam().getIp();

            if (!"http".equals(service.getMode()))
                throw new IllegalArgumentException("Judas supports only HTTP services");

            target.setDescription(service.getName());
        } else {
            if (Objects.isNull(host))
                throw new IllegalArgumentException("Host cannot be null for custom targets");

            target.setDescription("custom-target");
        }

        target.setHost(host);

        return targetRepository.save(target);
    }

    public List<JudasTarget> listTargets() {
        return targetRepository.findAll();
    }

    @Scheduled(fixedRate = 5000L)
    public void checkServices() {
        try {
            Map<String, Pair<Service, HAProxyBackend>> serviceBackendMap = haProxyManager.listMappings().stream()
                .filter(haProxyMapping -> haProxyMapping.getBackend().getName().equals("judas"))
                .collect(Collectors.toMap(
                    mapping -> String.format("%s-%s", mapping.getService().getName(), mapping.getBackend().getName()),
                    mapping -> Pair.of(mapping.getService(), mapping.getBackend())
                ));

            haProxyManager.getHAProxyStats().stream()
                .filter(record -> "BACKEND".equals(record.get(1)))
                .filter(record -> serviceBackendMap.containsKey(record.get(0)))
                .forEach(record -> setTargetStatus(
                    serviceBackendMap.get(record.get(0)).getFirst().getName(), record.get(17),
                    serviceBackendMap.get(record.get(0)).getSecond().getServers().size(),
                    Integer.parseInt(record.get(19))
                ));
        } catch (IOException e) {
            logger.error("Unable to check Judas status: {}", e.getMessage());
        }
    }

    private void setTargetStatus(String serviceName, String status, int serversMax, int serversActive) {
        JudasTarget target = getTarget(serviceManager.getService(serviceName).getPort());

        if (Objects.isNull(target))
            throw new IllegalArgumentException(String.format("Target for service '%s' not found", serviceName));

        if (!status.equals(target.getStatus())) {
            switch (status) {
                case "DOWN":
                    eventManager.createEvent(EventType.URGENT, "Judas target '%s' is DOWN", serviceName);
                    break;
                case "UP":
                    eventManager.createEvent(EventType.INFO, "Judas target '%s' is back to normal", serviceName);
                    break;
                default:
                    eventManager.createEvent(EventType.WARNING,"Judas target '%s' in unexpected state: %s", serviceName, status);
                    break;
            }
        }

        target.setStatus(status);

        target.setServersMax(serversMax);
        target.setServersActive(serversActive);

        targetRepository.save(target);
    }
}
