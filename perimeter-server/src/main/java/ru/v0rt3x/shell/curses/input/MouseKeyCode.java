package ru.v0rt3x.shell.curses.input;

public class MouseKeyCode {

    private final MouseKey key;
    private final byte keyCode;

    private final int x;
    private final int y;

    public MouseKeyCode(byte[] keyCodeBytes) {
        switch (keyCodeBytes[0]) {
            case 0x20:
                key = MouseKey.LEFT;
                break;
            case 0x21:
                key = MouseKey.MIDDLE;
                break;
            case 0x22:
                key = MouseKey.RIGHT;
                break;
            case 0x60:
                key = MouseKey.SCROLL_UP;
                break;
            case 0x61:
                key = MouseKey.SCROLL_DOWN;
                break;
            default:
                key = MouseKey.UNKNOWN;
                break;
        }
        keyCode = keyCodeBytes[0];

        x = (keyCodeBytes[2] & 0xFF) - 32;
        y = (keyCodeBytes[1] & 0xFF) - 32;
    }


    public MouseKey getKey() {
        return key;
    }

    public byte getKeyCode() {
        return keyCode;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
