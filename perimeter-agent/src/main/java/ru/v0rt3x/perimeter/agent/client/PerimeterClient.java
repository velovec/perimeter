package ru.v0rt3x.perimeter.agent.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import ru.v0rt3x.perimeter.agent.properties.PerimeterAgentProperties;
import ru.v0rt3x.perimeter.agent.types.AgentID;
import ru.v0rt3x.perimeter.agent.types.AgentInfo;
import ru.v0rt3x.perimeter.agent.types.AgentTask;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PerimeterClient {

    @Autowired
    private PerimeterAgentProperties perimeterAgentProperties;

    private final RestTemplate restClient = new RestTemplate();

    private String getEndpoint(String endpoint, String... args) {
        return String.format(
            "%s://%s:%s/api/%s",
            perimeterAgentProperties.getProtocol(),
            perimeterAgentProperties.getHost(),
            perimeterAgentProperties.getPort(),
            String.format(endpoint, (Object[]) args)
        );
    }

    public AgentID register(AgentInfo agentInfo) {
        try {
            return restClient.postForObject(getEndpoint("agent/register"), agentInfo, AgentID.class);
        } catch (ResourceAccessException e) {
            System.out.println(e.getMessage());

            return null;
        }
    }

    public AgentTask getTask(AgentID agentID) {
        try {
            return restClient.getForObject(getEndpoint("agent/%s/task", agentID.getUuid()), AgentTask.class);
        } catch (ResourceAccessException e) {
            return AgentTask.noOp();
        }
    }

    public void heartbeat(AgentID agentID) {
        try {
            restClient.getForObject(getEndpoint("agent/%s/heartbeat", agentID.getUuid()), LinkedHashMap.class);
        } catch (ResourceAccessException e) {
            // Do Nothing
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> reportData(AgentID agentID, String dataType, Map<String, Object> data) {
        try {
            return restClient.postForObject(getEndpoint("agent/%s/%s", agentID.getUuid(), dataType), data, LinkedHashMap.class);
        } catch (ResourceAccessException e) {
            return new LinkedHashMap<>();
        }
    }
}
