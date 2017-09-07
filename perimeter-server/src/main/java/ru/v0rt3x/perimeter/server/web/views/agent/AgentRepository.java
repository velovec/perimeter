package ru.v0rt3x.perimeter.server.web.views.agent;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AgentRepository extends CrudRepository<Agent, Long> {

    List<Agent> findAll();

}
