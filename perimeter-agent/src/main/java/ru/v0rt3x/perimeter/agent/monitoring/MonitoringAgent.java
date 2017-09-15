package ru.v0rt3x.perimeter.agent.monitoring;

import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.namednumber.EtherType;
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

import ru.v0rt3x.perimeter.agent.client.PerimeterClient;
import ru.v0rt3x.perimeter.agent.monitoring.tcp.TCPStream;
import ru.v0rt3x.perimeter.agent.properties.PerimeterProperties;
import ru.v0rt3x.perimeter.agent.types.AgentID;

import java.util.*;
import java.util.concurrent.Future;

@Component
public class MonitoringAgent {

    @Autowired
    private PerimeterProperties perimeterProperties;

    @Autowired
    private PerimeterClient perimeterClient;

    private AgentID agentID;

    private Map<Integer, Integer> services;
    private String server;

    private Map<TCPStream, List<Map<String, Object>>> tcpStreams = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(MonitoringAgent.class);

    @Async
    public Future<Map<String, Object>> startMonitoring() {
        try {
            PcapHandle pCap = new PcapHandle.Builder(perimeterProperties.getMonitor().getGatewayInterface()).build();
            logger.info("Started monitoring on interface: {}", perimeterProperties.getMonitor().getGatewayInterface());

            while (pCap.isOpen()) {
                Packet packet = pCap.getNextPacket();

                if (Objects.isNull(packet))
                    continue;

                if (!packet.get(EthernetPacket.class).getHeader().getType().equals(EtherType.IPV4))
                    continue;

                if (!packet.get(IpV4Packet.class).getHeader().getProtocol().equals(IpNumber.TCP))
                    continue;

                IpV4Packet.IpV4Header ipv4Header = packet.get(IpV4Packet.class).getHeader();
                TcpPacket.TcpHeader tcpHeader = packet.get(TcpPacket.class).getHeader();

                String srcHost = ipv4Header.getSrcAddr().getHostAddress();
                String dstHost = ipv4Header.getDstAddr().getHostAddress();

                Integer srcPort = tcpHeader.getSrcPort().valueAsInt();
                Integer dstPort = tcpHeader.getDstPort().valueAsInt();

                for (Integer servicePort: services.keySet()) {
                    boolean srcIsTarget = srcHost.equals(server) && srcPort.equals(servicePort);
                    boolean dstIsTarget = dstHost.equals(server) && dstPort.equals(servicePort);

                    if (srcIsTarget || dstIsTarget) {
                        processPacket(packet, dstIsTarget);
                    }
                }
            }
        } catch (NotOpenException | PcapNativeException e) {
            logger.error("PCapError[{}]: {}", e.getClass().getSimpleName(), e.getMessage());
        }

        return new AsyncResult<>(new LinkedHashMap<>());
    }

    private void processPacket(Packet packet, boolean inbound) {
        IpV4Packet.IpV4Header ipv4Header = packet.get(IpV4Packet.class).getHeader();

        TcpPacket tcpPacket = packet.get(TcpPacket.class);
        TcpPacket.TcpHeader tcpHeader = tcpPacket.getHeader();

        String serviceHost = (inbound ? ipv4Header.getDstAddr() : ipv4Header.getSrcAddr()).getHostAddress();
        String clientHost = (inbound ? ipv4Header.getSrcAddr() : ipv4Header.getDstAddr()).getHostAddress();

        Integer servicePort = (inbound ? tcpHeader.getDstPort() : tcpHeader.getSrcPort()).valueAsInt();
        Integer clientPort = (inbound ? tcpHeader.getSrcPort() : tcpHeader.getDstPort()).valueAsInt();

        TCPStream stream = new TCPStream(serviceHost, servicePort, clientHost, clientPort);

        if (tcpHeader.getSyn() && !tcpHeader.getAck()) {
            if (!tcpStreams.containsKey(stream)) {
                tcpStreams.put(stream, new ArrayList<>());

                logger.info(String.format(
                    "TCPStream<%s:%d <={%s}=> %s:%d>: transmission start",
                    serviceHost, servicePort, stream.hashCode(), clientHost, clientPort
                ));
            }
        }

        if (tcpStreams.containsKey(stream) && Objects.nonNull(tcpPacket.getPayload())) {
            tcpStreams.get(stream).add(
                getPacketDetails(inbound, tcpPacket.getPayload().getRawData())
            );
        }

        if (tcpHeader.getFin() || tcpHeader.getRst()) {
            if (tcpStreams.containsKey(stream)) {
                List<Map<String, Object>> transmission = tcpStreams.get(stream);

                logger.info(String.format(
                    "TCPStream<%s:%d <={%s:%d}=> %s:%d>: transmission end",
                    serviceHost, servicePort, stream.hashCode(), transmission.size(), clientHost, clientPort
                ));

                recordTransmission(stream.hashCode(), services.get(servicePort), clientHost, clientPort, transmission);
                tcpStreams.remove(stream);
            }
        }
    }

    private Map<String, Object> getPacketDetails(boolean inbound, byte[] data) {
        Map<String, Object> packetDetails = new LinkedHashMap<>();

        packetDetails.put("inbound", inbound);
        packetDetails.put("payload", Base64.getEncoder().encodeToString(data));
        packetDetails.put("time", System.currentTimeMillis());

        return packetDetails;
    }

    private void recordTransmission(Integer id, Integer serviceId, String clientHost, Integer clientPort, List<Map<String, Object>> transmission) {
        Map<String, Object> transmissionDetails = new LinkedHashMap<>();

        transmissionDetails.put("id", id);
        transmissionDetails.put("service", serviceId);
        transmissionDetails.put("clientHost", clientHost);
        transmissionDetails.put("clientPort", clientPort);
        transmissionDetails.put("transmission", transmission);

        perimeterClient.putTransmission(agentID, transmissionDetails);
    }

    public void setMonitoringServices(AgentID agentID, List<Map<String, Object>> services, String server) {
        this.agentID = agentID;
        this.services = new HashMap<>();
        this.server = server;

        for (Map<String, Object> service: services) {
            if (service.containsKey("id") && service.containsKey("port"))
                this.services.put((Integer) service.get("port"), (Integer) service.get("id"));
        }
    }
}
