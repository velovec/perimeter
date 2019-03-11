package ru.v0rt3x.themis.server.identity;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ru.v0rt3x.themis.server.network.NetworkManager;
import ru.v0rt3x.themis.server.team.TeamManager;

import javax.servlet.http.HttpServletRequest;

@RestController("/api/identity")
public class IdentityController {

    @Autowired
    private NetworkManager networkManager;

    @Autowired
    private TeamManager teamManager;

    @RequestMapping(path = "/api/identity", method = RequestMethod.GET)
    public Identity identity(HttpServletRequest request) {
        String remoteAddress = request.getRemoteAddr();

        if (networkManager.isTeamNetwork(remoteAddress)) {
            return new Identity("team", teamManager.getTeamByNetwork(remoteAddress).getId());
        } else if (networkManager.isInternalNetwork(remoteAddress)) {
            return new Identity("internal");
        } else {
            return new Identity("external");
        }
    }
}
