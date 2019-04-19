package ru.v0rt3x.perimeter.server.dashboard.window;

import org.springframework.data.domain.PageRequest;

import ru.v0rt3x.perimeter.server.dashboard.window.modal.EventContextMenuWindow;
import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.window.Rectangle;
import ru.v0rt3x.shell.curses.window.Window;
import ru.v0rt3x.shell.curses.window.WindowManager;
import ru.v0rt3x.perimeter.server.event.dao.Event;
import ru.v0rt3x.perimeter.server.event.dao.EventRepository;
import ru.v0rt3x.shell.console.ansi.ConsoleColor;

import java.io.IOException;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.*;
import static ru.v0rt3x.shell.console.ansi.ConsoleColor.WHITE;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.NORMAL;

public class EventLogWindow extends Window {

    private EventRepository eventRepository;

    public EventLogWindow(WindowManager windowManager) {
        super(windowManager, "Event Log", 2, 143, Math.max(7, windowManager.getCurses().getScreenHeight() - 4), Math.max(50, windowManager.getCurses().getScreenWidth() - 145), RED, BRIGHT_WHITE, null, 1);

        this.eventRepository = context.getBean(EventRepository.class);
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

            Rectangle rect = contextMenu(line, 2, window.getWidth() - 4, () -> {
                EventContextMenuWindow contextMenu = windowManager.createWindow(EventContextMenuWindow.class, "event_menu");
                contextMenu.setEvent(event);

                contextMenu.draw(true);
            });

            write(rect.getX(), 2, eventColor, NORMAL, curses.wrapLine(event.getMessage(), rect.getWidth() - 11));
            write(rect.getX(), rect.getWidth() - 8, WHITE, NORMAL, String.format("%03ds ago", ago));
            line++;
        }
    }

    @Override
    public void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
