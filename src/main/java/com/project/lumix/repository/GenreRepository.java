package com.project.lumix.repository;

import com.project.lumix.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GenreRepository extends JpaRepository<Genre, String> {
    Optional<Genre> findByName(String name);
    List<Genre> findByNameIn(Set<String> names);
    boolean existsByName(String name);
}
