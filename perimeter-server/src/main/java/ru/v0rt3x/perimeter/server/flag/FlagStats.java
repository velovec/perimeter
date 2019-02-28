package ru.v0rt3x.perimeter.server.flag;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.server.flag.dao.FlagPriority;
import ru.v0rt3x.perimeter.server.flag.dao.FlagStatus;
import ru.v0rt3x.perimeter.server.shell.console.Table;

@Component
public class FlagStats {

    private final Integer[] stats = new Integer[] { 0, 0, 0, 0, 0 };

    FlagStats() {
        Gauge.builder("perimeter_flag_queue", this, (o) -> o.getQueued()[0]).tag("queue", "queued").tag("priority", "high").register(Metrics.globalRegistry);
        Gauge.builder("perimeter_flag_queue", this, (o) -> o.getQueued()[1]).tag("queue", "queued").tag("priority", "normal").register(Metrics.globalRegistry);
        Gauge.builder("perimeter_flag_queue", this, (o) -> o.getQueued()[2]).tag("queue", "queued").tag("priority", "low").register(Metrics.globalRegistry);

        Gauge.builder("perimeter_flag_queue", this, FlagStats::getAccepted).tag("queue", "accepted").tag("priority", "n/a").register(Metrics.globalRegistry);
        Gauge.builder("perimeter_flag_queue", this, FlagStats::getRejected).tag("queue", "rejected").tag("priority", "n/a").register(Metrics.globalRegistry);
    }

    void setStats(int... stats) {
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
    }

    void decrementByStatus(FlagStatus status) {
        synchronized (stats) {
            stats[status.ordinal() + 2]--;
        }
    }

    void incrementByPriority(FlagPriority priority) {
        synchronized (stats) {
            stats[priority.ordinal()]++;
        }
    }

    void decrementByPriority(FlagPriority priority) {
        synchronized (stats) {
            stats[priority.ordinal()]--;
        }
    }

    public Table toTable() {
        Table statsTable = new Table("Queued (HIGH)", "Queued (NORMAL)", "Queued (LOW)", "Accepted", "Rejected");

        statsTable.addRow((Object[]) stats);

        return statsTable;
    }
}
