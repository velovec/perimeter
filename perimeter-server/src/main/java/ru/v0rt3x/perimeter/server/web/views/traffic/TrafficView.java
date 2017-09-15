package ru.v0rt3x.perimeter.server.web.views.traffic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
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
        return trafficRepository.findAll(new PageRequest(0, 10)).getContent();
    }

    @ModelAttribute("SERVICES")
    private List<Service> getServices() {
        return serviceRepository.findAll();
    }

    @MessageMapping("/packet/view")
    private void getPacketPayload(TCPPacket packetRef) {
        TCPPacket packet = trafficRepository.findById(packetRef.getId());
        if (Objects.nonNull(packet)) {
            LinkedHashMap<String, Object> packetPayload = new LinkedHashMap<>();

            packetPayload.put("id", packet.getId());

            packetPayload.put("ascii", packet.getPayloadAsASCII());
            packetPayload.put("hex", packet.getPayloadAsHEX());


            eventProducer.notify("view_tcppacket", packetPayload);
        }
    }

    @RequestMapping(value = "/traffic/", method = RequestMethod.GET)
    private String index(Map<String, Object> context) {
        addNavButton(context, "filter", "white", "data-toggle=\"modal\" data-target=\"#filterTraffic\"");

        return "traffic";
    }
}
