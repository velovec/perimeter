package ru.v0rt3x.perimeter.server.agent;

import ru.v0rt3x.perimeter.server.agent.dao.Agent;

public class AgentReportHandler {

    private final String agentType;
    private final String taskType;
    private final AgentTaskResultHandler resultHandler;

    public AgentReportHandler(String agentType, String taskType, AgentTaskResultHandler resultHandler) {
        this.agentType = agentType;
        this.taskType = taskType;
        this.resultHandler = resultHandler;
    }

    private boolean isEligibleToHandle(String agentType, String taskType) {
        return this.agentType.equals(agentType) && this.taskType.equals(taskType);
    }

    public boolean handleResult(Agent agent, AgentTask task) {
        if (isEligibleToHandle(agent.getType(), task.getType())) {
            this.resultHandler.handleResult(agentType, taskType, task.getParameters(), task.getResult());
            return true;
        }

        return false;
    }
}
