package ru.v0rt3x.perimeter.server.shell.command;

import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;

import java.io.IOException;

@ShellCommand(command = "exit", description = "Closes current session")
public class ExitCommand extends PerimeterShellCommand {

    @Override
    protected void init() throws IOException {}

    @Override
    protected void execute() {
        exit(0, "logout");
    }

    @Override
    protected void onInterrupt() {}
}