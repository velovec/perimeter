package ru.v0rt3x.perimeter.server.judas;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class JudasTarget {

    private String protocol;
    private String host;
    private int port;

    @JsonGetter("protocol")
    public String getProtocol() {
        return protocol;
    }

    @JsonIgnore
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @JsonGetter("host")
    public String getHost() {
        return host;
    }

    @JsonIgnore
    public void setHost(String host) {
        this.host = host;
    }

    @JsonGetter("port")
    public int getPort() {
        return port;
    }

    @JsonIgnore
    public void setPort(int port) {
        this.port = port;
    }
}
