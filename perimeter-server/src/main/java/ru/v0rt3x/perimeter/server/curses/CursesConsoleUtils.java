package ru.v0rt3x.perimeter.server.curses;

import org.apache.sshd.server.Environment;

import ru.v0rt3x.perimeter.server.curses.utils.*;
import ru.v0rt3x.perimeter.server.shell.console.ANSIUtils;
import ru.v0rt3x.perimeter.server.shell.console.ConsoleColor;
import ru.v0rt3x.perimeter.server.shell.console.ConsoleTextStyle;

import java.io.*;
import java.util.*;

public class CursesConsoleUtils {

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
    private CursesInputHandler defaultInputHandler;

    private CursesScreenSizeHandler screenSizeChangeHandler;

    public CursesConsoleUtils(InputStream input, OutputStream output, OutputStream error, Environment env, Object lock) {
        this.input = input;
        this.output = output;
        this.error = error;

        this.env = env;
        this.lock = lock;

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

    public void init(ConsoleColor borderColor, ConsoleColor textColor, String title) throws IOException {
        if (!checkTTY())
            throw new IOException("PTY is not available. Use '-t' SSH option or interactive shell.");

        this.clear();
        this.screen = Rectangle.newRect(0, 0, getScreenHeight(), getScreenWidth());

        synchronized (lock) {
            draw(this.screen, borderColor);
            write(this.screen, 0, getScreenWidth() / 2 - title.length() / 2, borderColor, textColor, ConsoleTextStyle.BOLD, String.format("-[ %s ]-", title));
        }

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
            if (isScreenSizeChanged()) {
                try {
                    init(borderColor, textColor, title);

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
                    write(ANSIUtils.CursorPosition(x, y), ANSIUtils.RenderString(" ", ConsoleColor.BRIGHT_WHITE, borderColor));
                }
            }
        }
        write(ANSIUtils.CursorPosition(minX, minY));
    }

    private void write(String... data) throws IOException {
        for (String line: data) {
            outputStreamWriter.write(line);
            outputStreamWriter.flush();
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

    public void write(int x, int y, ConsoleColor bgColor, ConsoleColor textColor, ConsoleTextStyle textStyle, String text) throws IOException {
        write(this.screen, x, y, bgColor, textColor, textStyle, text);
    }

    public void read() throws IOException {
        if (input.available() == 0)
            return;

        byte[] buffer = new byte[input.available()];
        int bytesRead = input.read(buffer);

        if (bytesRead == 0)
            return;

        KeyCode keyCode = KeyCode.of(buffer);

        if (inputHandlers.containsKey(keyCode)) {
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

    public Rectangle getScreen() {
        return screen;
    }

    public String wrapLine(String line, int length) {
        if (line.length() > length) {
            line = line.substring(0, length - 1) + "~";
        }

        String formatString = String.format("%%-%ds", length);

        return String.format(formatString, line).toUpperCase();
    }
}
