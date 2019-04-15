package ru.v0rt3x.perimeter.server.curses.utils;

import ru.v0rt3x.perimeter.server.curses.CursesConsoleUtils;
import ru.v0rt3x.perimeter.server.shell.console.ConsoleColor;
import ru.v0rt3x.perimeter.server.shell.console.ConsoleTextStyle;

import java.io.IOException;

public abstract class Window {

    protected final CursesConsoleUtils curses;

    protected final Rectangle window;
    private final String title;

    private final ConsoleColor borderColor;
    private final ConsoleColor borderTextColor;
    private final ConsoleColor bgColor;

    private boolean freeze = false;

    public Window(CursesConsoleUtils curses, String title, int x, int y, int height, int width, ConsoleColor borderColor, ConsoleColor borderTextColor, ConsoleColor bgColor) {
        this.curses = curses;

        this.window = Rectangle.newRect(x, y, height, width);
        this.title = title;

        this.borderColor = borderColor;
        this.borderTextColor = borderTextColor;
        this.bgColor = bgColor;
    }

    public void onMouseClickEvent(MouseKeyCode keyCode) throws IOException {
        if (window.isInside(keyCode.getX(), keyCode.getY())) {
            onMouseClick(keyCode.getKey(), keyCode.getX() - window.getX(), keyCode.getY() - window.getY());
        }
    }

    public void onKeyPressEvent(KeyCode keyCode) throws IOException {
        if (keyCode.equals(KeyCode.of('f'))) {
            freeze = !freeze;
        }

        onKeyPress(keyCode);
    }

    public void draw() throws IOException {
        if (curses.isPossibleToRender(window) && !freeze) {
            curses.draw(window, borderColor, borderTextColor, title);

            erase();
            onDraw();
        }
    }

    protected void erase() throws IOException {
        curses.erase(window, 1, window.getHeight() - 2, bgColor);
    }

    protected void erase(int startLine, int lines) throws IOException {
        curses.erase(window, startLine, lines, bgColor);
    }

    protected void write(int x, int y, ConsoleColor textColor, ConsoleTextStyle textStyle, String format, Object... args) throws IOException {
        write(x, y, bgColor, textColor, textStyle, format, args);
    }

    protected void write(int x, int y, ConsoleColor bgColor, ConsoleColor textColor, ConsoleTextStyle textStyle, String format, Object... args) throws IOException {
        curses.write(window, x, y, bgColor, textColor, textStyle, String.format(format, args));
    }

    protected boolean isFreeze() {
        return freeze;
    }

    protected abstract void onMouseClick(MouseKey key, int x, int y) throws IOException;

    protected abstract void onDraw() throws IOException;

    protected abstract void onKeyPress(KeyCode keyCode) throws IOException;
}
