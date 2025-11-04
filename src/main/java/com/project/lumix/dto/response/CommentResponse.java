package com.project.lumix.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private String id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String username;
    private String userId;
    private String email;
    private String movieId;
    private String posterUrl;
    private String movieTitle;
    private String year;
    private String parentId;
    private String parentName;
    private List<CommentResponse> replies;
    private long likeCount;
    private long dislikeCount;
    private String userReaction;
}
