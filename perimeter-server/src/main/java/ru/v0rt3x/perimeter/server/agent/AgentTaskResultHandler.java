package ru.v0rt3x.perimeter.server.agent;

import java.util.Map;

@FunctionalInterface
public interface AgentTaskResultHandler {

    void handleResult(String agentType, String taskType, Map<String, Object> parameters, Map<String, Object> result);
}
