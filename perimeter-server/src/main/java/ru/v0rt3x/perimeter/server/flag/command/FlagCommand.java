package ru.v0rt3x.perimeter.server.flag.command;

import ru.v0rt3x.perimeter.server.flag.FlagProcessor;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.CommandAction;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.command.exception.NotImplementedException;

import java.io.IOException;

@ShellCommand(command = "flag", description = "Flag processor (clear queue, view stats)")
public class FlagCommand extends PerimeterShellCommand {

    private FlagProcessor flagProcessor;

    @Override
    protected void init() throws IOException {
        flagProcessor = context.getBean(FlagProcessor.class);
    }

    @Override
    protected void execute() throws IOException {
        throw new NotImplementedException();
    }

    @CommandAction("Show queue stats")
    public void stats() throws IOException {
        if (kwargs.containsKey("watch") && Boolean.parseBoolean(kwargs.get("watch"))) {
            while (isRunning()) {
                console.write(flagProcessor.getStatsAsTable());
                sleep((kwargs.containsKey("delay")) ? Long.parseLong(kwargs.get("delay")) : 1000L);
            }
        } else {
            console.write(flagProcessor.getStatsAsTable());
        }
    }

    @CommandAction("Clear all flags")
    public void clear_all() throws IOException {
        if (console.readYesNo("Are you sure?")) {
            flagProcessor.clearQueue();
        }
    }

    @Override
    protected void onInterrupt() {

    }
}
