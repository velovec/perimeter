package ru.v0rt3x.perimeter.server.web.views;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.v0rt3x.perimeter.server.web.UIBaseView;
import ru.v0rt3x.perimeter.server.web.UIView;
import ru.v0rt3x.perimeter.server.web.types.Service;
import ru.v0rt3x.perimeter.server.web.types.repositories.ServiceRepository;

import java.util.Map;

@Controller
@UIView(name = "service", linkOrder = 6, link = "/service/", title = "Services", icon = "dns")
public class ServiceView extends UIBaseView {

    @Autowired
    private ServiceRepository serviceRepository;

    @MessageMapping("/service/add")
    @SendTo("/topic/perimeter")
    public Service addService(Service service) {
        return saveAndNotify(serviceRepository, service);
    }

    @RequestMapping(value = "/service/", method = RequestMethod.GET)
    public String index(Map<String, Object> context) {
        addNavButton(context,"add", "success", "data-toggle=\"modal\" data-target=\"#addService\"");
        addNavButton(context,"sync", "primary", "data-toggle=\"modal\" data-target=\"#syncServices\"");

        return "service";
    }
}
