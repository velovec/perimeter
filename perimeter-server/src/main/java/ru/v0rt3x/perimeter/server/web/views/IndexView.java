package ru.v0rt3x.perimeter.server.web.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import ru.v0rt3x.perimeter.server.themis.ContestState;
import ru.v0rt3x.perimeter.server.web.UIBaseView;
import ru.v0rt3x.perimeter.server.web.UIView;
import ru.v0rt3x.perimeter.server.web.views.agent.Agent;
import ru.v0rt3x.perimeter.server.web.views.agent.AgentView;
import ru.v0rt3x.perimeter.server.web.views.exploit.Exploit;
import ru.v0rt3x.perimeter.server.web.views.exploit.ExploitView;
import ru.v0rt3x.perimeter.server.web.views.flag.FlagStats;
import ru.v0rt3x.perimeter.server.web.views.flag.FlagView;
import ru.v0rt3x.perimeter.server.web.views.service.Service;
import ru.v0rt3x.perimeter.server.web.views.service.ServiceView;
import ru.v0rt3x.perimeter.server.web.views.team.Team;
import ru.v0rt3x.perimeter.server.web.views.team.TeamView;

import java.util.List;
import java.util.Map;

@Controller
@UIView(name = "index", linkOrder = 1, link = "/", title = "Overview")
public class IndexView extends UIBaseView {

    @Autowired
    private FlagView flagView;

    @Autowired
    private TeamView teamView;

    @Autowired
    private ServiceView serviceView;

    @Autowired
    private ExploitView exploitView;

    @Autowired
    private AgentView agentView;

    @ModelAttribute("FLAG_STATS")
    private FlagStats getFlagStats() {
        return flagView.getFlagStats();
    }

    @ModelAttribute("CONTEST_STATE")
    private ContestState getContestState() {
        return flagView.getContestState();
    }

    @ModelAttribute("CONTEST_ROUND")
    private Integer getContestRound() {
        return flagView.getContestRound();
    }

    @ModelAttribute("TEAMS")
    private List<Team> getTeams() {
        return teamView.getTeams();
    }

    @ModelAttribute("SERVICES")
    private List<Service> getServices() {
        return serviceView.getServices();
    }

    @ModelAttribute("EXPLOITS")
    private List<Exploit> getExploits() {
        return exploitView.getExploits();
    }

    @ModelAttribute("AGENTS")
    private List<Agent> getAgents() {
        return agentView.getAgents();
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    private String index(Map<String, Object> context) {
        return "index";
    }
}
