package ru.v0rt3x.perimeter.server.curses.window;

import ru.v0rt3x.perimeter.server.curses.CursesConsoleUtils;
import ru.v0rt3x.perimeter.server.curses.utils.KeyCode;
import ru.v0rt3x.perimeter.server.curses.utils.MouseKey;
import ru.v0rt3x.perimeter.server.curses.utils.Window;
import ru.v0rt3x.perimeter.server.exploit.dao.ExploitExecutionResultRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static ru.v0rt3x.perimeter.server.shell.console.ConsoleColor.BLUE;
import static ru.v0rt3x.perimeter.server.shell.console.ConsoleColor.BRIGHT_WHITE;
import static ru.v0rt3x.perimeter.server.shell.console.ConsoleTextStyle.BOLD;
import static ru.v0rt3x.perimeter.server.shell.console.ConsoleTextStyle.NORMAL;

public class TeamStatisticWindow extends Window {

    private final ExploitExecutionResultRepository resultRepository;

    public TeamStatisticWindow(CursesConsoleUtils curses, ExploitExecutionResultRepository resultRepository) {
        super(curses, "Team Statistics", 22, 108, Math.max(7, curses.getScreenHeight() - 24), 31, BLUE, BRIGHT_WHITE, null);

        this.resultRepository = resultRepository;
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        write(2, 2, BRIGHT_WHITE, BOLD, "Team");
        write(2, 22, BRIGHT_WHITE, BOLD, "Hits");

        Map<String, Integer> teamHits = new HashMap<>();
        resultRepository.findAll().forEach(
            result -> {
                int hits = teamHits.getOrDefault(result.getTeam(), 0);

                teamHits.put(result.getTeam(), hits + result.getHits());
            }
        );

        int line = 4;
        for (String team: teamHits.keySet()) {
            write(line, 2, BRIGHT_WHITE, NORMAL, curses.wrapLine(team, 19));
            write(line, 22, BRIGHT_WHITE, NORMAL, String.format("%07d", teamHits.get(team)));
            line++;
        }
    }

    @Override
    public void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
