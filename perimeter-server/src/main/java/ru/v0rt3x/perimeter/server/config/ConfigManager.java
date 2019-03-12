package ru.v0rt3x.perimeter.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.server.files.FileRouter;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ConfigManager {

    @Autowired
    private FileRouter fileRouter;

    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private final Map<String, ConfigProcessor> configProcessors = new HashMap<>();

    @PostConstruct
    public void setUpConfigManager() {
        fileRouter.addRoute("config", Pattern.compile("^config/(?<name>.*)\\.yml$"), this::applyConfig);
    }

    private void applyConfig(String user, Matcher pathMatcher, byte[] data) {
        logger.info("User '{}' uploaded new '{}' config. Processing...", user, pathMatcher.group("name"));
        if (configProcessors.containsKey(pathMatcher.group("name"))) {
            configProcessors.get(pathMatcher.group("name")).process(data);
        } else {
            logger.warn("Unsupported config file: {}", pathMatcher.group("name"));
        }
    }

    public void registerConfigProcessor(String name, ConfigProcessor configProcessor) {
        configProcessors.put(name, configProcessor);
    }

}
