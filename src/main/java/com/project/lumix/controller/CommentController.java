package com.project.lumix.controller;

import com.project.lumix.dto.request.CommentRequest;
import com.project.lumix.dto.response.ApiResponse;
import com.project.lumix.dto.response.CommentResponse;
import com.project.lumix.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/movie/{movieId}/comments"})
@Slf4j
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping
    public ApiResponse<List<CommentResponse>> getComments(@PathVariable String movieId) {
        log.info("Get comments for movie: {}", movieId);
        return ApiResponse.<List<CommentResponse>>builder()
                .result(this.commentService.getCommentsForMovie(movieId))
                .build();
    }

    @PostMapping
    public ApiResponse<CommentResponse> addComment(@PathVariable String movieId, @RequestBody @Valid CommentRequest request) {
        log.info("Add comment to movie: {}", movieId);
        return ApiResponse.<CommentResponse>builder()
                .result(this.commentService.addCommentToMovie(movieId, request))
                .build();
    }

    @PutMapping({"/{commentId}"})
    public ApiResponse<CommentResponse> updateComment(@PathVariable String commentId, @RequestBody @Valid CommentRequest request) {
        log.info("Update comment: {}", commentId);
        return ApiResponse.<CommentResponse>builder()
                .result(this.commentService.updateCommentToMovie(commentId, request))
                .build();
    }

    @DeleteMapping({"/{commentId}"})
    public ApiResponse<Void> deleteComment(@PathVariable String commentId) {
        log.info("Delete comment: {}", commentId);
        this.commentService.deleteComment(commentId);
        return ApiResponse.<Void>builder()
                .message("Comment deleted successfully")
                .build();
    }
}
