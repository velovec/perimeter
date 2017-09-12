package ru.v0rt3x.perimeter.agent.types;

import java.util.Map;

public class AgentTask {

    private AgentTaskType type;

    private Map<String, Object> parameters;
    private Map<String, Object> result;

    public static AgentTask noOp() {
        AgentTask noOpTask = new AgentTask();
        noOpTask.setType(AgentTaskType.NOOP);

        return noOpTask;
    }

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
}
