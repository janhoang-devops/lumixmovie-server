package com.project.lumix.repository;

import com.project.lumix.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, String>, JpaSpecificationExecutor<Movie> {
    boolean existsByTitle(String title);

    List<Movie> findByGenres_Name(String name);

    List<Movie> findByGenres_NameIn(List<String> names);

    Optional<Movie> findByTitle(String title);

    List<Movie> findTop20ByOrderByRatingDesc();

    List<Movie> findTop20ByOrderByCreatedAtDesc();
}

