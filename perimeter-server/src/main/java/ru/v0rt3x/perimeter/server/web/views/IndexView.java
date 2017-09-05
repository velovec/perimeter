package ru.v0rt3x.perimeter.server.web.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.v0rt3x.perimeter.server.web.UIBaseView;
import ru.v0rt3x.perimeter.server.web.UIView;
import ru.v0rt3x.perimeter.server.web.views.flag.FlagStats;
import ru.v0rt3x.perimeter.server.web.views.flag.FlagView;

import java.util.Map;

@Controller
@UIView(name = "index", linkOrder = 1, link = "/", title = "Overview")
public class IndexView extends UIBaseView {

    @Autowired
    private FlagView flagView;

    @ModelAttribute("FLAG_STATS")
    private FlagStats flagStats() {
        return flagView.getFlagStats();
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Map<String, Object> context) {
        return "index";
    }
}
