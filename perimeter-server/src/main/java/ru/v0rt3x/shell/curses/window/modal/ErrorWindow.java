package ru.v0rt3x.shell.curses.window.modal;

import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKey;
import ru.v0rt3x.shell.curses.window.Rectangle;
import ru.v0rt3x.shell.curses.window.WindowManager;

import java.io.IOException;
import java.util.Objects;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.*;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.*;

public class ErrorWindow extends ModalWindow {

    private String errorMessage = "";

    public ErrorWindow(WindowManager windowManager) {
        super(windowManager, "Error", (windowManager.getCurses().getScreenHeight() - 9) / 2 , (windowManager.getCurses().getScreenWidth() - 70) / 2, 9, 70, BRIGHT_RED, BRIGHT_WHITE, null, 2);

        hotkey(KeyCode.ENTER, (keyCode) -> onConfirm());
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {

    }

    @Override
    protected void onDraw() throws IOException {
        String[] message = curses.wrapMultiLine(errorMessage, window.getWidth() - 4, window.getHeight() - 6);

        int line = 2;
        for (String messageLine: message) {
            if (Objects.nonNull(messageLine)) {
                write(line, 2, BRIGHT_WHITE, NORMAL, messageLine);
            }
            line++;
        }

        button(window.getHeight() - 4, (window.getWidth() - 4) / 2, "OK", this::onConfirm);
    }

    private void onConfirm() throws IOException {
        hide();
        windowManager.draw(true);
    }

    @Override
    protected void onKeyPress(KeyCode keyCode) throws IOException {

    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
