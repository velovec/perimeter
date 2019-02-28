package ru.v0rt3x.perimeter.server.flag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.v0rt3x.perimeter.server.flag.dao.Flag;
import ru.v0rt3x.perimeter.server.flag.dao.FlagPriority;
import ru.v0rt3x.perimeter.server.flag.dao.FlagRepository;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;

import javax.annotation.PostConstruct;
import java.util.*;

import static ru.v0rt3x.perimeter.server.flag.dao.FlagPriority.HIGH;
import static ru.v0rt3x.perimeter.server.flag.dao.FlagPriority.LOW;
import static ru.v0rt3x.perimeter.server.flag.dao.FlagPriority.NORMAL;
import static ru.v0rt3x.perimeter.server.flag.dao.FlagStatus.QUEUED;

@Component
public class FlagQueue {

    private final Map<FlagPriority, Queue<Flag>> flagQueue = new HashMap<>();
    private final Set<Flag> flagHistory = new HashSet<>();

    @Autowired
    private PerimeterProperties perimeterProperties;

    @Autowired
    private FlagRepository flagRepository;

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
                flagRepository.save(flag);

                flagQueue.get(flag.getPriority()).add(flag);
                flagStats.incrementByPriority(flag.getPriority());
            }

            return true;
        } else {
            return false;
        }
    }

    Flag pollFlag() {
        for (FlagPriority priority : new FlagPriority[] { HIGH, NORMAL, LOW }) {
            if (!flagQueue.get(priority).isEmpty()) {
                synchronized (flagQueue) {
                    return flagQueue.get(priority).poll();
                }
            }
        }

        return null;
    }

    void addToHistory(Flag flag) {
        flagHistory.add(flag);
    }
}
