package ru.v0rt3x.perimeter.server.service.dao;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ServiceRepository extends CrudRepository<Service, Long> {

    List<Service> findAll();

    Service findByName(String name);

    Service findById(Integer id);

    Service findByPort(int port);
}
