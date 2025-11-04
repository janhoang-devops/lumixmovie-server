package com.project.lumix.service;

import com.project.lumix.dto.request.CommentRequest;
import com.project.lumix.dto.request.CommentSearchRequest;
import com.project.lumix.dto.request.DeleteCommentRequest;
import com.project.lumix.dto.response.CommentResponse;
import com.project.lumix.entity.Comment;
import com.project.lumix.entity.CommentReaction;
import com.project.lumix.entity.Movie;
import com.project.lumix.entity.User;
import com.project.lumix.exception.AppException;
import com.project.lumix.exception.ErrorCode;
import com.project.lumix.mapper.CommentMapper;
import com.project.lumix.repository.CommentReactionRepository;
import com.project.lumix.repository.CommentRepository;
import com.project.lumix.repository.MovieRepository;
import com.project.lumix.repository.UserRepository;
import com.project.lumix.specification.CommentSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final CommentReactionRepository commentReactionRepository;
    private final SimpMessagingTemplate messageTemplate;

    public List<CommentResponse> getAllComment() {
        return commentRepository.findAll()
                .stream()
                .map(commentMapper::toCommentResponse)
                .toList();
    }

    public List<CommentResponse> getCommentsForMovie(String movieId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        final String userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND))
                .getUserId();

        List<Comment> comments = commentRepository.findByMovieIdAndParentIsNullOrderByCreatedAtDesc(movieId);

        return comments.stream()
                .map(comment -> buildCommentResponseWithReactions(comment,userId))
                .toList();
    }

    private CommentResponse buildCommentResponseWithReactions(Comment comment, String userId) {
        long likeCount = commentReactionRepository.countByComment_IdAndIsLikeTrue(comment.getId());
        long dislikeCount = commentReactionRepository.countByComment_IdAndIsLikeFalse(comment.getId());

        String userReaction = "NONE";
        if (userId != null) {
            Optional<CommentReaction> reactionOpt =
                    commentReactionRepository.findByComment_IdAndUser_UserId(comment.getId(), userId);
            if (reactionOpt.isPresent()) {
                userReaction = reactionOpt.get().isLike() ? "LIKE" : "DISLIKE";
            }
        }

        CommentResponse response = commentMapper.toCommentResponse(comment);
        response.setLikeCount(likeCount);
        response.setDislikeCount(dislikeCount);
        response.setUserReaction(userReaction);

        // ✅ Lấy tất cả replies (mọi cấp), rồi gộp thành danh sách cấp 1
        List<Comment> allReplies = getAllReplies(comment.getId());

        List<CommentResponse> replyResponses = allReplies.stream()
                .map(child -> {
                    CommentResponse childResponse = commentMapper.toCommentResponse(child);
                    long childLikeCount = commentReactionRepository.countByComment_IdAndIsLikeTrue(child.getId());
                    long childDislikeCount = commentReactionRepository.countByComment_IdAndIsLikeFalse(child.getId());

                    String childUserReaction = "NONE";
                    if (userId != null) {
                        Optional<CommentReaction> childReactionOpt =
                                commentReactionRepository.findByComment_IdAndUser_UserId(child.getId(), userId);
                        if (childReactionOpt.isPresent()) {
                            childUserReaction = childReactionOpt.get().isLike() ? "LIKE" : "DISLIKE";
                        }
                    }

                    childResponse.setLikeCount(childLikeCount);
                    childResponse.setDislikeCount(childDislikeCount);
                    childResponse.setUserReaction(childUserReaction);
                    childResponse.setReplies(Collections.emptyList());
                    return childResponse;
                })
                .toList();

        response.setReplies(replyResponses);
        return response;
    }

    /**
     * 🔁 Lấy tất cả replies của một commentId (bao gồm nhiều cấp)
     */
    private List<Comment> getAllReplies(String parentId) {
        List<Comment> directReplies = commentRepository.findByParentId(parentId);
        List<Comment> allReplies = new ArrayList<>(directReplies);

        for (Comment reply : directReplies) {
            allReplies.addAll(getAllReplies(reply.getId())); // đệ quy thu thập
        }

        return allReplies;
    }


    public CommentResponse addCommentToMovie(String movieId, CommentRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_EXISTED));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setMovie(movie);
        comment.setUser(user);
        if(request.getParentId() != null && !request.getParentId().isBlank()){
            log.info("This is a reply to comment '{}'", request.getParentId());
            Comment parent = commentRepository.findById(request.getParentId()).orElseThrow(()->new AppException(ErrorCode.COMMENT_NOT_FOUND));
            comment.setParent(parent);
        }
        else{
            log.info("This is a root comment");
        }

        Comment saved = commentRepository.save(comment);
        CommentResponse response = commentMapper.toCommentResponse(saved);
        messageTemplate.convertAndSend("/topic/comments"+movieId,response);
        return response;
    }

    public CommentResponse updateCommentToMovie(String commentId, CommentRequest request) {
        Comment comment = checkOwnershipAndGetComment(commentId);
        commentMapper.updateComment(comment, request);
        Comment updated = commentRepository.save(comment);
        return commentMapper.toCommentResponse(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CommentResponse updateCommentToMovieForAdmin(String commentId, CommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        commentMapper.updateComment(comment, request);
        Comment updated = commentRepository.save(comment);
        return commentMapper.toCommentResponse(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCommentForAdmin(DeleteCommentRequest request) {
        for (String commentId : request.getCommentsIds()) {
            if (!commentRepository.existsById(commentId)) {
                throw new AppException(ErrorCode.COMMENT_NOT_FOUND);
            }
        }
        commentRepository.deleteAllById(request.getCommentsIds());
    }

    public void deleteComment(String commentId) {
        Comment comment = checkOwnershipAndGetComment(commentId);
        commentRepository.delete(comment);
    }

    public List<CommentResponse> searchComment(CommentSearchRequest request) {
        Specification<Comment> spec = CommentSpecification.fromRequest(request);
        return commentRepository.findAll(spec)
                .stream()
                .map(commentMapper::toCommentResponse)
                .toList();
    }

    private Comment checkOwnershipAndGetComment(String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!comment.getUser().getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return comment;
    }
}

