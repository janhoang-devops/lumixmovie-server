package com.project.lumix.repository;

import com.project.lumix.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ActorRepository extends JpaRepository<Actor, String> {
    Optional<Actor> findByName(String name);
    List<Actor> findByNameIn(Set<String> names);
}