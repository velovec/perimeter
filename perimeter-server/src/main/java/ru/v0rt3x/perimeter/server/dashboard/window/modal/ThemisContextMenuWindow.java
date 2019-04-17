package ru.v0rt3x.perimeter.server.dashboard.window.modal;

import ru.v0rt3x.perimeter.server.flag.FlagProcessor;
import ru.v0rt3x.perimeter.server.themis.ThemisClient;
import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.window.WindowManager;
import ru.v0rt3x.shell.curses.window.modal.ModalWindow;

import java.io.IOException;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BLACK;
import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BRIGHT_WHITE;

public class ThemisContextMenuWindow extends ModalWindow {

    public ThemisContextMenuWindow(WindowManager windowManager) {
        super(windowManager, "Themis Menu", (windowManager.getCurses().getScreenHeight() - 7) / 2, (windowManager.getCurses().getScreenWidth() - 28) / 2, 6, 28, BLACK, BRIGHT_WHITE, null, 2);
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        button(2, 2, "Update public key", this::updatePublicKey);
        button(3, 2, "Sync team list", this::syncTeamList);
    }

    private void updatePublicKey() throws IOException {
        String publicKey = context.getBean(ThemisClient.class).getPublicKey();
        context.getBean(FlagProcessor.class).setThemisPublicKey(publicKey);

        hide();
        windowManager.draw(true);
    }

    private void syncTeamList() throws IOException {
        TeamSyncDialogWindow dialogWindow = windowManager.createWindow(TeamSyncDialogWindow.class, "team_sync");
        dialogWindow.draw(true);

        hide();
        windowManager.draw(true);
    }

    @Override
    protected void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
