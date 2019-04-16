package ru.v0rt3x.shell.curses.window;

import org.springframework.context.ConfigurableApplicationContext;

import ru.v0rt3x.shell.curses.CursesEngine;
import ru.v0rt3x.shell.curses.handlers.CursesInputHandler;
import ru.v0rt3x.shell.curses.handlers.CursesMouseInputHandler;
import ru.v0rt3x.shell.curses.handlers.CursesScreenSizeHandler;
import ru.v0rt3x.shell.curses.handlers.WindowManagerInitHandler;
import ru.v0rt3x.shell.curses.input.KeyCode;
import ru.v0rt3x.shell.curses.input.MouseKeyCode;
import ru.v0rt3x.shell.curses.window.modal.DialogWindow;
import ru.v0rt3x.shell.curses.window.modal.ErrorWindow;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

public class WindowManager implements CursesInputHandler, CursesMouseInputHandler, CursesScreenSizeHandler {

    private final CursesEngine curses;
    private final ConfigurableApplicationContext context;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    private final WindowManagerInitHandler initHandler;

    private boolean showSplash;
    private long splashDuration;

    public <T extends Window> WindowManager(CursesEngine curses, ConfigurableApplicationContext context, WindowManagerInitHandler initHandler, Class<T> splash, long splashDuration) {
        this.curses = curses;
        this.context = context;
        this.initHandler = initHandler;

        this.showSplash = Objects.nonNull(splash);
        if (this.showSplash) {
            createWindow(splash, "splash");
            this.splashDuration = splashDuration;
        }

        this.curses.onMouseClick(this);
        this.curses.onKeyPress(this);
        this.curses.onScreenSizeChange(this);
    }

    public void init() throws IOException {
        this.curses.init();
        this.initHandler.onInit(this);

        if (showSplash) {
            showSplashWindow();
            showSplash = false;
        }
    }

    public <T extends Window> T createWindow(Class<T> window, String name) {
        try {
            T windowInstance = window.getConstructor(WindowManager.class).newInstance(this);
            windows.put(name, windowInstance);

            return windowInstance;
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void deleteWindow(String name) {
        windows.remove(name);
    }

    public void deleteWindows() {
        windows.clear();
    }

    private void showSplashWindow() throws IOException {
        if (windows.containsKey("splash")) {
            hideAllExcept("splash");
            draw();

            try {
                sleep(splashDuration);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }

            deleteWindow("splash");
            showAllExcept();
            draw(true);
        }
    }

    public void showErrorMessage(String format, Object... args) throws IOException {
        ErrorWindow errorWindow = createWindow(ErrorWindow.class, "error");

        errorWindow.setErrorMessage(String.format(format, args));

        draw();
    }

    public void showConfirmDialog(String message, DialogWindow.DialogConfirmHandler confirmHandler, DialogWindow.DialogCancelHandler cancelHandler) throws IOException {
        DialogWindow dialogWindow = createWindow(DialogWindow.class, "confirm_dialog");

        dialogWindow.setDialogMessage(message);
        dialogWindow.setConfirmHandler(confirmHandler);
        dialogWindow.setCancelHandler(cancelHandler);

        draw();
    }

    public void draw() throws IOException {
        draw(false);
    }

    public void draw(boolean dirty) throws IOException {
        List<Window> windowList = windows.values().stream()
            .sorted(Comparator.comparingInt(Window::getZIndex))
            .filter(window -> !window.isHidden())
            .collect(Collectors.toList());

        for (Window window: windowList) {
            window.draw(dirty);
        }
    }

    public void draw(String name) throws IOException {
        Window window = windows.get(name);

        if (Objects.isNull(window)) {
            throw new IllegalArgumentException(String.format("Window '%s' doesn't exists", name));
        }

        if (window.isHidden()) {
            window.show();
        }

        window.draw();
    }

    public void hide(String name) {
        Window window = windows.get(name);

        if (Objects.isNull(window)) {
            throw new IllegalArgumentException(String.format("Window '%s' doesn't exists", name));
        }

        if (!window.isHidden()) {
            window.hide();
        }
    }

    public void hideAllExcept(String... names) {
        for (String windowName: windows.keySet()) {
            if (Arrays.stream(names).noneMatch(windowName::equals)) {
                windows.get(windowName).hide();
            }
        }
    }

    public void showAllExcept(String... names) {
        for (String windowName: windows.keySet()) {
            if (Arrays.stream(names).noneMatch(windowName::equals)) {
                windows.get(windowName).show();
            }
        }
    }

    @Override
    public void onKeyPress(KeyCode keyCode) throws IOException {
        if (keyCode.equals(KeyCode.of('r'))) {
            draw();
        }

        for (String windowName: windows.keySet()) {
            windows.get(windowName).onKeyPressEvent(keyCode);
        }
    }

    @Override
    public void onMouseClick(MouseKeyCode keyCode) throws IOException {
        List<Window> windowList = windows.values().stream()
            .sorted(Comparator.comparingInt(Window::getZIndex).reversed())
            .filter(window -> !window.isHidden())
            .collect(Collectors.toList());

        for (Window window: windowList) {
            if (window.onMouseClickEvent(keyCode)) {
                break;
            }
        }
    }

    @Override
    public void onScreenSizeChange() throws IOException {
        init();
        draw();
    }

    public CursesEngine getCurses() {
        return curses;
    }

    public ConfigurableApplicationContext getContext() {
        return context;
    }
}
