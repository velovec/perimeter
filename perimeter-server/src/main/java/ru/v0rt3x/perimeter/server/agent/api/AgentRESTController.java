package ru.v0rt3x.perimeter.server.agent.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ru.v0rt3x.perimeter.server.agent.*;
import ru.v0rt3x.perimeter.server.agent.dao.Agent;
import ru.v0rt3x.perimeter.server.agent.dao.AgentRepository;
import ru.v0rt3x.perimeter.server.event.EventManager;
import ru.v0rt3x.perimeter.server.event.dao.EventType;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/agent")
public class AgentRESTController {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private AgentTaskQueue agentTaskQueue;

    @Autowired
    private EventManager eventManager;

    @Autowired
    private AgentManager agentManager;

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

            if (Objects.isNull(task.getResult()))
                return null;

            agentManager.handleReport(agent, task);

            agent.setTask("noop");
            agentRepository.save(agent);
            return new LinkedHashMap<>();
        }

        return null;
    }

    @RequestMapping(path = "/{uuid}/event", method = RequestMethod.POST)
    private Map<String, Object> event(@PathVariable String uuid, @RequestParam EventType type, @RequestParam String message) {
        Agent agent = agentRepository.findByUuid(uuid);

        if (agent != null) {
            agent.setLastSeen(System.currentTimeMillis());

            eventManager.createEvent(type, "%s: %s", agent.getHostName(), message);

            agentRepository.save(agent);
            return new LinkedHashMap<>();
        }

        return null;
    }
}
