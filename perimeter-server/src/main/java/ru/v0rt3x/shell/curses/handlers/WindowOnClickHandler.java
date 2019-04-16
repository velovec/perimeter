package ru.v0rt3x.shell.curses.handlers;

import java.io.IOException;

@FunctionalInterface
public interface WindowOnClickHandler {

    void onClick() throws IOException;
}
