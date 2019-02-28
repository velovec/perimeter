package ru.v0rt3x.perimeter.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import ru.v0rt3x.perimeter.server.files.FileRouter;
import ru.v0rt3x.perimeter.server.haproxy.HAProxyConfigWrapper;
import ru.v0rt3x.perimeter.server.haproxy.HAProxyManager;
import ru.v0rt3x.perimeter.server.service.ServiceManager;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ConfigManager {

    @Autowired
    private HAProxyManager haProxyManager;

    @Autowired
    private ServiceManager serviceManager;

    @Autowired
    private FileRouter fileRouter;

    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);

    @PostConstruct
    public void setUpConfigManager() {
        fileRouter.addRoute("config", Pattern.compile("^config/(?<name>.*)\\.yml$"), this::applyConfig);
    }

    private void applyConfig(String user, Matcher pathMatcher, byte[] data) {
        logger.info("User '{}' uploaded new '{}' config. Processing...", user, pathMatcher.group("name"));
        switch (pathMatcher.group("name")) {
            case "haproxy":
                processHAProxyConfig(data);
                break;
            default:
                logger.warn("Unsupported config file: {}", pathMatcher.group("name"));
                break;
        }
    }

    private void processHAProxyConfig(byte[] data) {
        HAProxyConfigWrapper configWrapper = new Yaml().loadAs(new ByteArrayInputStream(data), HAProxyConfigWrapper.class);

        haProxyManager.clearMappings();
        serviceManager.replaceServices(configWrapper.getServices());
        haProxyManager.replaceBackends(configWrapper.getBackends());
        haProxyManager.replaceACLs(configWrapper.getAcls());

        for (Map<String, String> mappingDefinition: configWrapper.getMappings()) {
            haProxyManager.setBackend(
                mappingDefinition.get("service"),
                mappingDefinition.get("backend"),
                mappingDefinition.get("acl")
            );
        }
    }
}
