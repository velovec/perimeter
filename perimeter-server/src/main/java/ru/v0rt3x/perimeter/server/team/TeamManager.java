package ru.v0rt3x.perimeter.server.team;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.v0rt3x.perimeter.server.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.server.team.dao.Team;
import ru.v0rt3x.perimeter.server.team.dao.TeamRepository;
import ru.v0rt3x.perimeter.server.utils.NetCalc;

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
        String teamSubnet = NetCalc.getSubnet(
            perimeterProperties.getTeam().getBaseNetwork(),
            perimeterProperties.getTeam().getSubnetCidr(),
            teamId
        );

        String ip = NetCalc.getAddress(
            teamSubnet, perimeterProperties.getTeam().getSubnetCidr(),
            perimeterProperties.getTeam().getVulnboxAddress()
        );

        addTeam(teamId, name, ip, isGuest);
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

    public void replaceTeams(List<Team> teamList) {
        teamRepository.deleteAll();
        teamRepository.saveAll(teamList);
    }
}
