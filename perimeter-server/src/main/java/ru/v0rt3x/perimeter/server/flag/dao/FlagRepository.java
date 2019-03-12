package ru.v0rt3x.perimeter.server.flag.dao;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FlagRepository extends CrudRepository<Flag, Long> {

    List<Flag> findAllByStatusOrderByCreateTimeStampDesc(FlagStatus status);

    List<Flag> findAllByStatusAndPriorityOrderByCreateTimeStampDesc(FlagStatus status, FlagPriority priority);

    Flag findFirstByStatusAndPriorityOrderByCreateTimeStamp(FlagStatus status, FlagPriority priority);

    Integer countAllByStatus(FlagStatus status);

    Integer countAllByStatusAndPriority(FlagStatus status, FlagPriority priority);
}
