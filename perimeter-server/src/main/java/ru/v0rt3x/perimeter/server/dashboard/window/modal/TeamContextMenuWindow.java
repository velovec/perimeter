package ru.v0rt3x.perimeter.server.dashboard.window.modal;

import ru.v0rt3x.perimeter.server.team.TeamManager;
import ru.v0rt3x.perimeter.server.team.dao.Team;
import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.window.WindowManager;
import ru.v0rt3x.shell.curses.window.modal.ModalWindow;

import java.io.IOException;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BLACK;
import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BRIGHT_WHITE;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.BOLD;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.NORMAL;

public class TeamContextMenuWindow extends ModalWindow {

    private Team team;

    public TeamContextMenuWindow(WindowManager windowManager) {
        super(windowManager, "Team Menu", (windowManager.getCurses().getScreenHeight() - 9) / 2, (windowManager.getCurses().getScreenWidth() - 28) / 2, 7, 28, BLACK, BRIGHT_WHITE, null, 2);
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        write(2, 2, BRIGHT_WHITE, BOLD, "Team: ");
        write(2, 11, BRIGHT_WHITE, NORMAL, team.getName());

        button(4, 2, team.isActive() ? "Disable" : "Enable", this::toggleActive);
    }

    private void toggleActive() throws IOException {
        context.getBean(TeamManager.class).setActive(team, !team.isActive());

        hide();
        windowManager.draw(true);
    }

    @Override
    protected void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
