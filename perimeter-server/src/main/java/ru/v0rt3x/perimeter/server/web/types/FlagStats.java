package ru.v0rt3x.perimeter.server.web.types;

public class FlagStats {

    private Integer[] queued;
    private Integer accepted;
    private Integer rejected;

    public FlagStats(Integer[] queued, Integer accepted, Integer rejected) {
        this.queued = queued;
        this.accepted = accepted;
        this.rejected = rejected;
    }

    public Integer[] getQueued() {
        return queued;
    }

    public Integer getAccepted() {
        return accepted;
    }

    public Integer getRejected() {
        return rejected;
    }
}
