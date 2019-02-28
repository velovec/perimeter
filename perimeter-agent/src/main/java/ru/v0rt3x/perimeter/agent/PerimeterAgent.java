package ru.v0rt3x.perimeter.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.agent.client.PerimeterClient;
import ru.v0rt3x.perimeter.agent.types.*;

import java.util.*;

@Component
public class PerimeterAgent {

    @Autowired
    private PerimeterClient client;

    private AgentID agentID;

    private static final Logger logger = LoggerFactory.getLogger(PerimeterAgent.class);

    @Scheduled(fixedRate = 5000L)
    private void heartbeat() {
        if (Objects.nonNull(agentID)) {
            client.heartbeat(agentID);
        }
    }

    public void registerAgent(String agentType) {
        agentID = client.register(AgentInfo.build(agentType));

        if (Objects.isNull(agentID)) {
            logger.error("Unable to register");
            System.exit(1);
        }
    }

    public AgentTask getTask() {
        return client.getTask(agentID);
    }

    public void reportData(String dataType, Map<String, Object> data) {
        client.reportData(agentID, dataType, data);
    }

    public void reportTask(AgentTask task, Map<String, Object> data) {
        Map<String, Object> report = new HashMap<>();

        report.put("type", task.getType());
        report.put("parameters", task.getParameters());
        report.put("result", data);

        reportData("report", report);
    }
}
