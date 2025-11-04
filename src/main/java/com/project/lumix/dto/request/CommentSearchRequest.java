package com.project.lumix.dto.request;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentSearchRequest {
    private String content;
    private String username;
    private String movieName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
