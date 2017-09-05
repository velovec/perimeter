package ru.v0rt3x.perimeter.server.web.views.team;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import ru.v0rt3x.perimeter.server.themis.ThemisClient;
import ru.v0rt3x.perimeter.server.web.UIBaseView;
import ru.v0rt3x.perimeter.server.web.UIView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@UIView(name = "team", linkOrder = 5, link = "/team/", title = "Team List", icon = "supervisor_account")
public class TeamView extends UIBaseView {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ThemisClient themisClient;

    @ModelAttribute("TEAMS")
    public List<Team> getTeams() {
        return teamRepository.findAll();
    }

    @MessageMapping("/team/request_sync")
    private void syncRequest() {
        notify("sync_team", themisClient.getTeamList());
    }

    @MessageMapping("/team/confirm_sync")
    private void syncConfirm(Team[] teams) {
        List<Team> existingTeams = Arrays.stream(teams)
            .map(team -> teamRepository.findByName(team.getName()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        deleteAndNotify(teamRepository, existingTeams);
        saveAndNotify(teamRepository, Arrays.asList(teams));
    }

    @RequestMapping(value = "/team/", method = RequestMethod.GET)
    public String index(Map<String, Object> context) {
        addNavButton(context,"add", "success", "data-toggle=\"modal\" data-target=\"#addTeam\"");
        addNavButton(context,"sync", "primary", "data-toggle=\"modal\" data-target=\"#syncTeams\" onclick=\"requestTeamSync();\"");

        return "team";
    }
}