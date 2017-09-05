package ru.v0rt3x.perimeter.server.web.views.flag;

import ru.v0rt3x.perimeter.server.web.events.EventEmitter;

public class FlagStats {

    private final int[] stats = new int[] { 0, 0, 0, 0, 0 };

    private final EventEmitter emitter;

    FlagStats(EventEmitter emitter, int... stats) {
        if (stats.length != 5)
            throw new IllegalArgumentException("FlagStats should have 5 integer parameters!");

        this.emitter = emitter;

        this.stats[0] = stats[0];
        this.stats[1] = stats[1];
        this.stats[2] = stats[2];
        this.stats[3] = stats[3];
        this.stats[4] = stats[4];
    }

    public Integer[] getQueued() {
        return new Integer[] { stats[0], stats[1], stats[2] };
    }

    public int getAccepted() {
        return stats[3];
    }

    public int getRejected() {
        return stats[4];
    }

    void incrementByStatus(FlagStatus status) {
        synchronized (stats) {
            stats[status.ordinal() + 2]++;
        }
        emitter.sendEvent("stats_flag", this);
    }

    void decrementByStatus(FlagStatus status) {
        synchronized (stats) {
            stats[status.ordinal() + 2]--;
        }
        emitter.sendEvent("stats_flag", this);
    }

    void incrementByPriority(FlagPriority priority) {
        synchronized (stats) {
            stats[priority.ordinal()]++;
        }
        emitter.sendEvent("stats_flag", this);
    }

    void decrementByPriority(FlagPriority priority) {
        synchronized (stats) {
            stats[priority.ordinal()]--;
        }
        emitter.sendEvent("stats_flag", this);
    }
}
