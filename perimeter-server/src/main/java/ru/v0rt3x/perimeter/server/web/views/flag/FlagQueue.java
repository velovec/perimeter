package ru.v0rt3x.perimeter.server.web.views.flag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.web.events.EventProducer;

import javax.annotation.PostConstruct;
import java.util.*;

import static ru.v0rt3x.perimeter.server.web.views.flag.FlagPriority.HIGH;
import static ru.v0rt3x.perimeter.server.web.views.flag.FlagPriority.LOW;
import static ru.v0rt3x.perimeter.server.web.views.flag.FlagPriority.NORMAL;
import static ru.v0rt3x.perimeter.server.web.views.flag.FlagStatus.QUEUED;

@Component
public class FlagQueue {

    private final Map<FlagPriority, Queue<Flag>> flagQueue = new HashMap<>();
    private final Set<Flag> flagHistory = new HashSet<>();

    @Autowired
    private PerimeterProperties perimeterProperties;

    @Autowired
    private FlagRepository flagRepository;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private FlagStats flagStats;

    @PostConstruct
    private void initQueue() {
        flagQueue.put(LOW, new LinkedList<>());
        flagQueue.put(NORMAL, new LinkedList<>());
        flagQueue.put(HIGH, new LinkedList<>());
    }

    public boolean enqueueFlag(Flag flag) {
        if (perimeterProperties.getFlag().getPattern().matcher(flag.getFlag()).matches()) {
            if (flag.getStatus() != QUEUED)
                return false;

            if (flagHistory.contains(flag))
                return false;

            synchronized (flagQueue) {
                eventProducer.saveAndNotify(flagRepository, flag);

                flagQueue.get(flag.getPriority()).add(flag);
                flagStats.incrementByPriority(flag.getPriority());
            }

            return true;
        } else {
            return false;
        }
    }

    public Flag pollFlag() {
        for (FlagPriority priority : new FlagPriority[] { HIGH, NORMAL, LOW }) {
            if (!flagQueue.get(priority).isEmpty()) {
                synchronized (flagQueue) {
                    return flagQueue.get(priority).poll();
                }
            }
        }

        return null;
    }

    public void addToHistory(Flag flag) {
        flagHistory.add(flag);
    }
}
