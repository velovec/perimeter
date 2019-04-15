package ru.v0rt3x.perimeter.server.curses.window;

import org.springframework.data.domain.PageRequest;
import ru.v0rt3x.perimeter.server.curses.CursesConsoleUtils;
import ru.v0rt3x.perimeter.server.curses.utils.KeyCode;
import ru.v0rt3x.perimeter.server.curses.utils.MouseKey;
import ru.v0rt3x.perimeter.server.curses.utils.Window;
import ru.v0rt3x.perimeter.server.event.dao.Event;
import ru.v0rt3x.perimeter.server.event.dao.EventRepository;
import ru.v0rt3x.perimeter.server.shell.console.ConsoleColor;

import java.io.IOException;

import static ru.v0rt3x.perimeter.server.shell.console.ConsoleColor.*;
import static ru.v0rt3x.perimeter.server.shell.console.ConsoleColor.WHITE;
import static ru.v0rt3x.perimeter.server.shell.console.ConsoleTextStyle.NORMAL;

public class EventLogWindow extends Window {

    private EventRepository eventRepository;

    public EventLogWindow(CursesConsoleUtils curses, EventRepository eventRepository) {
        super(curses, "Event Log", 2, 140, Math.max(7, curses.getScreenHeight() - 4), Math.max(50, curses.getScreenWidth() - 142), MAGENTA, BRIGHT_WHITE, null);

        this.eventRepository = eventRepository;
    }


    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        int line = 1;
        for (Event event: eventRepository.findAllByCreatedGreaterThanOrderByCreatedDesc(System.currentTimeMillis() - 600000L, PageRequest.of(0, window.getHeight() - 2))) {
            long ago = (System.currentTimeMillis() - event.getCreated()) / 1000;

            ConsoleColor eventColor;
            switch (event.getType()) {
                case INFO:
                    eventColor = BRIGHT_WHITE;
                    break;
                case WARNING:
                    eventColor = BRIGHT_YELLOW;
                    break;
                case URGENT:
                    eventColor = BRIGHT_RED;
                    break;
                default:
                    eventColor = WHITE;
                    break;
            }

            write(line, 2, eventColor, NORMAL, curses.wrapLine(event.getMessage(), window.getWidth() - 13));
            write(line, window.getWidth() - 10, WHITE, NORMAL, String.format("%03ds ago", ago));
            line++;
        }
    }

    @Override
    public void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
