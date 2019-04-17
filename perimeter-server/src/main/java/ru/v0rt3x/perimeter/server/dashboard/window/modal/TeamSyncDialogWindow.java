package ru.v0rt3x.perimeter.server.dashboard.window.modal;

import ru.v0rt3x.perimeter.server.team.TeamManager;
import ru.v0rt3x.perimeter.server.team.dao.Team;
import ru.v0rt3x.perimeter.server.themis.ThemisClient;
import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.window.Rectangle;
import ru.v0rt3x.shell.curses.window.WindowManager;
import ru.v0rt3x.shell.curses.window.modal.ModalWindow;

import java.io.IOException;
import java.util.List;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BLACK;
import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BRIGHT_WHITE;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.*;

public class TeamSyncDialogWindow extends ModalWindow {

    public TeamSyncDialogWindow(WindowManager windowManager) {
        super(windowManager, "Team Sync", (windowManager.getCurses().getScreenHeight() - 26) / 2, (windowManager.getCurses().getScreenWidth() - 40) / 2, 26, 40, BLACK, BRIGHT_WHITE, null, 2);
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        write(2, 2, BRIGHT_WHITE, NORMAL, "Team");
        write(2, 23, BRIGHT_WHITE, NORMAL, "IP");

        int line = 4;
        List<Team> teamList = context.getBean(ThemisClient.class).getTeamList();

        for (Team team: teamList) {
            Rectangle rect = contextMenu(line, 2, window.getWidth() - 4, () -> {});

            write(rect.getX(), 2, BRIGHT_WHITE, NORMAL, curses.wrapLine(team.getName(), 20));
            write(rect.getX(), 23, BRIGHT_WHITE, NORMAL, "%-15s", team.getIp());

            line++;
        }

        button(window.getHeight() - 2, (window.getWidth() - 10) / 2, "Yes", () -> onConfirm(teamList));
        button(window.getHeight() - 2, (window.getWidth() - 10) / 2 + 6, "No", this::onCancel);
    }

    private void onConfirm(List<Team> teamList) throws IOException {
        context.getBean(TeamManager.class).replaceTeams(teamList);

        hide();
        windowManager.draw(true);
    }

    private void onCancel() throws IOException {
        hide();
        windowManager.draw(true);
    }

    @Override
    protected void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
