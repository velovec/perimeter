package ru.v0rt3x.themis.server.identity;

public class Identity {

    private String name;
    private Integer id;

    public Identity(String name) {
        this(name, null);
    }

    public Identity(String name, Integer id) {
        this.name = name;
        this.id = id;
    }

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
}
