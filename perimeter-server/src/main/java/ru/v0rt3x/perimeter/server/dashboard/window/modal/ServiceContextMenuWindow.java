package ru.v0rt3x.perimeter.server.dashboard.window.modal;

import ru.v0rt3x.perimeter.server.service.dao.Service;
import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.window.WindowManager;
import ru.v0rt3x.shell.curses.window.modal.ModalWindow;

import java.io.IOException;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BLACK;
import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BRIGHT_WHITE;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.BOLD;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.NORMAL;

public class ServiceContextMenuWindow extends ModalWindow {

    private Service service;

    public ServiceContextMenuWindow(WindowManager windowManager) {
        super(windowManager, "Service Menu", (windowManager.getCurses().getScreenHeight() - 7) / 2, (windowManager.getCurses().getScreenWidth() - 28) / 2, 7, 28, BLACK, BRIGHT_WHITE, null, 2);
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        write(2, 2, BRIGHT_WHITE, BOLD, "Service: ");
        write(2, 12, BRIGHT_WHITE, NORMAL, service.getName());

        button(4, 2, "Set Judas target", this::setJudasTarget);
    }

    private void setJudasTarget() throws IOException {
        JudasTargetDialogWindow dialogWindow = windowManager.createWindow(JudasTargetDialogWindow.class, "judas_target_menu");
        dialogWindow.setService(service);
        dialogWindow.draw(true);

        hide();
        windowManager.draw(true);
    }

    @Override
    protected void onKeyPress(KeyCode keyCode) throws IOException {

    }

    public void setService(Service service) {
        this.service = service;
    }
}
