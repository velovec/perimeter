package ru.v0rt3x.shell.curses.handlers;

import ru.v0rt3x.shell.curses.input.KeyCode;

import java.io.IOException;

@FunctionalInterface
public interface CursesInputHandler {

    void onKeyPress(KeyCode keyCode) throws IOException;
}
