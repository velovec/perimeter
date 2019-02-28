package ru.v0rt3x.perimeter.server.shell;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.server.Application;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;

import java.util.*;

@Component
public class PerimeterShellCommandManager {

    private Map<String, Class<? extends PerimeterShellCommand>> shellCommands = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(PerimeterShellCommandManager.class);

    @Autowired
    private ConfigurableApplicationContext context;

    public PerimeterShellCommandManager() {
        Reflections discoveryHelper = new Reflections(
            ClasspathHelper.forPackage(Application.class.getPackage().getName()),
            new SubTypesScanner()
        );

        discoveryHelper.getSubTypesOf(PerimeterShellCommand.class).stream()
            .filter(command -> command.isAnnotationPresent(ShellCommand.class))
            .forEach(command -> {
                ShellCommand shellCommand = command.getAnnotation(ShellCommand.class);

                shellCommands.put(shellCommand.command(), command);
            });
    }

    public PerimeterShellCommand getCommand(String command) {
        if (shellCommands.containsKey(command)) {
            try {
                return shellCommands.get(command).newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("Unable to instantiate command", e);
            }
        }

        return null;
    }

    public ShellCommand getCommandInfo(String command) {
        if (shellCommands.containsKey(command)) {
            return shellCommands.get(command).getAnnotation(ShellCommand.class);
        }

        return null;
    }

    public List<String> listCommands() {
        return new ArrayList<>(shellCommands.keySet());
    }

    public ConfigurableApplicationContext getContext() {
        return context;
    }
}
