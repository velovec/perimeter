package ru.v0rt3x.perimeter.server.flag.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface FlagRepository extends CrudRepository<Flag, Long> {

    List<Flag> findAllByOrderByCreateTimeStampDesc();

    List<Flag> findAllByOrderByLastUpdateTimeStampDesc();

    Page<Flag> findAllByOrderByLastUpdateTimeStampDesc(Pageable pageable);

    Integer countAllByStatus(FlagStatus status);

    Integer countAllByStatusAndPriority(FlagStatus status, FlagPriority priority);
}
