package ru.v0rt3x.perimeter.server.judas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.server.judas.dao.JudasTarget;
import ru.v0rt3x.perimeter.server.judas.dao.JudasTargetRepository;
import ru.v0rt3x.perimeter.server.service.ServiceManager;
import ru.v0rt3x.perimeter.server.service.dao.Service;
import ru.v0rt3x.perimeter.server.team.TeamManager;

import java.util.List;
import java.util.Objects;

@Component
public class JudasManager {

    @Autowired
    private ServiceManager serviceManager;

    @Autowired
    private TeamManager teamManager;

    @Autowired
    private JudasTargetRepository targetRepository;

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
}
