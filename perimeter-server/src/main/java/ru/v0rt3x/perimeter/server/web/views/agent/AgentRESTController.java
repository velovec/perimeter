package ru.v0rt3x.perimeter.server.web.views.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.web.events.EventProducer;
import ru.v0rt3x.perimeter.server.web.views.exploit.Exploit;
import ru.v0rt3x.perimeter.server.web.views.exploit.ExploitRepository;
import ru.v0rt3x.perimeter.server.web.views.flag.Flag;
import ru.v0rt3x.perimeter.server.web.views.flag.FlagQueue;

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
    private EventProducer eventProducer;

    @Autowired
    private PerimeterProperties perimeterProperties;

    @Autowired
    private AgentTaskQueue agentTaskQueue;

    @Autowired
    private FlagQueue flagQueue;

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    private Agent registerAgent(@RequestBody Agent agent) {
        agent.setLastSeen(System.currentTimeMillis());
        agent.setAvailable(true);

        return eventProducer.saveAndNotify(agentRepository, agent);
    }

    @RequestMapping(path = "/{uuid}/heartbeat", method = RequestMethod.GET)
    private Map<String, Object> heartBeat(@PathVariable String uuid) {
        Agent agent = agentRepository.findByUuid(uuid);

        if (agent != null) {
            agent.setLastSeen(System.currentTimeMillis());
            eventProducer.saveAndNotify(agentRepository, agent);
        }

        return new LinkedHashMap<>();
    }

    @RequestMapping(path = "/{uuid}/task", method = RequestMethod.GET)
    private AgentTask getTask(@PathVariable String uuid) {
        Agent agent = agentRepository.findByUuid(uuid);

        if (agent != null) {
            agent.setLastSeen(System.currentTimeMillis());

            AgentTask task = null;

            if (agent.isExecutor()) {
                if (agentTaskQueue.hasTasks()) {
                    task = agentTaskQueue.getTask();
                }
            } else if (agent.isMonitor()) {
                // TODO: Implement monitoring tasks
            }

            if (Objects.isNull(task)) {
                task = new AgentTask();
                task.setType(AgentTaskType.NOOP);
            }

            agent.setTask(task.getType());
            eventProducer.saveAndNotify(agentRepository, agent);
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
                case NOOP:
                    break;
                case EXECUTE:
                    processExecutionReport(task.getParameters(), task.getResult());
                    break;
                case MONITOR:
                    break;
            }

            agent.setTask(AgentTaskType.NOOP);
            eventProducer.saveAndNotify(agentRepository, agent);
            return new LinkedHashMap<>();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private void processExecutionReport(Map<String, Object> parameters, Map<String, Object> result) {
        if (Objects.nonNull(result) && result.containsKey("flags") && parameters.containsKey("exploit")) {
            int hits = ((List<String>) result.get("flags")).parallelStream()
                .map(Flag::fromString)
                .map(flagQueue::enqueueFlag)
                .mapToInt(x -> x ? 1 : 0)
                .sum();

            Map<String, Object> exploitData = (Map<String, Object>) parameters.get("exploit");
            Exploit exploit = exploitRepository.findById((Integer) exploitData.get("id"));
            if (Objects.nonNull(exploit)) {
                exploit.setHits(exploit.getHits() + hits);
                eventProducer.saveAndNotify(exploitRepository, exploit);
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
                    eventProducer.saveAndNotify(agentRepository, agent);
                }

                if (System.currentTimeMillis() - agent.getLastSeen() > perimeterProperties.getAgent().getDeleteAfter()) {
                    eventProducer.deleteAndNotify(agentRepository, agent);
                }
            }
        );
    }
}