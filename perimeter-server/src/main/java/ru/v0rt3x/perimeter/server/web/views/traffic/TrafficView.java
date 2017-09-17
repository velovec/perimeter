package ru.v0rt3x.perimeter.server.web.views.traffic;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import ru.v0rt3x.perimeter.server.web.UIBaseView;
import ru.v0rt3x.perimeter.server.web.UIView;
import ru.v0rt3x.perimeter.server.web.views.service.Service;
import ru.v0rt3x.perimeter.server.web.views.service.ServiceRepository;
import ru.v0rt3x.perimeter.server.web.views.traffic.tcp.TCPPacket;
import ru.v0rt3x.perimeter.server.web.views.traffic.tcp.TrafficRepository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Controller
@UIView(name = "traffic", linkOrder = 4, link = "/traffic/", title = "Traffic Analyzer", icon = "traffic")
public class TrafficView extends UIBaseView {

    @Autowired
    private TrafficRepository trafficRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @ModelAttribute("TRAFFIC")
    public List<TCPPacket> getTraffic() {
        return trafficRepository.findAll(new PageRequest(0, 100)).getContent();
    }

    @ModelAttribute("SERVICES")
    private List<Service> getServices() {
        return serviceRepository.findAll();
    }

    @MessageMapping("/packet/view")
    private void getPacketPayload(SimpMessageHeaderAccessor headers, TCPPacket packetRef) {
        TCPPacket packet = trafficRepository.findById(packetRef.getId());
        if (Objects.nonNull(packet)) {
            LinkedHashMap<String, Object> packetPayload = new LinkedHashMap<>();

            packetPayload.put("id", packet.getId());

            packetPayload.put("ascii", packet.getPayloadAsASCII());
            packetPayload.put("hex", packet.getPayloadAsHEX());

            eventProducer.notify(headers.getSessionId(), "view_tcppacket", packetPayload);
        }
    }

    @MessageMapping("/packet/search")
    private void searchPacket(SimpMessageHeaderAccessor headers, Map<String, Object> filters) {
        Map<String, Object> query = new LinkedHashMap<>();

        String direction = (String) filters.getOrDefault("direction", "both");
        String service = (String) filters.getOrDefault("service", "any");
        String client = (String) filters.getOrDefault("client", "");
        String transmission = (String) filters.getOrDefault("transmission", "");

        List<Specification<TCPPacket>> specifications = new ArrayList<>();

        if (!Objects.equals(direction, "both")) {
            specifications.add(TrafficSpecifications.isInbound(direction.equals("in")));
        }

        if (!Objects.equals(service, "any")) {
            specifications.add(TrafficSpecifications.service(Integer.parseInt(service)));
        }

        if (client.length() > 0) {
            specifications.add(TrafficSpecifications.client(client));
        }

        if (transmission.length() > 0) {
            specifications.add(TrafficSpecifications.transmission(Integer.parseInt(transmission)));
        }

        eventProducer.notify(
            headers.getSessionId(), "search_tcppacket",
            trafficRepository.findAll(TrafficSpecifications.and(specifications))
        );
    }

    @RequestMapping(value = "/traffic/", method = RequestMethod.GET)
    private String index(Map<String, Object> context) {
        addNavButton(context, "filter", "white", "data-toggle=\"modal\" data-target=\"#filterTraffic\"");

        return "traffic";
    }
}
