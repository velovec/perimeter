package ru.v0rt3x.perimeter.server.web.events;

public class EventWrapper {

    private String type;
    private Object data;

    EventWrapper(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}
