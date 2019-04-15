package ru.v0rt3x.perimeter.server.curses.window;

import ru.v0rt3x.perimeter.server.curses.CursesConsoleUtils;
import ru.v0rt3x.perimeter.server.curses.utils.KeyCode;
import ru.v0rt3x.perimeter.server.curses.utils.MouseKey;
import ru.v0rt3x.perimeter.server.curses.utils.Rectangle;
import ru.v0rt3x.perimeter.server.curses.utils.Window;
import ru.v0rt3x.perimeter.server.shell.console.ConsoleColor;

import java.io.IOException;

import static ru.v0rt3x.perimeter.server.shell.console.ConsoleColor.BRIGHT_WHITE;
import static ru.v0rt3x.perimeter.server.shell.console.ConsoleColor.CYAN;
import static ru.v0rt3x.perimeter.server.shell.console.ConsoleTextStyle.BOLD;

public class PerimeterMainWindow extends Window {

    public PerimeterMainWindow(CursesConsoleUtils curses) {
        super(curses, "Perimeter Shell", 0, 0, curses.getScreenHeight(), curses.getScreenWidth(), ConsoleColor.CYAN, ConsoleColor.BRIGHT_WHITE, null);
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
