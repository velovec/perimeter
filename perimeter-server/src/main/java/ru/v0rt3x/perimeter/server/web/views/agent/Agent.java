package ru.v0rt3x.perimeter.server.web.views.agent;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.util.UUID;

@Entity
public class Agent {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(columnDefinition = "varchar(36)")
    private String uuid;

    private String hostName;

    private String osName;
    private String osArch;
    private String osVersion;

    private AgentTaskType task;

    private Long lastSeen;
    private boolean isAvailable;

    private boolean isExecutor;
    private boolean isMonitor;

    public Agent() {
        uuid = UUID.randomUUID().toString();
    }

    public Integer getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsArch() {
        return osArch;
    }

    public void setOsArch(String osArch) {
        this.osArch = osArch;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public AgentTaskType getTask() {
        return task;
    }

    public void setTask(AgentTaskType task) {
        this.task = task;
    }

    public Long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public boolean isExecutor() {
        return isExecutor;
    }

    public void setExecutor(boolean executor) {
        isExecutor = executor;
    }

    public boolean isMonitor() {
        return isMonitor;
    }

    public void setMonitor(boolean monitor) {
        isMonitor = monitor;
    }
}
