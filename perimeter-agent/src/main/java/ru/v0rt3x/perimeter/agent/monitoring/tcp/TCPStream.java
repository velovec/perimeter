package ru.v0rt3x.perimeter.agent.monitoring.tcp;

import java.util.Objects;

public class TCPStream {

    private final String serviceHost;
    private final Integer servicePort;

    private final String clientHost;
    private final Integer clientPort;

    public TCPStream(String serviceHost, Integer servicePort, String clientHost, Integer clientPort) {
        this.serviceHost = serviceHost;
        this.servicePort = servicePort;

        this.clientHost = clientHost;
        this.clientPort = clientPort;
    }

    @Override
    public int hashCode() {
        int hashCode = serviceHost.hashCode() & 0xFF;

        hashCode <<= 8;
        hashCode |= servicePort.hashCode() & 0xFF;

        hashCode <<= 8;
        hashCode |= clientHost.hashCode() & 0xFF;

        hashCode <<= 8;
        hashCode |= clientPort.hashCode() & 0xFF;

        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        return !Objects.isNull(obj) &&
            obj instanceof TCPStream &&
            this.hashCode() == obj.hashCode() &&
            this.serviceHost.equals(((TCPStream) obj).serviceHost) &&
            this.servicePort.equals(((TCPStream) obj).servicePort) &&
            this.clientHost.equals(((TCPStream) obj).clientHost) &&
            this.clientPort.equals(((TCPStream) obj).clientPort);
    }
}
