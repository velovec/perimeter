package ru.v0rt3x.perimeter.server.shell.command;

import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;

import java.io.IOException;

public class InvalidCommand extends PerimeterShellCommand {

    @Override
    protected void init() throws IOException {

    }

    @Override
    protected void execute() throws IOException {
        console.writeLine("Invalid command");
        exit(1);
    }

    @Override
    protected void onInterrupt() {

    }

}
