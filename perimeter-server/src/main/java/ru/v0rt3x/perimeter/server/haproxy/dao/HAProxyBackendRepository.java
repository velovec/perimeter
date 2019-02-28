package ru.v0rt3x.perimeter.server.haproxy.dao;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HAProxyBackendRepository extends CrudRepository<HAProxyBackend, Long> {

    HAProxyBackend findByName(String name);

    List<HAProxyBackend> findAll();
}
