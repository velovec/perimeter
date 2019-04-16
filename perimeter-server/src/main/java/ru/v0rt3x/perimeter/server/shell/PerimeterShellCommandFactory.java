package ru.v0rt3x.perimeter.server.shell;

import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.command.CommandFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.v0rt3x.perimeter.server.shell.command.InvalidCommand;
import ru.v0rt3x.perimeter.server.shell.command.UnknownCommand;
import ru.v0rt3x.shell.console.CommandLineParser;

@Component
public class PerimeterShellCommandFactory implements CommandFactory {

    @Autowired
    private PerimeterShellCommandManager commandManager;

    @Override
    public Command createCommand(String commandLine) {
        PerimeterShellCommand targetCommand;

        CommandLineParser.CommandLine command = CommandLineParser.parse(commandLine);

        targetCommand = routeCommand(command.getCmd());
        targetCommand.setUpCommand(commandManager, command);

        return targetCommand;
    }

    private PerimeterShellCommand routeCommand(String command) {
        if (command == null) {
            return new InvalidCommand();
        }

        PerimeterShellCommand commandObject = commandManager.getCommand(command);
        if (commandObject != null) {
            return commandObject;
        }

        return new UnknownCommand();
    }
}
