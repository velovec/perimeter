package ru.v0rt3x.perimeter.server.web.views.flag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.v0rt3x.perimeter.server.web.events.EventProducer;

@Component
public class FlagStats {

    private final int[] stats = new int[] { 0, 0, 0, 0, 0 };

    private EventProducer eventProducer;

    @Autowired
    FlagStats(EventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    public void setStats(int... stats) {
        if (stats.length != 5)
            throw new IllegalArgumentException("Invalid stats");

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

        eventProducer.notify("stats_flag", this);
    }

    void decrementByStatus(FlagStatus status) {
        synchronized (stats) {
            stats[status.ordinal() + 2]--;
        }

        eventProducer.notify("stats_flag", this);
    }

    void incrementByPriority(FlagPriority priority) {
        synchronized (stats) {
            stats[priority.ordinal()]++;
        }

        eventProducer.notify("stats_flag", this);
    }

    void decrementByPriority(FlagPriority priority) {
        synchronized (stats) {
            stats[priority.ordinal()]--;
        }

        eventProducer.notify("stats_flag", this);
    }
}
