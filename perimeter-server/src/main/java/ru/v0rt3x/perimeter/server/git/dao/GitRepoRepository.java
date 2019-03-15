package ru.v0rt3x.perimeter.server.git.dao;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GitRepoRepository extends CrudRepository<GitRepo, Long> {

    List<GitRepo> findAll();

    GitRepo findByName(String name);
}
