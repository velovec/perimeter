package ru.v0rt3x.themis.server.team;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ru.v0rt3x.themis.server.network.Network;

public class Team {

    private Integer id;
    private String name;
    private boolean guest;
    private Network subnet;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    @JsonIgnore
    public Network getSubnet() {
        return subnet;
    }

    @JsonIgnore
    public void setSubnet(Network subnet) {
        this.subnet = subnet;
    }
}
