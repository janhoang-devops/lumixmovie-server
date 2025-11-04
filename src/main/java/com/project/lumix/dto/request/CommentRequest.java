package com.project.lumix.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CommentRequest {
    private String parentId;
    private @NotEmpty(message = "Comment content cannot be empty")
    String content;
}
