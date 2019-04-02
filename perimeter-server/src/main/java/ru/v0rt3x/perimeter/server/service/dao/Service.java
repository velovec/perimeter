package ru.v0rt3x.perimeter.server.service.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Service {

    @Id
    @GeneratedValue
    private int id;
    private String name;
    private String mode;
    private int port;
    private String status = "DOWN";
    @Column(name = "httpcheck")
    private String check;
    private String expect;
    private int serversMax = 0;
    private int serversActive = 0;

    private String preCommitScript;
    private String deployScript;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("Service(name='%s',port=%d, mode='%s')", name, port, mode);
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public String getExpect() {
        return expect;
    }

    public void setExpect(String expect) {
        this.expect = expect;
    }

    public void setServersMax(int serversMax) {
        this.serversMax = serversMax;
    }

    public int getServersMax() {
        return serversMax;
    }

    public void setServersActive(int serversActive) {
        this.serversActive = serversActive;
    }

    public int getServersActive() {
        return serversActive;
    }

    public String getStatusString() {
        return "UP".equals(status) ? String.format("%s (%d/%d)", status, serversActive, serversMax) : status;
    }

    public String getPreCommitScript() {
        return preCommitScript;
    }

    public void setPreCommitScript(String preCommitScript) {
        this.preCommitScript = preCommitScript;
    }

    public String getDeployScript() {
        return deployScript;
    }

    public void setDeployScript(String deployScript) {
        this.deployScript = deployScript;
    }
}
