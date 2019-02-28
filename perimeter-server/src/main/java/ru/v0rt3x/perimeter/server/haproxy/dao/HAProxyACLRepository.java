package ru.v0rt3x.perimeter.server.haproxy.dao;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HAProxyACLRepository extends CrudRepository<HAProxyACL, Long> {

    HAProxyACL findByName(String name);

    List<HAProxyACL> findAll();
}
