package ru.v0rt3x.perimeter.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.v0rt3x.perimeter.agent.client.PerimeterClient;
import ru.v0rt3x.perimeter.agent.executor.ExploitExecutor;
import ru.v0rt3x.perimeter.agent.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.agent.types.AgentID;
import ru.v0rt3x.perimeter.agent.types.AgentInfo;
import ru.v0rt3x.perimeter.agent.types.AgentTask;
import ru.v0rt3x.perimeter.agent.types.AgentTaskType;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
public class PerimeterAgent {

    @Autowired
    private PerimeterClient client;

    @Autowired
    private PerimeterProperties properties;

    @Autowired
    private ExploitExecutor exploitExecutor;

    private AgentID agentID;
    private AgentTask agentTask = AgentTask.noOp();

    private Future<Map<String, Object>> executionResult;

    private static final Logger logger = LoggerFactory.getLogger(PerimeterAgent.class);

    @PostConstruct
    private void registerAgent() {
        agentID = client.register(AgentInfo.build(properties.getAgent().getType()));

        if (Objects.isNull(agentID)) {
            logger.error("Unable to register");
            System.exit(1);
        }
    }

    @Scheduled(fixedRate = 5000L)
    private void getTask() {
        if (agentTask.getType() == AgentTaskType.NOOP) {
            agentTask = client.getTask(agentID);
        } else if (agentTask.getType() == AgentTaskType.MONITOR) {
            agentTask.setParameters(client.getTask(agentID).getParameters());
        } else {
            client.heartbeat(agentID);
        }
    }

    @Scheduled(fixedDelay = 3000L)
    private void executeTask() {
        switch (agentTask.getType()) {
            case NOOP:
                break;
            case MONITOR:
                break;
            case EXECUTE:
                executeExploit(agentTask.getParameters());
                break;
        }
    }

    private void executeExploit(Map<String, Object> parameters) {
        if (Objects.isNull(executionResult)) {
            executionResult = exploitExecutor.executeExploit(parameters);
            return;
        }

        if (executionResult.isDone()) {
            try {
                agentTask.setResult(executionResult.get());
            } catch (InterruptedException | ExecutionException e) {
                Map<String, Object> error = new LinkedHashMap<>();

                error.put("errType", e.getClass().getSimpleName());
                error.put("errMessage", e.getMessage());

                agentTask.setResult(error);
            } finally {
                client.reportTask(agentID, agentTask);
            }

            executionResult = null;
            agentTask = AgentTask.noOp();
        }
    }
}
