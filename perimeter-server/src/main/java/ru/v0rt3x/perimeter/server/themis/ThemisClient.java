package ru.v0rt3x.perimeter.server.themis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import ru.v0rt3x.perimeter.server.flag.FlagInfo;
import ru.v0rt3x.perimeter.server.flag.dao.Flag;
import ru.v0rt3x.perimeter.server.flag.dao.FlagResult;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.team.dao.Team;
import ru.v0rt3x.perimeter.server.themis.dao.Identity;
import ru.v0rt3x.perimeter.server.utils.NetCalc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ThemisClient {

    /*
     * Source: https://github.com/themis-project/themis-finals-backend/blob/master/lib/server/application.rb
     */

    @Autowired
    private PerimeterProperties perimeterProperties;

    private final RestTemplate restClient = new RestTemplate();

    private String getEndpoint(String endpoint, Object... args) {
        return String.format(
            "%s://%s:%d/api/%s",
            perimeterProperties.getThemis().getProtocol(),
            perimeterProperties.getThemis().getHost(),
            perimeterProperties.getThemis().getPort(),
            String.format(endpoint, args)
        );
    }

    private <T> List<T> toList(T[] array) {
        return Objects.nonNull(array) ? Arrays.asList(array) : new ArrayList<>();
    }

    private <T> T getOrDefault(T value, T defaultValue) {
        return Objects.nonNull(value) ? value : defaultValue;
    }

    public Identity getIdentity() {
        try {
            return getOrDefault(restClient.getForObject(getEndpoint("identity"), Identity.class), Identity.unknown());
        } catch (ResourceAccessException e) {
            return Identity.unknown();
        }
    }

    public Integer getContestRound() {
        try {
            return getOrDefault(restClient.getForObject(getEndpoint("competition/round"), Integer.class), 0);
        } catch (ResourceAccessException e) {
            return 0;
        }
    }

    public ContestState getContestState() {
        try {
            return ContestState.values()[getOrDefault(
                restClient.getForObject(getEndpoint("competition/stage"), Integer.class), ContestState.values().length - 1
            )];
        } catch (ResourceAccessException e) {
            return ContestState.NOT_AVAILABLE;
        }
    }

    public List<Team> getTeamList() {
        try {
            return toList(restClient.getForObject(getEndpoint("teams"), Team[].class)).stream()
                .peek(team -> {
                    String teamSubnet = NetCalc.getSubnet(
                        perimeterProperties.getTeam().getBaseNetwork(),
                        perimeterProperties.getTeam().getSubnetCidr(),
                        team.getId()
                    );

                    team.setIp(NetCalc.getAddress(
                        teamSubnet, perimeterProperties.getTeam().getSubnetCidr(),
                        perimeterProperties.getTeam().getVulnboxAddress()
                    ));
                })
                .collect(Collectors.toList());
        } catch (ResourceAccessException e) {
            return new ArrayList<>();
        }
    }

    public String getPublicKey() {
        try {
            return restClient.getForObject(getEndpoint("capsule/v1/public_key"), String.class);
        } catch (ResourceAccessException e) {
            return null;
        }
    }

    public FlagResult submitFlag(Flag flag) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "text/plain");
            HttpEntity<String> entity = new HttpEntity<>(flag.getFlag(), headers);

            return FlagResult.values()[getOrDefault(
                restClient.postForObject(getEndpoint("flag/v1/submit"), entity, Integer.class), 1
            )];
        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == 400) {
                return FlagResult.values()[Integer.valueOf(e.getResponseBodyAsString())];
            } else {
                throw new RuntimeException(String.format(
                    "Themis Error: %s\n%s", e.getRawStatusCode(), e.getResponseBodyAsString()
                ));
            }
        }
    }

    public FlagInfo getFlagInfo(Flag flag) {
        try {
            return restClient.getForObject(getEndpoint("flag/v1/info/%s", flag.getFlag()), FlagInfo.class);
        } catch (HttpClientErrorException e) {
            if (e.getRawStatusCode() == 404) {
                return FlagInfo.notFound(flag);
            }
        } catch (ResourceAccessException e) {}

        return null;
    }
}
