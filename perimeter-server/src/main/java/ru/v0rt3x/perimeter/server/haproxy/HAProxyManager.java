package ru.v0rt3x.perimeter.server.haproxy;

import com.google.common.base.Charsets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import ru.v0rt3x.perimeter.server.config.ConfigManager;
import ru.v0rt3x.perimeter.server.haproxy.dao.*;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.service.ServiceManager;
import ru.v0rt3x.perimeter.server.service.dao.Service;
import ru.v0rt3x.perimeter.server.utils.YAMLUtils;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class HAProxyManager {

    @Autowired
    private ServiceManager serviceManager;

    @Autowired
    private HAProxyACLRepository aclRepository;

    @Autowired
    private HAProxyBackendRepository backendRepository;

    @Autowired
    private HAProxyMappingRepository mappingRepository;

    @Autowired
    private PerimeterProperties perimeterProperties;

    @Autowired
    private ConfigManager configManager;

    @PostConstruct
    public void setUpHAProxyManager() {
        configManager.registerConfigProcessor("haproxy", this::processHAProxyConfig);
    }

    private void processHAProxyConfig(byte[] data) {
        HAProxyConfigWrapper configWrapper = YAMLUtils.getParser(HAProxyConfigWrapper.class)
            .loadAs(new ByteArrayInputStream(data), HAProxyConfigWrapper.class);

        mappingRepository.deleteAll();
        backendRepository.deleteAll();
        aclRepository.deleteAll();

        backendRepository.saveAll(configWrapper.getBackends());
        aclRepository.saveAll(configWrapper.getAcls());

        serviceManager.replaceServices(configWrapper.getServices());

        for (Map<String, String> mappingDefinition: configWrapper.getMappings()) {
            setBackend(
                mappingDefinition.get("service"),
                mappingDefinition.get("backend"),
                mappingDefinition.get("acl")
            );
        }
    }

    public void setBackend(String serviceName, String backendName, String aclName) {
        Service service = serviceManager.getService(serviceName);
        if (Objects.isNull(service)) throw new IllegalArgumentException(String.format("Service '%s' not registered", serviceName));

        HAProxyBackend backend = backendRepository.findByName(backendName);
        if (Objects.isNull(backend)) throw new IllegalArgumentException(String.format("Backend '%s' not registered", backendName));

        HAProxyACL acl = null;
        if (Objects.nonNull(aclName)) {
            acl = aclRepository.findByName(aclName);
            if (Objects.isNull(acl)) throw new IllegalArgumentException(String.format("ACL '%s' not registered", aclName));
        }

        HAProxyMapping mapping = mappingRepository.findByServiceAndAcl(service, acl);

        if (Objects.isNull(mapping)) {
            mapping = new HAProxyMapping();

            mapping.setService(service);
            mapping.setAcl(acl);
        }

        mapping.setBackend(backend);

        mappingRepository.save(mapping);
    }

    public List<HAProxyMapping> listMappings() {
        return mappingRepository.findAllByOrderByAclDesc();
    }

    public List<HAProxyACL> listACLs() {
        return aclRepository.findAll();
    }

    public List<HAProxyBackend> listBackends() {
        return backendRepository.findAll();
    }

    public List<CSVRecord> getHAProxyStats() throws IOException {
        URL statsUrl = new URL(
            "http", perimeterProperties.getTeam().getInternalIp(),
            perimeterProperties.getTeam().getStatsPort(), "/stats;csv"
        );

        List<CSVRecord> statRecords = new ArrayList<>();
        for (CSVRecord statRecord: CSVParser.parse(statsUrl, Charsets.UTF_8, CSVFormat.DEFAULT)) {
            statRecords.add(statRecord);
        }

        return statRecords.subList(1, statRecords.size());
    }
}
