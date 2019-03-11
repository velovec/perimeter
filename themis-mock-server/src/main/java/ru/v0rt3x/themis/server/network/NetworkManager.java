package ru.v0rt3x.themis.server.network;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.v0rt3x.themis.server.properties.ThemisProperties;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class NetworkManager {

    @Autowired
    private ThemisProperties themisProperties;

    private Network internalNetwork;
    private Network teamNetwork;

    @PostConstruct
    public void setUpNetworkManager() {
        this.internalNetwork = Network.fromString(themisProperties.getNetwork().getInternal());
        this.teamNetwork = Network.fromString(themisProperties.getNetwork().getTeam());
    }

    public boolean isInternalNetwork(String address) {
        return internalNetwork.contains(address);
    }

    public boolean isTeamNetwork(String address) {
        return teamNetwork.contains(address);
    }

    public List<Network> getTeamSubnets(int count) {
        return teamNetwork.split(count, themisProperties.getNetwork().getTeamSubnetCidr());
    }
}
