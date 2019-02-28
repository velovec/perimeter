package ru.v0rt3x.perimeter.server.shell.command;

import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.console.ANSIUtils;

import java.io.IOException;

@ShellCommand(command = "clear", description = "Erases the screen with the background colour")
public class ClearCommand extends PerimeterShellCommand {

    @Override
    protected void init() throws IOException {}

    public void execute() throws IOException {
        console.write(ANSIUtils.CursorPosition(0, 0));
        console.write(ANSIUtils.EraseData(2));
    }

    @Override
    protected void onInterrupt() {}
}