package ru.v0rt3x.perimeter.server.shell.command;

import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.shell.console.Table;

import java.io.IOException;

@ShellCommand(command = "getenv", description = "List environment variables")
public class GetEnvCommand extends PerimeterShellCommand {

    @Override
    protected void init() throws IOException {}

    @Override
    protected void execute() throws IOException {
        console.write(new Table(getEnv(), "variable", "value"));
    }

    @Override
    protected void onInterrupt() {}
}
