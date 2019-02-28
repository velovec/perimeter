package ru.v0rt3x.perimeter.server.shell.command;

import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;

import java.io.IOException;

public class UnknownCommand extends PerimeterShellCommand {

    @Override
    protected void init() throws IOException {

    }

    @Override
    protected void execute() throws IOException {
        console.writeLine("%s: command not found", command);

        exit(1);
    }

    @Override
    protected void onInterrupt() {

    }
}
