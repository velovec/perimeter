package ru.v0rt3x.perimeter.server.agent;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.server.agent.dao.Agent;
import ru.v0rt3x.perimeter.server.agent.dao.AgentRepository;
import ru.v0rt3x.perimeter.server.event.EventManager;
import ru.v0rt3x.perimeter.server.event.dao.EventType;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AgentManager {

    @Autowired
    private AgentTaskQueue taskQueue;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private PerimeterProperties perimeterProperties;

    @Autowired
    private EventManager eventManager;

    @PostConstruct
    public void setUpMetrics() {
        Gauge.builder("remote_agents", agentRepository, (r) -> r.countByAvailableAndTypeAndTask(true, "execute", "noop"))
            .tag("type", "execute")
            .tag("task", "noop")
            .register(Metrics.globalRegistry);

        Gauge.builder("remote_agents", agentRepository, (r) -> r.countByAvailableAndTypeAndTask(true, "execute", "execute"))
            .tag("type", "execute")
            .tag("task", "execute")
            .register(Metrics.globalRegistry);

        Gauge.builder("remote_agents", agentRepository, (r) -> r.countByAvailableAndTypeAndTask(true, "configure", "noop"))
            .tag("type", "configure")
            .tag("task", "noop")
            .register(Metrics.globalRegistry);

        Gauge.builder("remote_agents", agentRepository, (r) -> r.countByAvailableAndTypeAndTask(true, "configure", "configure"))
            .tag("type", "configure")
            .tag("task", "configure")
            .register(Metrics.globalRegistry);
    }

    public void clearTaskQueue(String type) {
        taskQueue.clear(type);
    }

    public void queueTask(String taskType, Map<String, Object> parameters) {
        AgentTask task = new AgentTask();

        task.setType(taskType);
        task.setParameters(parameters);

        queueTask(task);
    }

    public void queueTask(AgentTask executionTask) {
        taskQueue.queueTask(executionTask);
    }

    public List<Agent> listAgents() {
        return agentRepository.findAll();
    }

    public List<Agent> getAgentsByType(String type) {
        return agentRepository.findAllByType(type);
    }

    @Scheduled(fixedRate = 5000L)
    private void checkAgentsStatus() {
        agentRepository.findAll().parallelStream().forEach(
            agent -> {
                boolean isAvailable = System.currentTimeMillis() - agent.getLastSeen() < perimeterProperties.getAgent().getTimeout();
                if (agent.isAvailable() != isAvailable) {
                    agent.setAvailable(isAvailable);
                    agentRepository.save(agent);
                }

                if (System.currentTimeMillis() - agent.getLastSeen() > perimeterProperties.getAgent().getDeleteAfter()) {
                    eventManager.createEvent(EventType.WARNING, "Agent '%s' (type: %s) is marked for removal", agent.getHostName(), agent.getType());
                    agentRepository.delete(agent);
                }
            }
        );
    }
}
