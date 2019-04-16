package ru.v0rt3x.perimeter.server.dashboard.window;

import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.window.Window;
import ru.v0rt3x.shell.curses.window.WindowManager;
import ru.v0rt3x.perimeter.server.themis.ThemisClient;

import java.io.IOException;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BLUE;
import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BRIGHT_WHITE;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.BOLD;

public class ThemisInfoWindow extends Window {

    private ThemisClient themisClient;

    public ThemisInfoWindow(WindowManager windowManager) {
        super(windowManager, "Themis Info", 2, 49, 6, 23, BLUE, BRIGHT_WHITE, null, 1);

        this.themisClient = context.getBean(ThemisClient.class);
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {
        // do nothing
    }

    @Override
    protected void onDraw() throws IOException {
        write(2, 2, BRIGHT_WHITE, BOLD, "State: %s", themisClient.getContestState());
        write(3, 2, BRIGHT_WHITE, BOLD, "Round: %d", themisClient.getContestRound());
    }

    @Override
    public void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
