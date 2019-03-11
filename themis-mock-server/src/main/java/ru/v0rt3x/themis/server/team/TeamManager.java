package ru.v0rt3x.themis.server.team;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.v0rt3x.themis.server.network.Network;
import ru.v0rt3x.themis.server.network.NetworkManager;
import ru.v0rt3x.themis.server.properties.ThemisProperties;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class TeamManager {

    @Autowired
    private NetworkManager networkManager;

    @Autowired
    private ThemisProperties themisProperties;

    private List<Team> teamList;

    private static final Logger logger = LoggerFactory.getLogger(TeamManager.class);

    @PostConstruct
    public void setUpTeamManager() {
        this.teamList = themisProperties.getTeams();

        List<Network> networkList = networkManager.getTeamSubnets(this.teamList.size());

        for (int i = 0; i < this.teamList.size(); i++) {
            this.teamList.get(i).setSubnet(networkList.get(i));
            logger.info(
                "Registered team ID{} '{}' with subnet: {}",
                this.teamList.get(i).getId(), this.teamList.get(i).getName(),
                networkList.get(i)
            );
        }
    }

    public List<Team> getTeamList() {
        return teamList;
    }

    public Team getTeamByNetwork(String address) {
        for (Team team: teamList) {
            if (team.getSubnet().contains(address)) {
                return team;
            }
        }

        return null;
    }
}
