package ru.v0rt3x.perimeter.server.web.views.team;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Team {

    @Id
    private int id;
    private String name;
    private boolean guest;
    private String ip;

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

    public boolean isGuest() {
        return guest;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    public void setIp(String ip) {
        this.ip = String.format(ip, id);
    }

    public String getIp() {
        return ip;
    }
}
