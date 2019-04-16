package ru.v0rt3x.shell.curses.window;

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

    public static Rectangle newRect(Rectangle parent, float top, float left, int height, int width) {
        int x = Math.round(parent.getHeight() * top);
        int y = Math.round(parent.getWidth() * left);

        return new Rectangle(
            x - height / 2, y - width / 2, height, width
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

    public boolean isInside(int x, int y) {
        int relativeX = x - this.x;
        int relativeY = y - this.y;

        return (relativeX >= 0) && (relativeX <= height) && (relativeY >= 0) && (relativeY <= width);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + x;
        hash = 31 * hash + y;
        hash = 31 * hash + height;
        hash = 31 * hash + width;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;

        Rectangle rect = (Rectangle) o;

        return (x == rect.x) && (y == rect.y) &&
            (height == rect.height) && (width == rect.width);
    }
}
