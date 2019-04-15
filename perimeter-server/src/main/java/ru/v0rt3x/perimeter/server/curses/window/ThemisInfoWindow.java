package ru.v0rt3x.perimeter.server.curses.window;

import ru.v0rt3x.perimeter.server.curses.CursesConsoleUtils;
import ru.v0rt3x.perimeter.server.curses.utils.KeyCode;
import ru.v0rt3x.perimeter.server.curses.utils.MouseKey;
import ru.v0rt3x.perimeter.server.curses.utils.Window;
import ru.v0rt3x.perimeter.server.themis.ThemisClient;

import java.io.IOException;

import static ru.v0rt3x.perimeter.server.shell.console.ConsoleColor.BLUE;
import static ru.v0rt3x.perimeter.server.shell.console.ConsoleColor.BRIGHT_WHITE;
import static ru.v0rt3x.perimeter.server.shell.console.ConsoleTextStyle.BOLD;

public class ThemisInfoWindow extends Window {

    private ThemisClient themisClient;

    public ThemisInfoWindow(CursesConsoleUtils curses, ThemisClient themisClient) {
        super(curses, "Themis Info", 2, 49, 6, 23, BLUE, BRIGHT_WHITE, null);

        this.themisClient = themisClient;
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {
        // do nothing
    }

    @Override
    protected void onDraw() throws IOException {
        write(2, 2, BRIGHT_WHITE, BOLD, "State: %s", themisClient.getContestState());
        write(3, 2, BRIGHT_WHITE, BOLD, "Round: %d", themisClient.getContestRound());
    }

    @Override
    public void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
