package ru.v0rt3x.perimeter.server.themis.dao;

public class Identity {

    private String name;
    private Integer id;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public static Identity unknown() {
        Identity identity = new Identity();

        identity.setName("unknown");

        return identity;
    }
}
