package ru.v0rt3x.perimeter.agent.types;

import java.util.Map;

public class AgentTask {

    private String type;

    private Map<String, Object> parameters;

    public static AgentTask noOp() {
        AgentTask noOp = new AgentTask();
        noOp.setType("noop");
        return noOp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return String.format("AgentTask<%s>: %s", type, parameters);
    }

}
