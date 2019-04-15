package ru.v0rt3x.perimeter.server.curses.utils;

public class MouseUtils {

    private static final byte[] MOUSE_EVENT = new byte[] { 0x1B, 0x5B, 0x4D };

    public static boolean isMouseKeyCode(KeyCode keyCode) {
        byte[] keyCodeBytes = keyCode.getCode();

        if (keyCodeBytes.length != 6)
            return false;

        for (int i = 0; i < MOUSE_EVENT.length; i++) {
            if (MOUSE_EVENT[i] != keyCodeBytes[i]) {
                return false;
            }
        }

        return true;
    }

    public static MouseKeyCode toMouseKeyCode(KeyCode keyCode) {
        byte[] keyCodeBytes = keyCode.getCode();
        byte[] mouseKeyCodeBytes = new byte[keyCodeBytes.length - 3];

        System.arraycopy(keyCodeBytes, 3, mouseKeyCodeBytes, 0, mouseKeyCodeBytes.length);

        return new MouseKeyCode(mouseKeyCodeBytes);
    }
}
