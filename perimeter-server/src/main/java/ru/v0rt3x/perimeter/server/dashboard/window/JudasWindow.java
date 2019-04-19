package ru.v0rt3x.perimeter.server.dashboard.window;

import ru.v0rt3x.perimeter.server.judas.dao.JudasTarget;
import ru.v0rt3x.perimeter.server.judas.dao.JudasTargetRepository;
import ru.v0rt3x.shell.console.ansi.ConsoleColor;
import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.window.Rectangle;
import ru.v0rt3x.shell.curses.window.Window;
import ru.v0rt3x.shell.curses.window.WindowManager;

import java.io.IOException;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.*;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.BOLD;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.NORMAL;

public class JudasWindow extends Window {

    private final JudasTargetRepository targetRepository;

    public JudasWindow(WindowManager windowManager) {
        super(windowManager,"Judas", 17, 108, 13, 34, BLUE, BRIGHT_WHITE, null, 1);

        this.targetRepository = context.getBean(JudasTargetRepository.class);
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        write(2, 2, BRIGHT_WHITE, BOLD, "Port");
        write(2, 8, BRIGHT_WHITE, BOLD, "Target");
        write(2, 24, BRIGHT_WHITE, BOLD, "Status");

        int line = 4;
        for (JudasTarget target: targetRepository.findAll()) {
            Rectangle rect = contextMenu(line, 2, window.getWidth() - 4, () -> {

            });

            ConsoleColor statusColor;

            switch (target.getStatus()) {
                case "UP":
                    statusColor = BRIGHT_GREEN;
                    break;
                case "DOWN":
                    statusColor = BRIGHT_RED;
                    break;
                default:
                    statusColor = WHITE;
                    break;
            }

            write(rect.getX(), 2, BRIGHT_WHITE, NORMAL, "%-5d", target.getPort());
            write(rect.getX(), 8, BRIGHT_WHITE, NORMAL, curses.wrapLine(target.getHost(), 15));
            write(rect.getX(), 24, statusColor, NORMAL, curses.wrapLine(target.getStatusString(), 8));
        }
    }

    @Override
    public void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
