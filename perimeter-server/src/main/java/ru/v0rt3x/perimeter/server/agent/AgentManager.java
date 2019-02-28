package ru.v0rt3x.perimeter.server.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.v0rt3x.perimeter.server.agent.dao.Agent;
import ru.v0rt3x.perimeter.server.agent.dao.AgentRepository;

import java.util.List;

@Component
public class AgentManager {

    @Autowired
    private AgentTaskQueue taskQueue;

    @Autowired
    private AgentRepository agentRepository;

    public void clearTaskQueue(String type) {
        taskQueue.clear(type);
    }

    public void queueTask(AgentTask executionTask) {
        taskQueue.queueTask(executionTask);
    }

    public List<Agent> listAgents() {
        return agentRepository.findAll();
    }
}
