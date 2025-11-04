package com.project.lumix.repository;

import com.project.lumix.entity.Director;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface DirectorRepository extends JpaRepository<Director, String> {
    Optional<Director> findByName(String name);
    List<Director> findByNameIn(Set<String> names);
}

