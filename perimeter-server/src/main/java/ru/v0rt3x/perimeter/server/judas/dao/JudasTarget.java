package ru.v0rt3x.perimeter.server.judas.dao;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class JudasTarget {

    @Id
    @GeneratedValue
    private Long id;

    private String protocol;
    private String host;
    private int port;

    private String status = "DOWN";
    private int serversMax = 0;
    private int serversActive = 0;

    private String description;

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

    @JsonIgnore
    public Long getId() {
        return id;
    }

    @JsonIgnore
    public void setId(Long id) {
        this.id = id;
    }

    @JsonGetter("description")
    public String getDescription() {
        return description;
    }

    @JsonIgnore
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonIgnore
    public String getStatus() {
        return status;
    }

    @JsonIgnore
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonIgnore
    public void setServersMax(int serversMax) {
        this.serversMax = serversMax;
    }

    @JsonIgnore
    public int getServersMax() {
        return serversMax;
    }

    @JsonIgnore
    public void setServersActive(int serversActive) {
        this.serversActive = serversActive;
    }

    @JsonIgnore
    public int getServersActive() {
        return serversActive;
    }

    @JsonIgnore
    public String getStatusString() {
        return "UP".equals(status) ? String.format("%s (%d/%d)", status, serversActive, serversMax) : status;
    }
}
