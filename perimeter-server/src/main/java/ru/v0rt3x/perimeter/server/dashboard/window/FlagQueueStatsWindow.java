package ru.v0rt3x.perimeter.server.dashboard.window;

import ru.v0rt3x.perimeter.server.dashboard.window.modal.FlagQueueContextMenuWindow;
import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.window.Window;
import ru.v0rt3x.shell.curses.window.WindowManager;
import ru.v0rt3x.perimeter.server.flag.dao.FlagPriority;
import ru.v0rt3x.perimeter.server.flag.dao.FlagRepository;

import java.io.IOException;

import static ru.v0rt3x.perimeter.server.flag.dao.FlagStatus.*;
import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BRIGHT_WHITE;
import static ru.v0rt3x.shell.console.ansi.ConsoleColor.MAGENTA;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.BOLD;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.NORMAL;

public class FlagQueueStatsWindow extends Window {

    private final FlagRepository flagRepository;

    public FlagQueueStatsWindow(WindowManager windowManager) {
        super(windowManager,"Flag Queue", 9, 49, 12, 23, MAGENTA, BRIGHT_WHITE, null, 1);

        this.flagRepository = context.getBean(FlagRepository.class);
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {
        if (key.equals(MouseKey.RIGHT)) {
            FlagQueueContextMenuWindow contextMenu = windowManager.createWindow(FlagQueueContextMenuWindow.class, "queue_menu");

            contextMenu.draw(true);
        }
    }

    @Override
    protected void onDraw() throws IOException {
        write(2, 2, BRIGHT_WHITE, BOLD, "Queued");
        write(4, 2, BRIGHT_WHITE, BOLD, "\u2523 Low");
        write(5, 2, BRIGHT_WHITE, BOLD, "\u2523 Normal");
        write(6, 2, BRIGHT_WHITE, BOLD, "\u2517 High");

        write(8, 2, BRIGHT_WHITE, BOLD, "Accepted");
        write(9, 2, BRIGHT_WHITE, BOLD, "Rejected");

        int offset = window.getWidth() - 11;

        write(4, offset, BRIGHT_WHITE, NORMAL, " %08d", flagRepository.countAllByStatusAndPriority(QUEUED, FlagPriority.LOW));
        write(5, offset, BRIGHT_WHITE, NORMAL, " %08d", flagRepository.countAllByStatusAndPriority(QUEUED, FlagPriority.NORMAL));
        write(6, offset, BRIGHT_WHITE, NORMAL, " %08d", flagRepository.countAllByStatusAndPriority(QUEUED, FlagPriority.HIGH));

        write(8, offset, BRIGHT_WHITE, NORMAL, " %08d", flagRepository.countAllByStatus(ACCEPTED));
        write(9, offset, BRIGHT_WHITE, NORMAL, " %08d", flagRepository.countAllByStatus(REJECTED));
    }

    @Override
    public void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
