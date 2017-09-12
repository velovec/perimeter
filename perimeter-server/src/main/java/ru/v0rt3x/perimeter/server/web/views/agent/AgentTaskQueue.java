package ru.v0rt3x.perimeter.server.web.views.agent;

import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

@Component
public class AgentTaskQueue {

    private final Queue<AgentTask> taskQueue = new LinkedList<>();

    public boolean hasTasks() {
        return !taskQueue.isEmpty();
    }

    public boolean queueTask(AgentTask task) {
        return taskQueue.add(task);
    }

    public AgentTask getTask() {
        return taskQueue.poll();
    }

    public void clear() {
        taskQueue.clear();
    }
}
