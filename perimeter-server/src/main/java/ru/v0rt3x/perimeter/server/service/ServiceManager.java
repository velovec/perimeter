package ru.v0rt3x.perimeter.server.service;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.server.haproxy.HAProxyManager;
import ru.v0rt3x.perimeter.server.haproxy.dao.HAProxyBackend;
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
    }

    private void setServiceStatus(String serviceName, String status, int serversMax, int serversActive) {
        Service service = getService(serviceName);

        if (Objects.isNull(service))
            throw new IllegalArgumentException(String.format("Service '%s' not found", serviceName));

        service.setStatus(status);

        service.setServersMax(serversMax);
        service.setServersActive(serversActive);

        serviceRepository.save(service);
    }

    @Scheduled(fixedRate = 5000L)
    public void checkServices() {
        try {
            Map<String, Pair<Service, HAProxyBackend>> serviceBackendMap = haProxyManager.listMappings().stream()
                .filter(haProxyMapping -> Objects.isNull(haProxyMapping.getAcl()))
                .collect(Collectors.toMap(
                    mapping -> String.format("%s-%s", mapping.getService().getName(), mapping.getBackend().getName()),
                    mapping -> new ImmutablePair<>(mapping.getService(), mapping.getBackend())
                ));

            haProxyManager.getHAProxyStats().stream()
                .filter(record -> "BACKEND".equals(record.get(1)))
                .filter(record -> serviceBackendMap.containsKey(record.get(0)))
                .forEach(record -> setServiceStatus(
                    serviceBackendMap.get(record.get(0)).getLeft().getName(), record.get(17),
                    serviceBackendMap.get(record.get(0)).getRight().getServers().size(),
                    Integer.parseInt(record.get(19))
                ));
        } catch (IOException e) {
            logger.error("Unable to check service status: {}", e.getMessage());
        }
    }
}
