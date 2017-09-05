package ru.v0rt3x.perimeter.server.web.views.traffic;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.v0rt3x.perimeter.server.web.UIBaseView;
import ru.v0rt3x.perimeter.server.web.UIView;

import java.util.Map;

@Controller
@UIView(name = "traffic", linkOrder = 4, link = "/traffic/", title = "Traffic Analyzer", icon = "traffic")
public class TrafficView extends UIBaseView {

    @RequestMapping(value = "/traffic/", method = RequestMethod.GET)
    public String index(Map<String, Object> context) {
        return "traffic";
    }
}
