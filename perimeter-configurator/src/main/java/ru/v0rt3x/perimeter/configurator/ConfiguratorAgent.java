package ru.v0rt3x.perimeter.configurator;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.agent.PerimeterAgent;
import ru.v0rt3x.perimeter.agent.types.AgentTask;
import ru.v0rt3x.perimeter.configurator.properties.PerimeterConfiguratorProperties;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Component
public class ConfiguratorAgent {

    @Autowired
    private PerimeterConfiguratorProperties configuratorProperties;

    @Autowired
    private PerimeterAgent perimeterAgent;

    private static final Logger logger = LoggerFactory.getLogger(ConfiguratorAgent.class);

    @PostConstruct
    private void registerAgent() {
        perimeterAgent.registerAgent("configure");
    }

    @Scheduled(fixedRate = 5000L)
    private void getTask() {
        AgentTask agentTask = perimeterAgent.getTask();

        switch (agentTask.getType()) {
            case "configure":
                applyConfiguration(agentTask.getParameters());
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private void applyConfiguration(Map<String, Object> parameters) {
        Jinjava jinjava = new Jinjava();
        Map<String, Object> context = Maps.newHashMap();

        context.put("configuration", parameters.get("configuration"));
        context.put("global", configuratorProperties);

        String templateName, configPath, applyCommand;
        switch ((String) parameters.get("type")) {
            case "haproxy":
                templateName = "haproxy.cfg.j2";
                configPath = configuratorProperties.getHaproxy().getConfigPath();
                applyCommand = configuratorProperties.getHaproxy().getApplyCommand();
                break;
            default:
                templateName = "default.j2";
                configPath = "/tmp/default.txt";
                applyCommand = "date";
                break;
        }

        try {
            String template = Resources.toString(Resources.getResource(templateName), Charsets.UTF_8);
            String renderedConfig = jinjava.render(template, context);
            Files.write(Paths.get(configPath), renderedConfig.getBytes());
        } catch (IOException e) {
            logger.error("Unable to write config '{}': ({}) {}", configPath, e.getClass().getSimpleName(), e.getMessage());
            return;
        }

        ProcessBuilder applyCommandBuilder = new ProcessBuilder(applyCommand.split("\\s"));

        try {
            Process applyCommandProcess = applyCommandBuilder.start();

            applyCommandProcess.waitFor();
        } catch (InterruptedException | IOException e) {
            logger.error("Unable to apply changes for '{}': ({}) {}", parameters.get("type"), e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
