package ru.v0rt3x.shell.curses.handlers;

import ru.v0rt3x.shell.curses.window.WindowManager;

import java.io.IOException;

@FunctionalInterface
public interface WindowManagerInitHandler {

    void onInit(WindowManager windowManager) throws IOException;
}
