package ru.v0rt3x.perimeter.server.web.views;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.v0rt3x.perimeter.server.web.UIBaseView;
import ru.v0rt3x.perimeter.server.web.UIView;

import java.util.Map;

@Controller
@UIView(name = "agent", linkOrder = 7, link = "/agent/", title = "Remote Agents", icon = "cast")
public class AgentView extends UIBaseView {

    @RequestMapping(value = "/agent/", method = RequestMethod.GET)
    public String index(Map<String, Object> context) {
        return "agent";
    }
}
