package ru.v0rt3x.shell.curses.window;

import org.springframework.context.ConfigurableApplicationContext;
import ru.v0rt3x.shell.curses.CursesEngine;
import ru.v0rt3x.shell.console.ansi.ConsoleColor;
import ru.v0rt3x.shell.console.ansi.ConsoleTextStyle;
import ru.v0rt3x.shell.curses.handlers.WindowOnClickHandler;
import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.input.MouseKeyCode;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BLACK;
import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BRIGHT_WHITE;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.BOLD;

public abstract class Window {

    protected final WindowManager windowManager;

    protected final CursesEngine curses;
    protected final ConfigurableApplicationContext context;

    protected final Rectangle window;
    private final String title;

    private final ConsoleColor borderColor;
    private final ConsoleColor borderTextColor;
    private final ConsoleColor bgColor;

    private final int zIndex;

    private boolean freeze = false;
    private boolean hidden = false;

    private final Map<Rectangle, WindowOnClickHandler> buttons = new ConcurrentHashMap<>();
    private final Map<Rectangle, WindowOnClickHandler> contextMenus = new ConcurrentHashMap<>();

    public Window(WindowManager windowManager, String title, int x, int y, int height, int width, ConsoleColor borderColor, ConsoleColor borderTextColor, ConsoleColor bgColor, int zIndex) {
        this.windowManager = windowManager;

        this.curses = windowManager.getCurses();
        this.context = windowManager.getContext();

        this.window = Rectangle.newRect(x, y, height, width);
        this.title = title;

        this.borderColor = borderColor;
        this.borderTextColor = borderTextColor;
        this.bgColor = bgColor;

        this.zIndex = zIndex;
    }

    public boolean onMouseClickEvent(MouseKeyCode keyCode) throws IOException {
        if (window.isInside(keyCode.getX(), keyCode.getY())) {
            int x = keyCode.getX() - window.getX();
            int y = keyCode.getY() - window.getY();

            if (keyCode.getKey().equals(MouseKey.LEFT)) {
                for (Rectangle button: buttons.keySet()) {
                    if (button.isInside(x, y)) {
                        buttons.get(button).onClick();

                        return true;
                    }
                }
            }

            if (keyCode.getKey().equals(MouseKey.RIGHT)) {
                for (Rectangle contextMenu: contextMenus.keySet()) {
                    if (contextMenu.isInside(x, y)) {
                        contextMenus.get(contextMenu).onClick();

                        return true;
                    }
                }
            }

            onMouseClick(keyCode.getKey(), x, y);
            return true;
        }

        return false;
    }

    public void onKeyPressEvent(KeyCode keyCode) throws IOException {
        if (keyCode.equals(KeyCode.of('f'))) {
            freeze = !freeze;
        }

        onKeyPress(keyCode);
    }

    public void draw() throws IOException {
        draw(false);
    }

    public void draw(boolean dirty) throws IOException {
        if (curses.isPossibleToRender(window) && !freeze) {
            curses.draw(window, borderColor, borderTextColor, title);

            buttons.clear();
            contextMenus.clear();

            if (dirty)
                erase();
            onDraw();
        }
    }

    protected Rectangle button(int x, int y, String text, WindowOnClickHandler onClickHandler) throws IOException {
        Rectangle rect = Rectangle.newRect(x - 1, y - 1, 0, text.length() + 2);

        write(rect.getX(), rect.getY(), BLACK, BRIGHT_WHITE, BOLD, "[%s]", text);

        buttons.put(rect, onClickHandler);
        return rect;
    }


    protected Rectangle contextMenu(int x, int y, int length, WindowOnClickHandler onClickHandler) throws IOException {
        Rectangle rect = Rectangle.newRect(x - 1, y - 1, 0, length);

        contextMenus.put(rect, onClickHandler);
        return rect;
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

    public boolean isHidden() {
        return hidden;
    }

    public void hide() {
        this.hidden = true;
    }

    public void show() {
        this.hidden = false;
    }

    public int getZIndex() {
        return zIndex;
    }
}
