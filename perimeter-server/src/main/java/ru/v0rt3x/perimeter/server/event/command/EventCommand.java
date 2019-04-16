package ru.v0rt3x.perimeter.server.event.command;

import ru.v0rt3x.perimeter.server.event.EventManager;
import ru.v0rt3x.perimeter.server.event.dao.Event;
import ru.v0rt3x.perimeter.server.event.dao.EventType;
import ru.v0rt3x.perimeter.server.shell.PerimeterShellCommand;
import ru.v0rt3x.perimeter.server.shell.annotations.CommandAction;
import ru.v0rt3x.perimeter.server.shell.annotations.ShellCommand;
import ru.v0rt3x.perimeter.server.shell.command.exception.NotImplementedException;
import ru.v0rt3x.shell.console.Table;

import java.io.IOException;

@ShellCommand(command = "events", description = "List, create system events")
public class EventCommand extends PerimeterShellCommand {

    private EventManager eventManager;

    @Override
    protected void init() throws IOException {
        eventManager = context.getBean(EventManager.class);
    }

    @Override
    protected void execute() throws IOException {
        throw new NotImplementedException();
    }

    @CommandAction("List events")
    public void list() throws IOException {
        Table eventList = new Table("Message");

        for (Event event: eventManager.getLastEvents(20, System.currentTimeMillis() - 600000L)) {
            eventList.addRow(event.getMessage());
        }

        console.write(eventList);
    }

    @CommandAction("Create event")
    public void create() throws IOException {
        if (args.size() == 0) {
            console.error("events create [--level <LEVEL>] <message>");
            exit(1);
            return;
        }

        eventManager.createEvent(
            EventType.valueOf(kwargs.getOrDefault("level", "info").toUpperCase()),
            String.join(" ", args)
        );
    }

    @Override
    protected void onInterrupt() {

    }
}
