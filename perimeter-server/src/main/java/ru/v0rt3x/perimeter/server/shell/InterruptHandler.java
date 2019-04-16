package ru.v0rt3x.perimeter.server.shell;

public interface InterruptHandler {

    void onEOTEvent(); // Handles Control + D
    void onETXEvent(); // Handles Control + C
    void onSUBEvent(); // Handles Control + Z
}