package ru.v0rt3x.perimeter.server.web.views.traffic.tcp;

import java.util.List;

public class TCPTransmission {

    private Integer id;

    private Integer service;

    private String clientHost;
    private Integer clientPort;

    private List<TCPPacket> transmission;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<TCPPacket> getTransmission() {
        return transmission;
    }

    public void setTransmission(List<TCPPacket> transmission) {
        this.transmission = transmission;
    }

    public String getClientHost() {
        return clientHost;
    }

    public void setClientHost(String clientHost) {
        this.clientHost = clientHost;
    }

    public Integer getClientPort() {
        return clientPort;
    }

    public void setClientPort(Integer clientPort) {
        this.clientPort = clientPort;
    }

    @Override
    public String toString() {
        long inbound = transmission.stream().filter(TCPPacket::isInbound).count();
        long outbound = transmission.size() - inbound;

        return String.format("TCPTransmission<%d>: %d/%d", id, inbound, outbound);
    }

    public Integer getService() {
        return service;
    }

    public void setService(Integer service) {
        this.service = service;
    }
}
