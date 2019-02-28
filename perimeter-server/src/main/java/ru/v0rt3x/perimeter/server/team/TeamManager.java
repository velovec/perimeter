package ru.v0rt3x.perimeter.server.team;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.team.dao.Team;
import ru.v0rt3x.perimeter.server.team.dao.TeamRepository;

import java.util.List;

@Component
public class TeamManager {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PerimeterProperties perimeterProperties;

    public List<Team> listTeams() {
        return teamRepository.findAll();
    }

    public void addTeam(int teamId, String name, boolean isGuest) {
        addTeam(teamId, name, String.format(perimeterProperties.getTeam().getIpPattern(), teamId), isGuest);
    }

    public void addTeam(int teamId, String name, String ip, boolean isGuest) {
        Team team = new Team();

        team.setId(teamId);
        team.setName(name);
        team.setGuest(isGuest);
        team.setIp(ip);

        teamRepository.save(team);
    }

    public Team getTeam(String name) {
        return teamRepository.findByName(name);
    }

    public void setActive(Team team, boolean isActive) {
        team.setActive(isActive);
        teamRepository.save(team);
    }
}
