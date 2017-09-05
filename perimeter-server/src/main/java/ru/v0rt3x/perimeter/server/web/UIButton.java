package ru.v0rt3x.perimeter.server.web;

public class UIButton {

    private final String icon;
    private final String color;
    private final String extraOptions;

    public UIButton(String icon, String color, String extraOptions) {
        this.icon = icon;
        this.color = color;
        this.extraOptions = extraOptions;
    }

    public String icon() {
        return icon;
    }

    public String color() {
        return color;
    }

    public String extraOptions() {
        return extraOptions;
    }
}
