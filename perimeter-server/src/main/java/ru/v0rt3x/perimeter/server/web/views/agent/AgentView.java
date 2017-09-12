package ru.v0rt3x.perimeter.server.web.views.agent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.v0rt3x.perimeter.server.web.UIBaseView;
import ru.v0rt3x.perimeter.server.web.UIView;

import java.util.List;
import java.util.Map;

@Controller
@UIView(name = "agent", linkOrder = 7, link = "/agent/", title = "Remote Agents", icon = "cast")
public class AgentView extends UIBaseView {

    @Autowired
    private AgentRepository agentRepository;

    @ModelAttribute("AGENTS")
    public List<Agent> getAgents() {
        return agentRepository.findAll();
    }

    @MessageMapping("/agent/delete")
    private void deleteAgent(Agent agent) {
        eventProducer.deleteAndNotify(agentRepository, agent);
    }

    @RequestMapping(value = "/agent/", method = RequestMethod.GET)
    private String index(Map<String, Object> context) {
        return "agent";
    }
}
