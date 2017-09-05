package ru.v0rt3x.perimeter.server.web.views;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.v0rt3x.perimeter.server.web.UIBaseView;
import ru.v0rt3x.perimeter.server.web.UIView;

import java.util.Map;

@Controller
@UIView(name = "team", linkOrder = 5, link = "/team/", title = "Team List", icon = "supervisor_account")
public class TeamView extends UIBaseView {

    @RequestMapping(value = "/team/", method = RequestMethod.GET)
    public String index(Map<String, Object> context) {
        addNavButton(context,"add", "success", "data-toggle=\"modal\" data-target=\"#addTeam\"");
        addNavButton(context,"sync", "primary", "data-toggle=\"modal\" data-target=\"#syncTeams\"");

        return "team";
    }
}