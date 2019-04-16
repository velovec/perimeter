package ru.v0rt3x.shell.curses.handlers;

import ru.v0rt3x.shell.curses.input.MouseKeyCode;

import java.io.IOException;

@FunctionalInterface
public interface CursesMouseInputHandler {

    void onMouseClick(MouseKeyCode keyCode) throws IOException;
}
