package ru.v0rt3x.shell.curses.window.modal;

import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.window.WindowManager;

import java.io.IOException;
import java.util.Objects;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.*;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.NORMAL;

public class DialogWindow extends ModalWindow {

    private String dialogMessage = "";

    private DialogConfirmHandler confirmHandler;
    private DialogCancelHandler cancelHandler;

    public DialogWindow(WindowManager windowManager) {
        super(windowManager, "Confirm", (windowManager.getCurses().getScreenHeight() - 9) / 2 , (windowManager.getCurses().getScreenWidth() - 70) / 2, 9, 70, BRIGHT_RED, BRIGHT_WHITE, null, 2);

        hotkey(KeyCode.ENTER, (keyCode) -> onConfirm());
        hotkey(KeyCode.ESCAPE, (keyCode) -> onCancel());
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        String[] message = curses.wrapMultiLine(dialogMessage, window.getWidth() - 4, window.getHeight() - 6);

        int line = 2;
        for (String messageLine: message) {
            if (Objects.nonNull(messageLine)) {
                write(line, 2, BRIGHT_WHITE, NORMAL, messageLine);
            }
            line++;
        }

        button(window.getHeight() - 4, (window.getWidth() - 11) / 2, "Yes", this::onConfirm);
        button(window.getHeight() - 4, (window.getWidth() - 11) / 2 + 7, "No", this::onCancel);
    }

    @Override
    protected void onKeyPress(KeyCode keyCode) throws IOException {

    }

    public void setDialogMessage(String dialogMessage) {
        this.dialogMessage = dialogMessage;
    }

    public void setConfirmHandler(DialogConfirmHandler confirmHandler) {
        this.confirmHandler = confirmHandler;
    }

    public void setCancelHandler(DialogCancelHandler cancelHandler) {
        this.cancelHandler = cancelHandler;
    }

    private void onConfirm() throws IOException {
        if (Objects.nonNull(confirmHandler))
            confirmHandler.onConfirm();

        hide();
        windowManager.draw(true);
    }

    private void onCancel() throws IOException {
        if (Objects.nonNull(cancelHandler))
            cancelHandler.onCancel();

        hide();
        windowManager.draw(true);
    }

    @FunctionalInterface
    public interface DialogConfirmHandler {

        void onConfirm();

    }

    @FunctionalInterface
    public interface DialogCancelHandler {

        void onCancel();

    }
}
