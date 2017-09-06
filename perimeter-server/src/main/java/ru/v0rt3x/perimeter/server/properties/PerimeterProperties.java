package ru.v0rt3x.perimeter.server.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "perimeter")
public class PerimeterProperties {

    private String teamIpPattern;

    private String teamIp;

    public String getTeamIpPattern() {
        return teamIpPattern;
    }

    public void setTeamIpPattern(String teamIPPattern) {
        this.teamIpPattern = teamIPPattern;
    }

    public String getTeamIp() {
        return teamIp;
    }

    public void setTeamIp(String teamIp) {
        this.teamIp = teamIp;
    }
}
