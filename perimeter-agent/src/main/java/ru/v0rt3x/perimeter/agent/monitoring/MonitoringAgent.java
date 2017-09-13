package ru.v0rt3x.perimeter.agent.monitoring;

import org.pcap4j.packet.namednumber.IpNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import ru.v0rt3x.perimeter.agent.properties.PerimeterProperties;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class MonitoringAgent {

    @Autowired
    private PerimeterProperties perimeterProperties;

    private Map<Integer, Integer> services;
    private String server;

    private static final Logger logger = LoggerFactory.getLogger(MonitoringAgent.class);

    @Async
    public Future<Map<String, Object>> startMonitoring() {
        try {
            PcapHandle pCap = new PcapHandle.Builder(perimeterProperties.getMonitor().getGatewayInterface()).build();
            logger.info("Started monitoring on interface: {}", perimeterProperties.getMonitor().getGatewayInterface());

            while (pCap.isOpen()) {
                Packet packet = pCap.getNextPacket();

                if (packet.get(IpV4Packet.class).getHeader().getProtocol().compareTo(IpNumber.TCP) != 0)
                    continue;

                String srcHost = packet.get(IpV4Packet.class).getHeader().getSrcAddr().getHostAddress();
                String dstHost = packet.get(IpV4Packet.class).getHeader().getDstAddr().getHostAddress();

                Integer srcPort = packet.get(TcpPacket.class).getHeader().getSrcPort().valueAsInt();
                Integer dstPort = packet.get(TcpPacket.class).getHeader().getDstPort().valueAsInt();

                Long sequenceId = packet.get(TcpPacket.class).getHeader().getSequenceNumberAsLong();

                for (Integer servicePort: services.keySet()) {
                    boolean srcIsTarget = srcHost.equals(server) && srcPort.equals(servicePort);
                    boolean dstIsTarget = dstHost.equals(server) && dstPort.equals(servicePort);

                    if (srcIsTarget || dstIsTarget) {
                        if (srcIsTarget) {
                            logger.info(String.format(
                                "Service<%s:%d>: --{%s}-> DST[%s:%d]",
                                srcHost, srcPort, sequenceId, dstHost, dstPort
                            ));
                        } else {
                            logger.info(String.format(
                                "Service<%s:%d>: <-{%s}-- SRC[%s:%d]",
                                dstHost, dstPort, sequenceId, srcHost, srcPort
                            ));
                        }
                    }
                }
            }
        } catch (NotOpenException | PcapNativeException e) {
            logger.error("PCapError[{}]: {}", e.getClass().getSimpleName(), e.getMessage());
        }

        return new AsyncResult<>(new LinkedHashMap<>());
    }

    public void setMonitoringServices(List<Map<String, Object>> services, String server) {
        this.services = new HashMap<>();
        this.server = server;

        for (Map<String, Object> service: services) {
            if (service.containsKey("id") && service.containsKey("port"))
                this.services.put((Integer) service.get("port"), (Integer) service.get("id"));
        }
    }
}
