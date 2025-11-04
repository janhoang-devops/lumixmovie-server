package com.project.lumix.service;

import com.project.lumix.entity.Comment;
import com.project.lumix.entity.CommentReaction;
import com.project.lumix.entity.User;
import com.project.lumix.exception.AppException;
import com.project.lumix.exception.ErrorCode;
import com.project.lumix.repository.CommentReactionRepository;
import com.project.lumix.repository.CommentRepository;
import com.project.lumix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentReactionService {

    private final CommentReactionRepository commentReactionRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public void toggleReaction(String commentId, boolean isLike) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("🔹 User [{}] is toggling a {} reaction for comment [{}]", username, (isLike ? "LIKE" : "DISLIKE"), commentId);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.error("❌ User [{}] not found in database", username);
                        return new AppException(ErrorCode.USER_NOT_FOUND);
                    });

            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> {
                        log.error("❌ Comment [{}] not found", commentId);
                        return new AppException(ErrorCode.COMMENT_NOT_FOUND);
                    });

            Optional<CommentReaction> existing = commentReactionRepository.findByComment_IdAndUser_UserId(commentId, user.getUserId());
            if (existing.isPresent()) {
                CommentReaction reaction = existing.get();
                log.debug("🔍 Found existing reaction: [id={}, isLike={}]", reaction.getId(), reaction.isLike());

                if (reaction.isLike() == isLike) {
                    log.info("🗑 Removing existing reaction (same type) from user [{}] for comment [{}]", username, commentId);
                    commentReactionRepository.delete(reaction);
                } else {
                    reaction.setLike(isLike);
                    commentReactionRepository.save(reaction);
                    log.info("🔄 Updated reaction to [{}] for user [{}] on comment [{}]", isLike ? "LIKE" : "DISLIKE", username, commentId);
                }
            } else {
                CommentReaction newReaction = CommentReaction.builder()
                        .comment(comment)
                        .user(user)
                        .isLike(isLike)
                        .build();

                commentReactionRepository.save(newReaction);
                log.info("✅ Created new reaction [{}] for user [{}] on comment [{}]", isLike ? "LIKE" : "DISLIKE", username, commentId);
            }

        } catch (AppException ex) {
            log.error("⚠️ Application error during toggleReaction: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("💥 Unexpected error while toggling reaction: {}", ex.getMessage(), ex);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public long countLikes(String commentId) {
        try {
            long count = commentReactionRepository.countByComment_IdAndIsLikeTrue(commentId);
            log.debug("👍 Counted [{}] likes for comment [{}]", count, commentId);
            return count;
        } catch (Exception ex) {
            log.error("💥 Error counting likes for comment [{}]: {}", commentId, ex.getMessage(), ex);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public long countDisLikes(String commentId) {
        try {
            long count = commentReactionRepository.countByComment_IdAndIsLikeFalse(commentId);
            log.debug("👎 Counted [{}] dislikes for comment [{}]", count, commentId);
            return count;
        } catch (Exception ex) {
            log.error("💥 Error counting dislikes for comment [{}]: {}", commentId, ex.getMessage(), ex);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
