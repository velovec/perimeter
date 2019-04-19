package ru.v0rt3x.perimeter.configurator;

import com.hubspot.jinjava.Jinjava;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.agent.PerimeterAgent;
import ru.v0rt3x.perimeter.agent.types.AgentTask;
import ru.v0rt3x.perimeter.configurator.properties.Configurator;
import ru.v0rt3x.perimeter.configurator.properties.PerimeterConfiguratorProperties;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

@Component
public class ConfiguratorAgent {

    @Autowired
    private PerimeterConfiguratorProperties configuratorProperties;

    @Autowired
    private PerimeterAgent perimeterAgent;

    private static final Logger logger = LoggerFactory.getLogger(ConfiguratorAgent.class);

    @PostConstruct
    private void registerAgent() {
        perimeterAgent.registerAgent("configurator");
    }

    @Scheduled(fixedRate = 5000L)
    private void getTask() {
        AgentTask agentTask = perimeterAgent.getTask();

        switch (agentTask.getType()) {
            case "apply":
                applyConfiguration(agentTask.getParameters());
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("unchecked")
    private void applyConfiguration(Map<String, Object> parameters) {
        Jinjava jinjava = new Jinjava();

        Configurator configurator = configuratorProperties.getConfigurator((String) parameters.get("type"));
        if (Objects.nonNull(configurator)) {
            Map<String, Object> context = (Map<String, Object>) parameters.get("configuration");

            context.putAll(configurator.getOverrides());

            Path templatePath = Paths.get(configuratorProperties.getTemplatesPath(), configurator.getTemplateFile());
            try {
                String template = new String(Files.readAllBytes(templatePath));

                String config = jinjava.render(template, context);

                Files.write(Paths.get(configurator.getConfigPath()), config.getBytes());
            } catch (IOException e) {
                logger.error("Unable to write config '{}': ({}) {}", configurator.getConfigPath(), e.getClass().getSimpleName(), e.getMessage());
                return;
            }

            ProcessBuilder applyCommandBuilder = new ProcessBuilder(configurator.getApplyCommand().split("\\s"));
            try {
                Process applyCommandProcess = applyCommandBuilder.start();

                applyCommandProcess.waitFor();
            } catch (InterruptedException | IOException e) {
                logger.error("Unable to apply changes for '{}': ({}) {}", parameters.get("type"), e.getClass().getSimpleName(), e.getMessage());
            }
        } else {
            logger.warn("No configurator found for '{}'", parameters.get("type"));
        }
    }
}
