package com.project.lumix.repository;

import com.project.lumix.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String>, JpaSpecificationExecutor<Comment> {
    List<Comment> findByMovieIdOrderByCreatedAtDesc(String movieId);
    List<Comment> findByMovieIdAndParentIsNullOrderByCreatedAtDesc(String movieId);
    List<Comment> findByMovieIdAndParentIsNull(String movieId);
    List<Comment> findByParentId(String parentId);
}
