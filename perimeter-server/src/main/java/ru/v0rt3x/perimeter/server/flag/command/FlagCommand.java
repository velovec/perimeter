package ru.v0rt3x.perimeter.server.flag.command;

import ru.v0rt3x.perimeter.server.flag.FlagStats;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.CommandAction;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.command.exception.NotImplementedException;

import java.io.IOException;

@ShellCommand(command = "flag", description = "Flag processor (submit flag, view queue stats)")
public class FlagCommand extends PerimeterShellCommand { // TODO: Add command action for manual flag submission

    private FlagStats flagStats;

    @Override
    protected void init() throws IOException {
        flagStats = context.getBean(FlagStats.class);
    }

    @Override
    protected void execute() throws IOException {
        throw new NotImplementedException();
    }

    @CommandAction("Show queue stats")
    public void stats() throws IOException {
        if (kwargs.containsKey("watch") && Boolean.parseBoolean(kwargs.get("watch"))) {
            while (isRunning()) {
                console.write(flagStats.toTable());
                sleep((kwargs.containsKey("delay")) ? Long.parseLong(kwargs.get("delay")) : 1000L);
            }
        } else {
            console.write(flagStats.toTable());
        }
    }

    @Override
    protected void onInterrupt() {

    }
}
