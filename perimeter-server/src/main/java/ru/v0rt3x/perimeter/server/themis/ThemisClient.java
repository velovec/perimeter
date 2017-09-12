package ru.v0rt3x.perimeter.server.themis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.web.views.flag.Flag;
import ru.v0rt3x.perimeter.server.web.views.flag.FlagResult;
import ru.v0rt3x.perimeter.server.web.views.service.Service;
import ru.v0rt3x.perimeter.server.web.views.team.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    public List<Team> getTeamList() {
        try {
            return Arrays.stream(restClient.getForObject(getEndpoint("teams"), Team[].class))
                .peek(team -> team.setIp(String.format(perimeterProperties.getTeam().getIpPattern(), team.getId())))
                .collect(Collectors.toList());
        } catch (ResourceAccessException e) {
            return new ArrayList<>();
        }
    }

    public List<Service> getServiceList() {
        try {
            return Arrays.asList(restClient.getForObject(
                getEndpoint("services"), Service[].class
            ));
        } catch (ResourceAccessException e) {
            return new ArrayList<>();
        }
    }

    public ContestState getContestState() {
        try {
            return ContestState.values()[restClient.getForObject(getEndpoint("contest/state"), Integer.class)];
        } catch (ResourceAccessException e) {
            return ContestState.NOT_AVAILABLE;
        }
    }

    public Integer getContestRound() {
        try {
            return restClient.getForObject(getEndpoint("contest/round"), Integer.class);
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

            results = Arrays.asList(restClient.postForObject(
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
