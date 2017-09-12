package ru.v0rt3x.perimeter.server.web.views.agent;

import java.util.Map;

public class AgentTask {

    private AgentTaskType type;
    private Map<String, Object> parameters;
    private Map<String, Object> result;

    public AgentTaskType getType() {
        return type;
    }

    public void setType(AgentTaskType type) {
        this.type = type;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return String.format("AgentTask<%s>: %s", type, result);
    }
}