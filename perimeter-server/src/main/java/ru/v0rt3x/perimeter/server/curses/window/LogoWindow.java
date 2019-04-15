package ru.v0rt3x.perimeter.server.curses.window;

import ru.v0rt3x.perimeter.server.curses.CursesConsoleUtils;
import ru.v0rt3x.perimeter.server.curses.utils.KeyCode;
import ru.v0rt3x.perimeter.server.curses.utils.MouseKey;
import ru.v0rt3x.perimeter.server.curses.utils.Window;
import ru.v0rt3x.perimeter.server.shell.console.ConsoleColor;

import java.io.IOException;

public class LogoWindow extends Window {

    public LogoWindow(CursesConsoleUtils curses, String title, int x, int y, int height, int width, ConsoleColor borderColor, ConsoleColor borderTextColor, ConsoleColor bgColor) {
        super(curses, title, x, y, height, width, borderColor, borderTextColor, bgColor);
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {

    }

    @Override
    public void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
