package ru.v0rt3x.perimeter.server.web.views.agent;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Agent {

    @Id
    @GeneratedValue
    private Integer id;

    public Integer getId() {
        return id;
    }
}
