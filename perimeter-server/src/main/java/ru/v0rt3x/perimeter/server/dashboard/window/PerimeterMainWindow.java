package ru.v0rt3x.perimeter.server.dashboard.window;

import ru.v0rt3x.shell.console.ansi.ConsoleColor;
import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.window.Window;
import ru.v0rt3x.shell.curses.window.WindowManager;

import java.io.IOException;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BRIGHT_WHITE;
import static ru.v0rt3x.shell.console.ansi.ConsoleColor.CYAN;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.BOLD;

public class PerimeterMainWindow extends Window {

    public PerimeterMainWindow(WindowManager windowManager) {
        super(windowManager, "Perimeter Shell", 0, 0, windowManager.getCurses().getScreenHeight(), windowManager.getCurses().getScreenWidth(), ConsoleColor.CYAN, ConsoleColor.BRIGHT_WHITE, null, 0);
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        write(curses.getScreenHeight() - 1, curses.getScreenWidth() - 8, CYAN, BRIGHT_WHITE, BOLD, "%3dx%-3d", curses.getScreenHeight(), curses.getScreenWidth());
    }

    @Override
    public void onKeyPress(KeyCode keyCode) throws IOException {
        if (keyCode.equals(KeyCode.of('f'))) {
            write(0, curses.getScreenWidth() - 7, CYAN, BRIGHT_WHITE, BOLD, "%6s", isFreeze() ? "FREEZE": "");
        }
    }
}
