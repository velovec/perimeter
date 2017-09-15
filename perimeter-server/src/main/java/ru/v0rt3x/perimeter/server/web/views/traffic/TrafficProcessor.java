package ru.v0rt3x.perimeter.server.web.views.traffic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.v0rt3x.perimeter.server.web.events.EventProducer;
import ru.v0rt3x.perimeter.server.web.views.service.ServiceRepository;
import ru.v0rt3x.perimeter.server.web.views.traffic.tcp.TCPTransmission;
import ru.v0rt3x.perimeter.server.web.views.traffic.tcp.TrafficRepository;

@Component
public class TrafficProcessor {

    @Autowired
    private TrafficRepository trafficRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private EventProducer eventProducer;

    public void processTransmission(TCPTransmission transmission) {
        transmission.getTransmission().stream()
            .peek(tcpPacket -> tcpPacket.setTransmission(transmission.getId()))
            .peek(tcpPacket -> tcpPacket.setService(serviceRepository.findById(transmission.getService())))
            .peek(tcpPacket -> tcpPacket.setClientHost(transmission.getClientHost()))
            .peek(tcpPacket -> tcpPacket.setClientPort(transmission.getClientPort()))
            .forEach(tcpPacket -> eventProducer.saveAndNotify(trafficRepository, tcpPacket));
    }
}
