package ru.v0rt3x.perimeter.server.agent;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

@Component
public class AgentTaskQueue {

    private final Map<String, Queue<AgentTask>> taskQueue = new HashMap<>();

    private Queue<AgentTask> getQueue(String type) {
        taskQueue.putIfAbsent(type, new LinkedList<>());

        return taskQueue.get(type);
    }

    public boolean hasTasks(String type) {
        return !getQueue(type).isEmpty();
    }

    public boolean queueTask(AgentTask task) {
        return getQueue(task.getType()).add(task);
    }

    public AgentTask getTask(String type) {
        return getQueue(type).poll();
    }

    public void clear(String type) {
        getQueue(type).clear();
    }
}
