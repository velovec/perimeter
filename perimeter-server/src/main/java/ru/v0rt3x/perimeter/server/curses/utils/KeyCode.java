package ru.v0rt3x.perimeter.server.curses.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeyCode {

    public static final KeyCode ESCAPE = KeyCode.of(0x1B);

    public static final KeyCode F1  = KeyCode.of(0x1B, 0x4F, 0x50);
    public static final KeyCode F2  = KeyCode.of(0x1B, 0x4F, 0x51);
    public static final KeyCode F3  = KeyCode.of(0x1B, 0x4F, 0x52);
    public static final KeyCode F4  = KeyCode.of(0x1B, 0x4F, 0x53);
    public static final KeyCode F5  = KeyCode.of(0x1B, 0x5B, 0x31, 0x35, 0x7E);
    public static final KeyCode F6  = KeyCode.of(0x1B, 0x5B, 0x31, 0x37, 0x7E);
    public static final KeyCode F7  = KeyCode.of(0x1B, 0x5B, 0x31, 0x38, 0x7E);
    public static final KeyCode F8  = KeyCode.of(0x1B, 0x5B, 0x31, 0x39, 0x7E);
    public static final KeyCode F9  = KeyCode.of(0x1B, 0x5B, 0x32, 0x30, 0x7E);
    public static final KeyCode F10 = KeyCode.of(0x1B, 0x5B, 0x32, 0x31, 0x7E);
    public static final KeyCode F11 = KeyCode.of(0x1B, 0x5B, 0x32, 0x32, 0x7E);
    public static final KeyCode F12 = KeyCode.of(0x1B, 0x5B, 0x32, 0x34, 0x7E);

    public static final KeyCode INSERT    = KeyCode.of(0x1B, 0x5B, 0x32, 0x7E);
    public static final KeyCode DELETE    = KeyCode.of(0x1B, 0x5B, 0x33, 0x7E);
    public static final KeyCode HOME      = KeyCode.of(0x1B, 0x5B, 0x48);
    public static final KeyCode END       = KeyCode.of(0x1B, 0x5B, 0x46);
    public static final KeyCode PAGE_UP   = KeyCode.of(0x1B, 0x5B, 0x35, 0x7E);
    public static final KeyCode PAGE_DOWN = KeyCode.of(0x1B, 0x5B, 0x36, 0x7E);

    public static final KeyCode ARROW_UP    = KeyCode.of(0x1B, 0x5B, 0x41);
    public static final KeyCode ARROW_LEFT  = KeyCode.of(0x1B, 0x5B, 0x44);
    public static final KeyCode ARROW_DOWN  = KeyCode.of(0x1B, 0x5B, 0x42);
    public static final KeyCode ARROW_RIGHT = KeyCode.of(0x1B, 0x5B, 0x43);

    public static final KeyCode TAB       = KeyCode.of(0x09);
    public static final KeyCode SHIFT_TAB = KeyCode.of(0x1B, 0x5B, 0x5A);

    public static final KeyCode CTRL_ARROW_UP    = KeyCode.of(0x1B, 0x5B, 0x31, 0x3B, 0x35, 0x41);
    public static final KeyCode CTRL_ARROW_LEFT  = KeyCode.of(0x1B, 0x5B, 0x31, 0x3B, 0x35, 0x44);
    public static final KeyCode CTRL_ARROW_DOWN  = KeyCode.of(0x1B, 0x5B, 0x31, 0x3B, 0x35, 0x42);
    public static final KeyCode CTRL_ARROW_RIGHT = KeyCode.of(0x1B, 0x5B, 0x31, 0x3B, 0x35, 0x43);

    public static final KeyCode ENTER         = KeyCode.of(0x0D);
    public static final KeyCode NUMPAD_DELETE = KeyCode.of(0x2E);

    private final byte[] keyCode;

    private KeyCode(byte[] keyCode) {
        this.keyCode = keyCode;
    }

    private static KeyCode of(int... code) {
        byte[] byteCode = new byte[code.length];

        for (int i=0; i<code.length; i++) {
            byteCode[i] = (byte) code[i];
        }

        return new KeyCode(byteCode);
    }

    public static KeyCode of(byte... keyCode) {
        return new KeyCode(keyCode);
    }

    public static KeyCode controlOf(char chr) {
        int code = (int) (Character.toLowerCase(chr)) - (int) 'a' + 1;

        if ((code > 0) && code <= 26) {
            return KeyCode.of(code);
        }

        throw new IllegalArgumentException(String.format("Unexpected character: %c. Use only a-z or A-Z.", chr));
    }

    public static KeyCode of(char chr) {
        return KeyCode.of((int) chr);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(keyCode);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof KeyCode) {
            return Arrays.equals(keyCode, ((KeyCode) o).keyCode) && this.hashCode() == o.hashCode();
        }

        return false;
    }

    @Override
    public String toString() {
        List<String> codeList = new ArrayList<>();

        for (byte code: keyCode) {
            codeList.add(String.format("0x%02X", code));
        }

        return String.format("KeyCode<[%s]>", String.join(", ", codeList));
    }

    public byte[] getCode() {
        return keyCode;
    }
}
