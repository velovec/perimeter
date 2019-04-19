package ru.v0rt3x.perimeter.server.agent.dao;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AgentRepository extends CrudRepository<Agent, Long> {

    List<Agent> findAll();

    Agent findByUuid(String uuid);

    long countByAvailableAndTypeAndTask(boolean active, String type, String task);

    List<Agent> findAllByType(String type);
}
