package ru.v0rt3x.perimeter.server.judas.dao;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface JudasTargetRepository extends CrudRepository<JudasTarget, Long> {

    JudasTarget findByPort(int port);

    List<JudasTarget> findAll();
}
