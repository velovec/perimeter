package ru.v0rt3x.perimeter.server.web.views.team;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TeamRepository extends CrudRepository<Team, Long> {

    List<Team> findAll();

    Team findByName(String name);

    Team findById(int id);

    List<Team> findAllByActive(boolean isActive);
}
