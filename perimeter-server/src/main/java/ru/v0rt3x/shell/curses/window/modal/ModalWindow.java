package ru.v0rt3x.shell.curses.window.modal;

import ru.v0rt3x.shell.console.ansi.ConsoleColor;
import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.window.Window;
import ru.v0rt3x.shell.curses.window.WindowManager;

import java.io.IOException;

public abstract class ModalWindow extends Window {

    public ModalWindow(WindowManager windowManager, String title, int x, int y, int height, int width, ConsoleColor borderColor, ConsoleColor borderTextColor, ConsoleColor bgColor, int zIndex) {
        super(windowManager, title, x, y, height, width, borderColor, borderTextColor, bgColor, zIndex);

        hotkey(KeyCode.ESCAPE, (keyCode) -> {
            hide();
            windowManager.draw(true);
        });
    }

    @Override
    public void draw(boolean dirty) throws IOException {
        super.draw(true); // Always perform 'dirty' draw
    }
}
