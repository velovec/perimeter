package ru.v0rt3x.perimeter.server.web.views;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.v0rt3x.perimeter.server.web.UIBaseView;
import ru.v0rt3x.perimeter.server.web.UIView;

import java.util.Map;

@Controller
@UIView(name = "config", linkOrder = 8, link = "/config/", title = "Configuration", icon = "build")
public class ConfigView extends UIBaseView {

    @RequestMapping(value = "/config/", method = RequestMethod.GET)
    public String index(Map<String, Object> context) {
        return "config";
    }
}
