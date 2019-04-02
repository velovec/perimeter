package ru.v0rt3x.perimeter.server.curses;

import ru.v0rt3x.perimeter.server.curses.utils.KeyCode;

import java.io.IOException;

@FunctionalInterface
public interface CursesInputHandler {

    void onKeyPress(KeyCode keyCode) throws IOException;
}
