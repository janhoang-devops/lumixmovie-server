package com.project.lumix.repository;

import com.project.lumix.entity.CommentReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentReactionRepository extends JpaRepository<CommentReaction,String> {
    Optional<CommentReaction> findByComment_IdAndUser_UserId(String commentId,String userId);
    long countByComment_IdAndIsLikeTrue(String commentId);
    long countByComment_IdAndIsLikeFalse(String commentId);

}
