package ru.v0rt3x.perimeter.judas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.v0rt3x.perimeter.agent.PerimeterAgent;
import ru.v0rt3x.perimeter.agent.PerimeterAgentTaskHandler;
import ru.v0rt3x.perimeter.agent.annotation.AgentTaskHandler;
import ru.v0rt3x.perimeter.agent.properties.PerimeterAgentProperties;
import ru.v0rt3x.perimeter.agent.types.AgentTask;
import ru.v0rt3x.perimeter.judas.properties.PerimeterJudasProperties;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class JudasExecutor extends PerimeterAgentTaskHandler {

    @Autowired
    private PerimeterJudasProperties judasProperties;

    @Autowired
    private PerimeterAgentProperties agentProperties;

    private static final Logger logger = LoggerFactory.getLogger(JudasExecutor.class);

    private final Map<JudasInstance, Process> judasInstances = new ConcurrentHashMap<>();

    public JudasExecutor() {
        super("judas");
    }

    @AgentTaskHandler(taskType = "setup")
    void setUpJudas(AgentTask task) throws InterruptedException, IOException {
        Map<String, Object> parameters = task.getParameters();

        Optional<JudasInstance> judasInstance = judasInstances.keySet().stream()
            .filter(instance -> instance.getPort().equals(parameters.get("port")))
            .findFirst();

        if (judasInstance.isPresent()) {
            JudasInstance runningInstance = judasInstance.get();

            if (Objects.isNull(parameters.get("target"))) {
                destroyJudasInstance(runningInstance);
                judasInstances.remove(runningInstance);
                logger.info("Stopping Judas instance '{}'", runningInstance.getPort());
            } else if (!runningInstance.getTarget().equals(parameters.get("target"))) {
                runningInstance.setTarget((String) parameters.get("target"));

                restartJudasInstance(runningInstance);
                logger.info("Target for Judas instance '{}' changed. Restarting...", runningInstance.getPort());
            }
        } else {
            JudasInstance newInstance = new JudasInstance((Integer) parameters.get("port"));
            newInstance.setTarget((String) parameters.get("target"));

            startJudasInstance(newInstance);
            logger.info("Starting Judas instance '{}'", newInstance.getPort());
        }
    }

    @Scheduled(fixedDelay = 5000L)
    void checkJudasInstances() throws IOException, InterruptedException {
        for (JudasInstance judasInstance: judasInstances.keySet()) {
            if (!isJudasRunning(judasInstance)) {
                restartJudasInstance(judasInstance);
                perimeterAgent.reportEvent("warning", String.format("Judas instance '%s' failed. Restarting...", judasInstance.getPort()));
                logger.warn("Judas instance '{}' failed. Restarting...", judasInstance.getPort());
            }
        }
    }

    private void startJudasInstance(JudasInstance judasInstance) throws IOException {
        Process judasProcess = new ProcessBuilder(
            judasProperties.getPath(),
            "--port", String.valueOf(judasInstance.getPort()),
            "--perimeter", String.format("%s://%s:%s", agentProperties.getProtocol(), agentProperties.getHost(), agentProperties.getPort())
        ).start();

        perimeterAgent.reportEvent("info", String.format("Judas instance '%s' started", judasInstance.getPort()));
        judasInstances.put(judasInstance, judasProcess);
    }

    private boolean isJudasRunning(JudasInstance judasInstance) {
        return judasInstances.get(judasInstance).isAlive();
    }

    private void destroyJudasInstance(JudasInstance judasInstance) throws InterruptedException {
        Process judasProcess = judasInstances.get(judasInstance);

        judasProcess.destroy();
        judasProcess.destroyForcibly();
        judasProcess.waitFor(20, TimeUnit.SECONDS);
    }

    private void restartJudasInstance(JudasInstance judasInstance) throws InterruptedException, IOException {
        destroyJudasInstance(judasInstance);
        startJudasInstance(judasInstance);
    }
}
