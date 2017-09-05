package ru.v0rt3x.perimeter.server.web.events.types;

public class NotificationEvent {

    private String type;
    private String message;

    public NotificationEvent(String type, String message) {
        this.type = type;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}