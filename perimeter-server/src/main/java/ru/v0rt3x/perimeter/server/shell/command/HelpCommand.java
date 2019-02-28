package ru.v0rt3x.perimeter.server.shell.command;

import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.console.Table;

import java.io.IOException;

@ShellCommand(command = "help", description = "Print available commands with description")
public class HelpCommand extends PerimeterShellCommand {

    @Override
    protected void init() throws IOException {}

    public void execute() throws IOException {
        Table commandHelp = new Table("Command", "Description");

        commandManager.listCommands().stream()
            .map(command -> commandManager.getCommandInfo(command))
            .forEach(commandInfo ->
                commandHelp.addRow(commandInfo.command(), commandInfo.description())
            );

        console.write(commandHelp);
    }

    @Override
    protected void onInterrupt() {}
}