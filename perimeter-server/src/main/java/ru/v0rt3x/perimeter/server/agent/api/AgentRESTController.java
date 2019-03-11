package ru.v0rt3x.perimeter.server.agent.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import ru.v0rt3x.perimeter.server.agent.dao.Agent;
import ru.v0rt3x.perimeter.server.agent.dao.AgentRepository;
import ru.v0rt3x.perimeter.server.agent.AgentTask;
import ru.v0rt3x.perimeter.server.agent.AgentTaskQueue;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.exploit.dao.Exploit;
import ru.v0rt3x.perimeter.server.exploit.dao.ExploitRepository;
import ru.v0rt3x.perimeter.server.flag.dao.Flag;
import ru.v0rt3x.perimeter.server.flag.FlagQueue;

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
    private PerimeterProperties perimeterProperties;

    @Autowired
    private AgentTaskQueue agentTaskQueue;

    @Autowired
    private FlagQueue flagQueue;

    private static final Logger logger = LoggerFactory.getLogger(AgentRESTController.class);

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    private Agent registerAgent(@RequestBody Agent agent) {
        agent.setLastSeen(System.currentTimeMillis());
        agent.setAvailable(true);

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

        if (agent != null) {
            agent.setLastSeen(System.currentTimeMillis());

            AgentTask task = agentTaskQueue.hasTasks(agent.getType()) ? agentTaskQueue.getTask(agent.getType()) : AgentTask.noop();

            agent.setTask(task.getType());
            agentRepository.save(agent);
            return task;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
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
        if (Objects.nonNull(result)) {
            if (result.containsKey("flags") && parameters.containsKey("exploit")) {
                Map<String, Object> exploitData = (Map<String, Object>) parameters.get("exploit");
                Exploit exploit = exploitRepository.findById((Integer) exploitData.get("id"));
                if (Objects.nonNull(exploit)) {
                    int hits = ((List<String>) result.get("flags")).parallelStream()
                        .filter(Objects::nonNull)
                        .map(flag -> Flag.newFlag(flag, exploit.getPriority()))
                        .map(flagQueue::enqueueFlag)
                        .mapToInt(x -> x ? 1 : 0)
                        .sum();

                    exploit.setHits(exploit.getHits() + hits);
                    exploitRepository.save(exploit);
                } else {
                    logger.warn("Got flags from unregistered exploit: {}", exploitData);
                }
            }
        }
    }

    @Scheduled(fixedRate = 5000L)
    private void checkAgentsStatus() {
        agentRepository.findAll().parallelStream().forEach(
            agent -> {
                boolean isAvailable = System.currentTimeMillis() - agent.getLastSeen() < perimeterProperties.getAgent().getTimeout();
                if (agent.isAvailable() != isAvailable) {
                    agent.setAvailable(isAvailable);
                    agentRepository.save(agent);
                }

                if (System.currentTimeMillis() - agent.getLastSeen() > perimeterProperties.getAgent().getDeleteAfter()) {
                    agentRepository.delete(agent);
                }
            }
        );
    }
}