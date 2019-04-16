package ru.v0rt3x.perimeter.server.dashboard.window;

import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.window.Window;
import ru.v0rt3x.shell.curses.window.WindowManager;

import java.io.IOException;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BLACK;
import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BRIGHT_WHITE;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.NORMAL;
import static ru.v0rt3x.Logo.LOGO;

public class SplashWindow extends Window {

    public SplashWindow(WindowManager windowManager) {
        super(windowManager,"Powered By", 0, 0, windowManager.getCurses().getScreenHeight(), windowManager.getCurses().getScreenWidth(), BLACK, BRIGHT_WHITE, null, 1);
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        int logoHeight = LOGO.length;
        int logoWidth = LOGO[0].length();

        int x = (window.getHeight() - logoHeight) / 2;
        int y = (window.getWidth() - logoWidth) / 2;

        for (int i = 0; i < LOGO.length; i++) {
            write(x + i, y, BRIGHT_WHITE, NORMAL, LOGO[i]);
        }
    }

    @Override
    public void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
