package ru.v0rt3x.shell.console.ansi;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class ANSIUtils {

    private static final String CSI = new String(new byte[] { 0x1B, '[' });

    public static String CursorUp(int n) {
        return String.format("%s%d%s", CSI, n, "A");
    }

    public static String CursorDown(int n) {
        return String.format("%s%d%s", CSI, n, "B");
    }

    public static String CursorForward(int n) {
        return String.format("%s%d%s", CSI, n, "C");
    }

    public static String CursorBack(int n) {
        return String.format("%s%d%s", CSI, n, "D");
    }

    public static String CursorNextLine(int n) {
        return String.format("%s%d%s", CSI, n, "E");
    }

    public static String CursorPreviousLine(int n) {
        return String.format("%s%d%s", CSI, n, "F");
    }

    public static String CursorHorizontalAbsolute(int n) {
        return String.format("%s%d%s", CSI, n, "G");
    }

    public static String CursorPosition(int n, int m) {
        return String.format("%s%d;%d%s", CSI, n, m, "H");
    }

    public static String EraseData(int n) {
        return String.format("%s%d%s", CSI, n, "J");
    }

    public static String EraseInLine(int n) {
        return String.format("%s%d%s", CSI, n, "K");
    }

    public static String ScrollUp(int n) {
        return String.format("%s%d%s", CSI, n, "S");
    }

    public static String ScrollDown(int n) {
        return String.format("%s%d%s", CSI, n, "T");
    }

    public static String DECSet(int n) {
        return String.format("%s?%d%s", CSI, n, "h");
    }

    public static String DECReset(int n) {
        return String.format("%s?%d%s", CSI, n, "l");
    }

    public static String SelectGraphicRendition(Integer... args) {
        return String.format("%s%s%s", CSI, Arrays.stream(args).map(Objects::toString).collect(Collectors.joining(";")), "m");
    }

    public static String SelectGraphicRendition(ConsoleColor textColor, ConsoleColor bgColor, ConsoleTextStyle textStyle) {
        return SelectGraphicRendition(
                (textStyle != null) ? textStyle.ordinal() : 0,
                (bgColor != null) ? (bgColor.ordinal() % 8) + ((bgColor.ordinal() / 8 == 1) ? 100 : 40) : 49,
                (textColor != null) ? (textColor.ordinal() % 8) + ((textColor.ordinal() / 8 == 1) ? 90 : 30) : 30
        );
    }

    public static String ResetGraphicRendition() {
        return SelectGraphicRendition(0);
    }

    public static String RenderString(String string, ConsoleColor textColor, ConsoleColor bgColor, ConsoleTextStyle textStyle) {
        return String.format("%s%s%s", SelectGraphicRendition(textColor, bgColor, textStyle), string, ResetGraphicRendition());
    }

    public static String RenderString(String string, ConsoleColor textColor, ConsoleColor bgColor) {
        return RenderString(string, textColor, bgColor, null);
    }

    public static String RenderString(String string, ConsoleColor textColor, ConsoleTextStyle textStyle) {
        return RenderString(string, textColor, null, textStyle);
    }

    public static String RenderString(String string, ConsoleColor textColor) {
        return RenderString(string, textColor, null, null);
    }

    public static String RenderString(String string, ConsoleTextStyle textStyle) {
        return RenderString(string, null, null, textStyle);
    }
}