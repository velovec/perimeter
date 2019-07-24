package ru.v0rt3x.perimeter.configurator;

import com.hubspot.jinjava.Jinjava;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.agent.PerimeterAgentTaskHandler;
import ru.v0rt3x.perimeter.agent.annotation.AgentTaskHandler;
import ru.v0rt3x.perimeter.agent.types.AgentTask;
import ru.v0rt3x.perimeter.configurator.properties.Configurator;
import ru.v0rt3x.perimeter.configurator.properties.PerimeterConfiguratorProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

@Component
public class ConfiguratorAgent extends PerimeterAgentTaskHandler {

    @Autowired
    private PerimeterConfiguratorProperties configuratorProperties;

    private static final Logger logger = LoggerFactory.getLogger(ConfiguratorAgent.class);

    public ConfiguratorAgent() {
        super("configurator");
    }

    @SuppressWarnings("unchecked")
    @AgentTaskHandler(taskType = "apply")
    private void applyConfiguration(AgentTask task) {
        Jinjava jinjava = new Jinjava();

        Configurator configurator = configuratorProperties.getConfigurator((String) task.getParameters().get("type"));
        if (Objects.nonNull(configurator)) {
            Map<String, Object> context = (Map<String, Object>) task.getParameters().get("configuration");

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
                logger.error("Unable to apply changes for '{}': ({}) {}", task.getParameters().get("type"), e.getClass().getSimpleName(), e.getMessage());
            }
        } else {
            logger.warn("No configurator found for '{}'", task.getParameters().get("type"));
        }
    }
}
