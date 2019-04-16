package ru.v0rt3x.perimeter.server.dashboard.window;

import org.springframework.data.domain.PageRequest;

import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.window.Window;
import ru.v0rt3x.shell.curses.window.WindowManager;
import ru.v0rt3x.perimeter.server.flag.dao.Flag;
import ru.v0rt3x.perimeter.server.flag.dao.FlagRepository;
import ru.v0rt3x.shell.console.ansi.ConsoleColor;

import java.io.IOException;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.*;
import static ru.v0rt3x.shell.console.ansi.ConsoleColor.WHITE;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.NORMAL;

public class FlagHistoryWindow extends Window {

    private final FlagRepository flagRepository;

    public FlagHistoryWindow(WindowManager windowManager) {
        super(windowManager,"Flag History", 2, 2, Math.max(3, windowManager.getCurses().getScreenHeight() - 4), 46, GREEN, BRIGHT_WHITE, null, 1);

        this.flagRepository = context.getBean(FlagRepository.class);
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        int line = 1;
        for (Flag flag: flagRepository.findAllByOrderByCreateTimeStampDesc(PageRequest.of(0, window.getHeight() - 2))) {
            ConsoleColor flagColor;

            switch (flag.getStatus()) {
                case QUEUED:
                    flagColor = BRIGHT_WHITE;
                    break;
                case ACCEPTED:
                    flagColor = BRIGHT_GREEN;
                    break;
                case REJECTED:
                    flagColor = BRIGHT_RED;
                    break;
                default:
                    flagColor = WHITE;
            }

            write(line, 2, WHITE, NORMAL, flag.getFlag());
            write(line, 36, flagColor, NORMAL, curses.wrapLine(flag.getStatus().name(), 9));
            line++;
        }
    }

    @Override
    public void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
