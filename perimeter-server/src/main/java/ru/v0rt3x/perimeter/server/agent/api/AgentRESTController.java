package ru.v0rt3x.perimeter.server.agent.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ru.v0rt3x.perimeter.server.agent.AgentTask;
import ru.v0rt3x.perimeter.server.agent.AgentTaskQueue;
import ru.v0rt3x.perimeter.server.agent.dao.Agent;
import ru.v0rt3x.perimeter.server.agent.dao.AgentRepository;
import ru.v0rt3x.perimeter.server.event.EventManager;
import ru.v0rt3x.perimeter.server.event.dao.EventType;
import ru.v0rt3x.perimeter.server.exploit.ExploitManager;
import ru.v0rt3x.perimeter.server.exploit.dao.Exploit;
import ru.v0rt3x.perimeter.server.exploit.dao.ExploitExecutionResult;
import ru.v0rt3x.perimeter.server.exploit.dao.ExploitExecutionResultRepository;
import ru.v0rt3x.perimeter.server.exploit.dao.ExploitRepository;
import ru.v0rt3x.perimeter.server.flag.FlagProcessor;
import ru.v0rt3x.perimeter.server.flag.dao.Flag;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/agent")
public class AgentRESTController {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private ExploitRepository exploitRepository;

    @Autowired
    private ExploitManager exploitManager;

    @Autowired
    private ExploitExecutionResultRepository executionResultRepository;

    @Autowired
    private AgentTaskQueue agentTaskQueue;

    @Autowired
    private FlagProcessor flagProcessor;

    @Autowired
    private EventManager eventManager;

    private static final Logger logger = LoggerFactory.getLogger(AgentRESTController.class);
    private static final Object taskQueueLock = new Object();

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    private Agent registerAgent(@RequestBody Agent agent, HttpServletRequest request) {
        agent.setLastSeen(System.currentTimeMillis());
        agent.setAvailable(true);
        agent.setIp(request.getRemoteAddr());

        eventManager.createEvent("Agent '%s' registered (type: %s)", agent.getHostName(), agent.getType());

        return agentRepository.save(agent);
    }

    @RequestMapping(path = "/{uuid}/heartbeat", method = RequestMethod.GET)
    private Map<String, Object> heartBeat(@PathVariable String uuid) {
        Agent agent = agentRepository.findByUuid(uuid);

        if (agent != null) {
            agent.setLastSeen(System.currentTimeMillis());
            agentRepository.save(agent);
        }

        return new LinkedHashMap<>();
    }

    @RequestMapping(path = "/{uuid}/task", method = RequestMethod.GET)
    private AgentTask getTask(@PathVariable String uuid) {
        Agent agent = agentRepository.findByUuid(uuid);

        if (Objects.nonNull(agent)) {
            agent.setLastSeen(System.currentTimeMillis());

            synchronized (taskQueueLock) {
                AgentTask task = agentTaskQueue.hasTasks(agent.getType()) ? agentTaskQueue.getTask(agent.getType()) : AgentTask.noop();

                agent.setTask(task.getType());
                agentRepository.save(agent);

                return task;
            }
        }

        return null;
    }

    @RequestMapping(path = "/{uuid}/report", method = RequestMethod.POST)
    private Map<String, Object> reportTask(@PathVariable String uuid, @RequestBody AgentTask task) {
        Agent agent = agentRepository.findByUuid(uuid);

        if (agent != null) {
            agent.setLastSeen(System.currentTimeMillis());

            switch (task.getType()) {
                case "execute":
                    processExecutionReport(task.getParameters(), task.getResult());
                    break;
                case "noop":
                default:
                    break;
            }

            agent.setTask("noop");
            agentRepository.save(agent);
            return new LinkedHashMap<>();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private void processExecutionReport(Map<String, Object> parameters, Map<String, Object> result) {
        if (Objects.isNull(result))
            return;

        if (!result.containsKey("execution") || !parameters.containsKey("exploit"))
            return;

        Map<String, Object> exploitData = (Map<String, Object>) parameters.get("exploit");
        Exploit exploit = exploitRepository.findById((Integer) exploitData.get("id"));

        if (Objects.isNull(exploit))  {
            logger.warn("Got flags from unregistered exploit: {}", exploitData);
            return;
        }

        Map<String, Object> execution = (Map<String, Object>) result.get("execution");
        for (String team: execution.keySet()) {
            ExploitExecutionResult executionResult = executionResultRepository.findByExploitAndTeam(exploit, team);

            if (Objects.isNull(executionResult)) {
                executionResult = new ExploitExecutionResult();

                executionResult.setExploit(exploit);
                executionResult.setTeam(team);
                executionResult.setHits(0);
            }

            Map<String, Object> teamExecution = (Map<String, Object>) execution.get(team);

            int hits = ((List<String>) teamExecution.get("flags")).parallelStream()
                .filter(Objects::nonNull)
                .map(flag -> Flag.newFlag(flag, exploit.getPriority()))
                .map(flagProcessor::addFlag)
                .mapToInt(x -> x ? 1 : 0)
                .sum();

            ((List<Map<String, String>>) teamExecution.get("events"))
                .forEach(event -> {
                    String message = String.format("%s: %s", exploit.getName(), event.get("message"));
                    EventType level = EventType.valueOf(event.get("level"));

                    eventManager.createEvent(level, message);
                });

            executionResult.setHits(executionResult.getHits() + hits);
            executionResult.setExitCode((Integer) teamExecution.get("exitCode"));

            switch (executionResult.getExitCode()) {
                case 0:
                    break;
                case 68:
                    eventManager.createEvent(EventType.WARNING, "%s: Team '%s' is no longer vulnerable", exploit.getName(), team);
                    break;
                case 126:
                    eventManager.createEvent(EventType.WARNING, "%s: Internal error: command invoked cannot execute", exploit.getName());
                    break;
                case 127:
                    eventManager.createEvent(EventType.WARNING, "%s: Internal error: command not found", exploit.getName());
                    break;
                case 128:
                    eventManager.createEvent(EventType.WARNING, "%s: Internal error: invalid argument to exit", exploit.getName());
                    break;
                case 129:
                case 130:
                case 131:
                case 132:
                case 133:
                case 134:
                case 135:
                case 136:
                case 137:
                    eventManager.createEvent(EventType.WARNING, "%s: Exploit for '%s' was killed (signal: %d)", exploit.getName(), team, executionResult.getExitCode() - 128);
                    break;
                default:
                    eventManager.createEvent(EventType.WARNING, "%s: Failed to attack '%s' (exit code: %d)", exploit.getName(), team, executionResult.getExitCode());
                    break;
            }

            exploit.setHits(exploit.getHits() + hits);

            exploitManager.buildMetric(executionResult);
            executionResultRepository.save(executionResult);
        }

        exploit.setUpdated(false);
        exploitRepository.save(exploit);
    }

}
