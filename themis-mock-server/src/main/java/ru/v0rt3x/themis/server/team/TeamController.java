package ru.v0rt3x.themis.server.team;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController("/api/teams")
public class TeamController {

    @Autowired
    private TeamManager teamManager;

    @RequestMapping(path = "/api/teams", method = GET)
    public List<Team> list() {
        return teamManager.getTeamList();
    }
}
