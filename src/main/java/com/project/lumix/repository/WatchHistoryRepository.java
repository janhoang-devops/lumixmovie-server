package com.project.lumix.repository;

import com.project.lumix.entity.User;
import com.project.lumix.entity.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, String> {
    Optional<WatchHistory> findByUserAndMovieId(User user, String movieId);

    List<WatchHistory> findByUserAndIsFinishedFalseOrderByLastWatchedAtDesc(User user);
}
