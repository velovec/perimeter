package ru.v0rt3x.perimeter.server.curses.command;

import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.console.Table;

import java.io.IOException;

@ShellCommand(command = "pty-modes", description = "(DEBUG) List PTY modes")
public class PTYModesCommand extends PerimeterShellCommand {

    @Override
    protected void init() throws IOException {

    }

    @Override
    protected void execute() throws IOException {
        Table ptyModes = new Table("Mode", "Value");

        getEnvironment().getPtyModes().forEach(
            (ptyMode, value) -> ptyModes.addRow(ptyMode, value)
        );

        console.write(ptyModes);
    }

    @Override
    protected void onInterrupt() {

    }
}
