package ru.v0rt3x.perimeter.server.dashboard.window.modal;

import ru.v0rt3x.perimeter.server.judas.JudasManager;
import ru.v0rt3x.perimeter.server.service.dao.Service;
import ru.v0rt3x.perimeter.server.team.dao.Team;
import ru.v0rt3x.perimeter.server.team.dao.TeamRepository;
import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.window.WindowManager;
import ru.v0rt3x.shell.curses.window.modal.ModalWindow;

import java.io.IOException;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BLACK;
import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BRIGHT_WHITE;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.BOLD;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.NORMAL;

public class JudasTargetDialogWindow extends ModalWindow {

    private Service service;

    public JudasTargetDialogWindow(WindowManager windowManager) {
        super(windowManager, "Select Judas Target", (windowManager.getCurses().getScreenHeight() - 26) / 2, (windowManager.getCurses().getScreenWidth() - 40) / 2, 26, 40, BLACK, BRIGHT_WHITE, null, 2);
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        write(2, 2, BRIGHT_WHITE, BOLD, "Port: ");
        write(2, 8, BRIGHT_WHITE, NORMAL, "%d", service.getPort());

        int line = 4;
        for (Team team: context.getBean(TeamRepository.class).findAllByActive(true)) {
            button(line, 2, String.format("%15s: %s", team.getIp(), team.getName()), () -> {
                context.getBean(JudasManager.class).setTarget(service.getPort(), team.getIp());

                hide();
                windowManager.draw(true);
            });
            line++;
        }
    }

    @Override
    protected void onKeyPress(KeyCode keyCode) throws IOException {

    }

    public void setService(Service service) {
        this.service = service;
    }
}
