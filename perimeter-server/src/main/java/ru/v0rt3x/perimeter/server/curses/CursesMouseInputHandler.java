package ru.v0rt3x.perimeter.server.curses;

import ru.v0rt3x.perimeter.server.curses.utils.MouseKeyCode;

import java.io.IOException;

@FunctionalInterface
public interface CursesMouseInputHandler {

    void onMouseClick(MouseKeyCode keyCode) throws IOException;
}
