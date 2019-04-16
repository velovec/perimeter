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
    private Rectangle okButton;

    public ErrorWindow(WindowManager windowManager) {
        super(windowManager, "Error", (windowManager.getCurses().getScreenHeight() - 9) / 2 , (windowManager.getCurses().getScreenWidth() - 70) / 2, 9, 70, BRIGHT_RED, BRIGHT_WHITE, null, 2);

        okButton = Rectangle.newRect(window.getHeight() - 4, (window.getWidth() - 4) / 2, 0, 4);
    }

    @Override
    protected void onMouseClick(MouseKey key, int x, int y) throws IOException {
        if (key.equals(MouseKey.LEFT) && okButton.isInside(x, y)) {
            hide();

            windowManager.draw(true);
        }
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

        write(okButton.getX(), okButton.getY(), BRIGHT_BLACK, BRIGHT_WHITE, BOLD, "[OK]");
    }

    @Override
    protected void onKeyPress(KeyCode keyCode) throws IOException {
        if (keyCode.equals(KeyCode.ENTER)) {
            hide();

            windowManager.draw(true);
        }
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
