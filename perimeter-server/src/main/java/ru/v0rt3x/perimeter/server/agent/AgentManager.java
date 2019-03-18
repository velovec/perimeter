package ru.v0rt3x.perimeter.server.agent;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.server.agent.dao.Agent;
import ru.v0rt3x.perimeter.server.agent.dao.AgentRepository;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class AgentManager {

    @Autowired
    private AgentTaskQueue taskQueue;

    @Autowired
    private AgentRepository agentRepository;

    @PostConstruct
    public void setUpMetrics() {
        Gauge.builder("remote_agents", agentRepository, AgentRepository::count)
            .tag("status", "registered")
            .register(Metrics.globalRegistry);

        Gauge.builder("remote_agents", agentRepository, (r) -> r.countByAvailable(true))
            .tag("status", "active")
            .register(Metrics.globalRegistry);
    }

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
