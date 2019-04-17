package ru.v0rt3x.perimeter.server.dashboard.window.modal;

import ru.v0rt3x.perimeter.server.exploit.ExploitManager;
import ru.v0rt3x.perimeter.server.flag.FlagProcessor;
import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.window.WindowManager;
import ru.v0rt3x.shell.curses.window.modal.ModalWindow;

import java.io.IOException;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BLACK;
import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BRIGHT_WHITE;

public class FlagQueueContextMenuWindow extends ModalWindow {

    public FlagQueueContextMenuWindow(WindowManager windowManager) {
        super(windowManager, "Flag Queue Menu", (windowManager.getCurses().getScreenHeight() - 5) / 2, (windowManager.getCurses().getScreenWidth() - 28) / 2, 5, 28, BLACK, BRIGHT_WHITE, null, 2);
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        button(2, 2, "Clear flag queue", this::clearFlagQueue);
    }

    private void clearFlagQueue() throws IOException {
        windowManager.showConfirmDialog(
            "You are going to delete all flags from queue. Are you sure?",
            this::onDeleteConfirm, this::onDeleteCancel
        );

        hide();
        windowManager.draw(true);
    }

    private void onDeleteConfirm() {
        context.getBean(FlagProcessor.class).clearQueue();
        context.getBean(ExploitManager.class).clearStats();
    }

    private void onDeleteCancel() {

    }

    @Override
    protected void onKeyPress(KeyCode keyCode) throws IOException {

    }
}
