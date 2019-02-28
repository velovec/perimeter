package ru.v0rt3x.perimeter.server.themis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.flag.dao.Flag;
import ru.v0rt3x.perimeter.server.flag.dao.FlagResult;
import ru.v0rt3x.perimeter.server.service.dao.Service;
import ru.v0rt3x.perimeter.server.team.dao.Team;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ThemisClient {

    @Autowired
    private PerimeterProperties perimeterProperties;

    private final RestTemplate restClient = new RestTemplate();

    private String getEndpoint(String endpoint) {
        return String.format(
            "%s://%s:%d/api/%s",
            perimeterProperties.getThemis().getProtocol(),
            perimeterProperties.getThemis().getHost(),
            perimeterProperties.getThemis().getPort(),
            endpoint
        );
    }

    private <T> List<T> toList(T[] array) {
        return Objects.nonNull(array) ? Arrays.asList(array) : new ArrayList<>();
    }

    private <T> T getOrDefault(T value, T defaultValue) {
        return Objects.nonNull(value) ? value : defaultValue;
    }

    public List<Team> getTeamList() {
        try {
            return toList(restClient.getForObject(getEndpoint("teams"), Team[].class)).stream()
                .peek(team -> team.setIp(String.format(perimeterProperties.getTeam().getIpPattern(), team.getId())))
                .collect(Collectors.toList());
        } catch (ResourceAccessException e) {
            return new ArrayList<>();
        }
    }

    public List<Service> getServiceList() {
        try {
            return toList(restClient.getForObject(
                getEndpoint("services"), Service[].class
            ));
        } catch (ResourceAccessException e) {
            return new ArrayList<>();
        }
    }

    public ContestState getContestState() {
        try {
            return ContestState.values()[getOrDefault(restClient.getForObject(getEndpoint("contest/state"), Integer.class), ContestState.values().length - 1)];
        } catch (ResourceAccessException e) {
            return ContestState.NOT_AVAILABLE;
        }
    }

    public Integer getContestRound() {
        try {
            return getOrDefault(restClient.getForObject(getEndpoint("contest/round"), Integer.class), 0);
        } catch (ResourceAccessException e) {
            return 0;
        }
    }

    public List<FlagResult> sendFlags(List<Flag> flags) {
        List<Integer> results;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            HttpEntity<List<Flag>> entity = new HttpEntity<>(flags, headers);

            results = toList(restClient.postForObject(
                getEndpoint("submit"), entity, Integer[].class
            ));
        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == 400) {
                results = Collections.singletonList(Integer.valueOf(e.getResponseBodyAsString()));
            } else {
                throw new RuntimeException(String.format(
                    "Themis Error: %s\n%s", e.getRawStatusCode(), e.getResponseBodyAsString()
                ));
            }
        }

        return results.stream()
            .map(result -> FlagResult.values()[result])
            .collect(Collectors.toList());
    }
}
