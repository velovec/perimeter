package ru.v0rt3x.perimeter.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import ru.v0rt3x.perimeter.agent.annotation.AgentTaskHandler;
import ru.v0rt3x.perimeter.agent.types.AgentTask;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public abstract class PerimeterAgentTaskHandler {

    @Autowired
    protected PerimeterAgent perimeterAgent;

    protected final String agentType;

    protected static final Logger logger = LoggerFactory.getLogger(PerimeterAgentTaskHandler.class);

    public PerimeterAgentTaskHandler(String agentType) {
        this.agentType = agentType;
    }

    @PostConstruct
    protected void registerAgent() {
        perimeterAgent.registerAgent(agentType);
    }

    @Scheduled(fixedDelay = 5000L)
    protected void pollTask() {
        AgentTask task = perimeterAgent.getTask();

        if (Objects.isNull(task))
            return;

        for (Method method: getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(AgentTaskHandler.class)) {
                AgentTaskHandler taskHandlerInfo = method.getAnnotation(AgentTaskHandler.class);

                if (taskHandlerInfo.taskType().equals(task.getType())) {
                    try {
                        method.setAccessible(true);
                        method.invoke(this, task);
                    } catch (IllegalAccessException e) {
                        // perimeterAgent.reportEvent("warning", String.format("Unable to handle task '%s'", task.getType()));
                        logger.warn("Unable to handle task '{}': ({}) {}", task.getType(), e.getClass().getSimpleName(), e.getMessage());
                    } catch (InvocationTargetException e) {
                        Throwable t = e.getCause();

                        logger.warn("Unable to handle task '{}': ({}) {}", task.getType(), t.getClass().getSimpleName(), t.getMessage());
                    }
                }
            }
        }
    }
}
