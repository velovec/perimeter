package ru.v0rt3x.perimeter.server.shell.command;

import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;

import java.io.IOException;

@ShellCommand(command = "setenv", description = "Set environment variables")
public class SetEnvCommand extends PerimeterShellCommand {

    @Override
    protected void init() throws IOException {}

    public void execute() throws IOException {
        if (kwargs.containsKey("USER")) {
            kwargs.remove("USER");
            console.writeLine("USER variable cannot be changed");
        }

        getEnvironment().getEnv().putAll(kwargs);
    }

    @Override
    protected void onInterrupt() {}
}