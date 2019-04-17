package ru.v0rt3x.perimeter.server.dashboard.window.modal;

import ru.v0rt3x.perimeter.server.event.dao.Event;
import ru.v0rt3x.perimeter.server.event.dao.EventRepository;
import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.window.WindowManager;
import ru.v0rt3x.shell.curses.window.modal.ModalWindow;

import java.io.IOException;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BLACK;
import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BRIGHT_WHITE;

public class EventContextMenuWindow extends ModalWindow {

    private Event event;

    public EventContextMenuWindow(WindowManager windowManager) {
        super(windowManager, "Event Menu", (windowManager.getCurses().getScreenHeight() - 5) / 2, (windowManager.getCurses().getScreenWidth() - 28) / 2, 5, 28, BLACK, BRIGHT_WHITE, null, 2);
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        button(2, 2, "Dismiss", this::dismissEvent);
    }

    private void dismissEvent() throws IOException {
        context.getBean(EventRepository.class).delete(event);

        hide();
        windowManager.draw(true);
    }

    @Override
    protected void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
