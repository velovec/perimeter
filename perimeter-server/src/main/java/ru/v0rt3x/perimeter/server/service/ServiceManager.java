package ru.v0rt3x.perimeter.server.service;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.server.event.EventManager;
import ru.v0rt3x.perimeter.server.event.dao.EventType;
import ru.v0rt3x.perimeter.server.git.GitRepositoryManager;
import ru.v0rt3x.perimeter.server.haproxy.HAProxyManager;
import ru.v0rt3x.perimeter.server.haproxy.dao.HAProxyBackend;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.service.dao.Service;
import ru.v0rt3x.perimeter.server.service.dao.ServiceRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ServiceManager {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private HAProxyManager haProxyManager;

    @Autowired
    private PerimeterProperties perimeterProperties;

    @Autowired
    private GitRepositoryManager repositoryManager;

    @Autowired
    private EventManager eventManager;

    public List<Service> listServices() {
        return serviceRepository.findAll();
    }

    private static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);

    public Service getService(String serviceName) {
        return serviceRepository.findByName(serviceName);
    }

    public void replaceServices(List<Service> services) {
        serviceRepository.deleteAll();
        serviceRepository.saveAll(services);
        createGitRepositories(services);
    }

    private void setServiceStatus(String serviceName, String status, int serversMax, int serversActive) {
        Service service = getService(serviceName);

        if (Objects.isNull(service))
            throw new IllegalArgumentException(String.format("Service '%s' not found", serviceName));

        if (!status.equals(service.getStatus())) {
            switch (status) {
                case "DOWN":
                    eventManager.createEvent(EventType.URGENT, "Service '%s' is DOWN", serviceName);
                    break;
                case "UP":
                    eventManager.createEvent(EventType.INFO, "Service '%s' is back to normal", serviceName);
                    break;
                default:
                    eventManager.createEvent(EventType.WARNING,"Service '%s' in unexpected state: %s", serviceName, status);
                    break;
            }
        }

        service.setStatus(status);

        service.setServersMax(serversMax);
        service.setServersActive(serversActive);

        serviceRepository.save(service);
    }

    private void createGitRepositories(List<Service> services) {
        for (Service service: services) {
            if (!repositoryManager.hasRepository(service.getName())) {
                try {
                    repositoryManager.createRepository(service.getName());
                } catch (IOException | GitAPIException e) {
                    logger.error("Unable to create Git repository for service '{}': {}", service.getName(), e.getMessage());
                }
            }
        }
    }

    @Scheduled(fixedRate = 5000L)
    public void checkServices() {
        try {
            Map<String, Pair<Service, HAProxyBackend>> serviceBackendMap = haProxyManager.listMappings().stream()
                .filter(haProxyMapping -> haProxyMapping.getBackend().getName().equals(perimeterProperties.getTeam().getProductionBackend()))
                .collect(Collectors.toMap(
                    mapping -> String.format("%s-%s", mapping.getService().getName(), mapping.getBackend().getName()),
                    mapping -> Pair.of(mapping.getService(), mapping.getBackend())
                ));

            haProxyManager.getHAProxyStats().stream()
                .filter(record -> "BACKEND".equals(record.get(1)))
                .filter(record -> serviceBackendMap.containsKey(record.get(0)))
                .forEach(record -> setServiceStatus(
                    serviceBackendMap.get(record.get(0)).getFirst().getName(), record.get(17),
                    serviceBackendMap.get(record.get(0)).getSecond().getServers().size(),
                    Integer.parseInt(record.get(19))
                ));
        } catch (IOException e) {
            logger.error("Unable to check service status: {}", e.getMessage());
        }
    }

    public Service getService(int port) {
        return serviceRepository.findByPort(port);
    }
}
