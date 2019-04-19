package ru.v0rt3x.perimeter.server.dashboard.window;

import ru.v0rt3x.perimeter.server.dashboard.window.modal.TeamContextMenuWindow;
import ru.v0rt3x.perimeter.server.team.dao.TeamRepository;
import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.window.Rectangle;
import ru.v0rt3x.shell.curses.window.Window;
import ru.v0rt3x.shell.curses.window.WindowManager;
import ru.v0rt3x.perimeter.server.exploit.dao.ExploitExecutionResultRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.*;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.BOLD;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.NORMAL;

public class TeamStatisticWindow extends Window {

    private final ExploitExecutionResultRepository resultRepository;
    private final TeamRepository teamRepository;

    public TeamStatisticWindow(WindowManager windowManager) {
        super(windowManager, "Team Statistics", 31, 108, Math.max(7, windowManager.getCurses().getScreenHeight() - 33), 34, MAGENTA, BRIGHT_WHITE, null, 1);

        this.resultRepository = context.getBean(ExploitExecutionResultRepository.class);
        this.teamRepository = context.getBean(TeamRepository.class);
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        write(2, 2, BRIGHT_WHITE, BOLD, "Team");
        write(2, 25, BRIGHT_WHITE, BOLD, "Hits");

        Map<String, Integer> teamHits = new HashMap<>();
        teamRepository.findAll().forEach(team -> teamHits.put(team.getName(), 0));

        resultRepository.findAll().forEach(
            result -> {
                int hits = teamHits.getOrDefault(result.getTeam(), 0);

                teamHits.put(result.getTeam(), hits + result.getHits());
            }
        );

        int line = 4;
        for (String team: teamHits.keySet()) {
            boolean enabled = teamRepository.findByName(team).isActive();

            Rectangle rect = contextMenu(line, 2, window.getWidth() - 4, () -> {
                TeamContextMenuWindow contextMenu = windowManager.createWindow(TeamContextMenuWindow.class, "team_menu");
                contextMenu.setTeam(teamRepository.findByName(team));

                contextMenu.draw(true);
                // windowManager.draw();
            });

            write(rect.getX(), 2, enabled ? BRIGHT_WHITE : WHITE, NORMAL, curses.wrapLine(team, 24));
            write(rect.getX(), 25, enabled ? BRIGHT_WHITE : WHITE, NORMAL, String.format("%07d", teamHits.get(team)));
            line++;
        }
    }

    @Override
    public void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
