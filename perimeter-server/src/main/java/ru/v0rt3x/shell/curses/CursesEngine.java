package ru.v0rt3x.shell.curses;

import org.apache.sshd.server.Environment;

import ru.v0rt3x.shell.curses.handlers.CursesInputHandler;
import ru.v0rt3x.shell.curses.handlers.CursesMouseInputHandler;
import ru.v0rt3x.shell.curses.handlers.CursesScreenSizeHandler;
import ru.v0rt3x.shell.console.ansi.ANSIUtils;
import ru.v0rt3x.shell.console.ansi.ConsoleColor;
import ru.v0rt3x.shell.console.ansi.ConsoleTextStyle;
import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseUtils;
import ru.v0rt3x.shell.curses.window.Rectangle;

import java.io.*;
import java.util.*;

import static ru.v0rt3x.shell.console.ansi.ConsoleColor.BRIGHT_WHITE;
import static ru.v0rt3x.shell.console.ansi.ConsoleTextStyle.NORMAL;

public class CursesEngine {

    private final InputStream input;
    private final OutputStream output;
    private final OutputStream error;

    private final OutputStreamWriter outputStreamWriter;
    private final OutputStreamWriter errorStreamWriter;

    private final Environment env;

    private final Timer timer;
    private final Object lock;

    private Rectangle screen;

    private Map<KeyCode, CursesInputHandler> inputHandlers = new HashMap<>();

    private CursesInputHandler defaultInputHandler = (k) -> {};
    private CursesMouseInputHandler mouseInputHandler = (k) -> {};

    private CursesScreenSizeHandler screenSizeChangeHandler;

    public CursesEngine(InputStream input, OutputStream output, OutputStream error, Environment env) {
        this.input = input;
        this.output = output;
        this.error = error;

        this.env = env;
        this.lock = new Object();

        this.outputStreamWriter = new OutputStreamWriter(this.output);
        this.errorStreamWriter = new OutputStreamWriter(this.error);

        this.timer = new Timer();
    }

    public int getScreenWidth() {
        return Integer.parseInt(env.getEnv().get("COLUMNS"));
    }

    public int getScreenHeight() {
        return Integer.parseInt(env.getEnv().get("LINES"));
    }

    public void init() throws IOException {
        if (!checkTTY())
            throw new IOException("PTY is not available. Use '-t' SSH option or interactive shell.");

        this.clear();
        this.screen = Rectangle.newRect(0, 0, getScreenHeight(), getScreenWidth());

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
            if (isScreenSizeChanged()) {
                try {
                    init();

                    if (Objects.nonNull(screenSizeChangeHandler)) {
                        screenSizeChangeHandler.onScreenSizeChange();
                    }
                } catch (IOException e) {
                    // do nothing
                }
            }
            }
        }, 2000L, 2000L);
    }

    public void draw(Rectangle rect, ConsoleColor borderColor, ConsoleColor textColor, String title) throws IOException {
        title = String.format("-[ %s ]-", title);

        draw(rect, borderColor);
        write(rect, 0, rect.getWidth() / 2 - title.length() / 2, borderColor, textColor, ConsoleTextStyle.BOLD, title);
    }

    public void draw(Rectangle rect, ConsoleColor borderColor) throws IOException {
        int minX = rect.getX(), minY = rect.getY(), maxX = rect.getX() + rect.getHeight(), maxY = rect.getY() + rect.getWidth();

        write(ANSIUtils.CursorPosition(minX, minY));
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                if ((x == minX) || (x == maxX - 1) || (y == minY) || (y == maxY - 1)) {
                    write(ANSIUtils.CursorPosition(x, y), ANSIUtils.RenderString(" ", BRIGHT_WHITE, borderColor));
                }
            }
        }
        write(ANSIUtils.CursorPosition(minX, minY));
    }

    private void write(String... data) throws IOException {
        synchronized (lock) {
            for (String line : data) {
                outputStreamWriter.write(line);
                outputStreamWriter.flush();
            }
        }
    }

    private void error(String... data) throws IOException {
        for (String line: data) {
            errorStreamWriter.write(line);
            errorStreamWriter.flush();
        }
    }

    public void write(Rectangle rect, int x, int y, ConsoleColor bgColor, ConsoleColor textColor, ConsoleTextStyle textStyle, String text) throws IOException {
        if ((x < rect.getHeight()) && (y < rect.getWidth())) {
            write(
                ANSIUtils.CursorPosition(rect.getX() + x, rect.getY() + y),
                ANSIUtils.RenderString(text, textColor, bgColor, textStyle)
            );
        }
        write(ANSIUtils.CursorPosition(rect.getX(), rect.getY()));
    }

    public void read() throws IOException {
        if (input.available() == 0)
            return;

        byte[] buffer = new byte[input.available()];
        int bytesRead = input.read(buffer);

        if (bytesRead == 0)
            return;

        KeyCode keyCode = KeyCode.of(buffer);

        if (MouseUtils.isMouseKeyCode(keyCode)) {
            mouseInputHandler.onMouseClick(MouseUtils.toMouseKeyCode(keyCode));
        } else if (inputHandlers.containsKey(keyCode)) {
            inputHandlers.get(keyCode).onKeyPress(keyCode);
        } else {
            defaultInputHandler.onKeyPress(keyCode);
        }
    }

    public void clear() throws IOException {
        write(ANSIUtils.CursorPosition(0, 0));
        write(ANSIUtils.EraseData(2));
    }

    public void onKeyPress(CursesInputHandler inputHandler, KeyCode... keyCodes) {
        for (KeyCode keyCode: keyCodes) {
            inputHandlers.put(keyCode, inputHandler);
        }
    }

    public void onMouseClick(CursesMouseInputHandler inputHandler) {
        this.mouseInputHandler = inputHandler;
    }

    public void onKeyPress(CursesInputHandler inputHandler) {
        this.defaultInputHandler = inputHandler;
    }

    private boolean checkTTY() {
        return env.getPtyModes().size() > 0;
    }

    private boolean isScreenSizeChanged() {
        return (screen.getHeight() != getScreenHeight()) || (screen.getWidth() != getScreenWidth());
    }

    public void onScreenSizeChange(CursesScreenSizeHandler screenSizeHandler) {
        this.screenSizeChangeHandler = screenSizeHandler;
    }

    public boolean isPossibleToRender(Rectangle rect) {
        return (getScreenHeight() + 1 >= rect.getX() + rect.getHeight()) &&
            (getScreenWidth() + 1 >= rect.getY() + rect.getWidth());
    }

    public String wrapLine(String line, int length) {
        if (line.length() > length) {
            line = line.substring(0, length - 1) + "~";
        }

        String formatString = String.format("%%-%ds", length);

        return String.format(formatString, line).toUpperCase();
    }

    public String[] wrapMultiLine(String line, int length, int lines) {
        if (line.length() > length * lines) {
            line = line.substring(0, length * lines - 3) + "...";
        }

        String[] wrappedLine = new String[lines];

        int i = 0;
        while (line.length() > length) {
            wrappedLine[i] = line.substring(0, length);
            line = line.substring(length);
            i++;
        }

        wrappedLine[i] = line;

        return wrappedLine;
    }

    public void erase(Rectangle rect, int startLine, int lines, ConsoleColor bgColor) throws IOException {
        for (int line = 0; line < lines; line++) {
            write(rect, startLine + line, 1, bgColor, BRIGHT_WHITE, NORMAL, wrapLine("", rect.getWidth() - 2));
        }
    }

    public void mouseEnable() throws IOException {
        write(ANSIUtils.DECSet(9));
    }

    public void mouseDisable() throws IOException {
        write(ANSIUtils.DECReset(9));
    }
}
