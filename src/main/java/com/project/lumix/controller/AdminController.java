package com.project.lumix.controller;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

import com.project.lumix.dto.request.*;
import com.project.lumix.dto.response.ApiResponse;
import com.project.lumix.dto.response.CommentResponse;
import com.project.lumix.dto.response.UserResponse;
import com.project.lumix.service.CommentService;
import com.project.lumix.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@RequiredArgsConstructor
public class AdminController {
    private final CommentService commentService;
    private final UserService userService;

    // --- QUẢN LÝ USER ---
    @PostMapping("/users/create")
    public ApiResponse<UserResponse> createUserByAdmin(@RequestBody AdminCreateUserRequest request) {
        log.info("Admin creating user: {}", request.getUsername());
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUserByAdmin(request))
                .build();
    }

    @PutMapping("/users/{userId}/roles")
    public ApiResponse<UserResponse> updateUserRoles(
            @PathVariable String userId,
            @RequestBody RoleUpdateRequest request) {
        log.info("Admin updating roles for userId: {}", userId);
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUserRoles(userId, request))
                .build();
    }

    // --- QUẢN LÝ COMMENT ---
    @GetMapping("/comments")
    public ApiResponse<List<CommentResponse>> getAllComments() {
        log.info("Admin fetching all comments");
        return ApiResponse.<List<CommentResponse>>builder()
                .result(commentService.getAllComment())
                .build();
    }

    @PutMapping("/comments/{commentId}")
    public ApiResponse<CommentResponse> updateComment(
            @PathVariable String commentId,
            @RequestBody CommentRequest request) {
        log.info("Admin updating commentId: {}", commentId);
        return ApiResponse.<CommentResponse>builder()
                .result(commentService.updateCommentToMovieForAdmin(commentId, request))
                .build();
    }

    @DeleteMapping("/comments")
    public ApiResponse<Void> deleteComments(@RequestBody DeleteCommentRequest request) {
        log.info("Admin deleting comments: {}", request);
        commentService.deleteCommentForAdmin(request);
        return ApiResponse.<Void>builder()
                .message("Comments deleted successfully")
                .build();
    }

    @GetMapping("search/comments")
    public ApiResponse<List<CommentResponse>> searchComments(
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String movieName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate
            ) {
        CommentSearchRequest request = CommentSearchRequest.builder()
                .content(content)
                .username(username)
                .movieName(movieName)
                .startDate(fromDate)
                .endDate(toDate)
                .build();
        log.info("Admin searching comments with filter: {}", request);

        List<CommentResponse> results = commentService.searchComment(request);
        return ApiResponse.<List<CommentResponse>>builder()
                .result(results)
                .build();
    }
}

