package ru.v0rt3x.perimeter.server.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.themis.ThemisClient;
import ru.v0rt3x.perimeter.server.web.views.team.Team;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private ThemisClient themisClient;

    @Autowired
    private PerimeterProperties perimeterProperties;

    @RequestMapping(method = RequestMethod.GET)
    public List<Team> getTeams() {
        List<Team> teams = themisClient.getTeamList();

        teams.forEach(team -> team.setIp(perimeterProperties.getTeamIpPattern()));

        return teams;
    }
}
