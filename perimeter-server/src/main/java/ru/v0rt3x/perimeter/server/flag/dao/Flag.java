package ru.v0rt3x.perimeter.server.flag.dao;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Flag {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    private String flag;
    private int service;
    private FlagStatus status;
    private FlagPriority priority;

    private long createTimeStamp;
    private long lastUpdateTimeStamp;

    public Integer getId() {
        return id;
    }

    public String getFlag() {
        return flag;
    }

    public FlagStatus getStatus() {
        return status;
    }

    public void setStatus(FlagStatus status) {
        this.status = status;

        setLastUpdateTimeStamp(System.currentTimeMillis());
    }

    public void setStatusByResult(FlagResult result) {
        switch (result) {
            case FLAG_ACCEPTED:
                status = FlagStatus.ACCEPTED;
                break;
            case FLAG_EXPIRED:
            case FLAG_BELONGS_ATTACKER:
            case FLAG_ALREADY_ACCEPTED:
            case FLAG_NOT_FOUND:
                status = FlagStatus.REJECTED;
                break;
            default:
                status = FlagStatus.QUEUED;
                break;
        }

        setLastUpdateTimeStamp(System.currentTimeMillis());
    }

    public FlagPriority getPriority() {
        return priority;
    }

    public void setPriority(FlagPriority priority) {
        this.priority = priority;

        setLastUpdateTimeStamp(System.currentTimeMillis());
    }

    public void setCreateTimeStamp(long createTimeStamp) {
        this.createTimeStamp = createTimeStamp;

        setLastUpdateTimeStamp(System.currentTimeMillis());
    }

    public long getCreateTimeStamp() {
        return createTimeStamp;
    }

    private void setLastUpdateTimeStamp(long lastUpdateTimeStamp) {
        this.lastUpdateTimeStamp = lastUpdateTimeStamp;
    }

    @Override
    public int hashCode() {
        return flag.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Flag && flag.equals(((Flag) obj).flag);
    }

    @Override
    public String toString() {
        return String.format("Flag(flag='%s', service=%d)", flag, service);
    }

    public static Flag newFlag(String flagString, FlagPriority priority) {
        Flag flag = new Flag();

        flag.setFlag(flagString);
        flag.setPriority(priority);
        flag.setCreateTimeStamp(System.currentTimeMillis());
        flag.setStatus(FlagStatus.QUEUED);

        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}
