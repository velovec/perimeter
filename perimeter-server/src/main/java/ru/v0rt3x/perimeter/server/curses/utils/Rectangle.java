package ru.v0rt3x.perimeter.server.curses.utils;

public class Rectangle {

    private final int x;
    private final int y;
    private final int height;
    private final int width;

    private Rectangle(int x, int y, int height, int width) {
        this.x = x + 1;
        this.y = y + 1;
        this.height = height;
        this.width = width;
    }

    public static Rectangle newRect(int x, int y, int height, int width) {
        return new Rectangle(x, y, height, width);
    }

    public static Rectangle newRect(Rectangle parent, float top, float left, float height, float width) {
        return new Rectangle(
            Math.round(parent.getHeight() * top), Math.round(parent.getWidth() * left),
            Math.round(parent.getHeight() * height), Math.round(parent.getWidth() * width)
        );
    }

    public static Rectangle newRect(Rectangle parent, int x, int y, float height, float width) {
        return new Rectangle(
            x, y, Math.round(parent.getHeight() * height), Math.round(parent.getWidth() * width)
        );
    }

    public static Rectangle newLine(int x, int y, int width) {
        return new Rectangle(x, y, 1, width);
    }

    public static Rectangle newVLine(int x, int y, int height) {
        return new Rectangle(x, y, height, 1);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
