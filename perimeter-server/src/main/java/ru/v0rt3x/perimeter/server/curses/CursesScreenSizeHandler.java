package ru.v0rt3x.perimeter.server.curses;

import java.io.IOException;

@FunctionalInterface
public interface CursesScreenSizeHandler {

    void onScreenSizeChange() throws IOException;
}
