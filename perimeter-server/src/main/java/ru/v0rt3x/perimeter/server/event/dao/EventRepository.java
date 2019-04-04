package ru.v0rt3x.perimeter.server.event.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EventRepository extends CrudRepository<Event, Long> {

    List<Event> findAllByCreatedGreaterThanOrderByCreatedDesc(long createdAfter, Pageable page);
}
