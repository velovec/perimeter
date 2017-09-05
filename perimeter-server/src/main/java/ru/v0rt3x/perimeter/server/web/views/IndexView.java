package ru.v0rt3x.perimeter.server.web.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import ru.v0rt3x.perimeter.server.themis.ContestState;
import ru.v0rt3x.perimeter.server.web.UIBaseView;
import ru.v0rt3x.perimeter.server.web.UIView;
import ru.v0rt3x.perimeter.server.web.views.flag.FlagStats;
import ru.v0rt3x.perimeter.server.web.views.flag.FlagView;
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

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Map<String, Object> context) {
        return "index";
    }
}
