package com.project.lumix.dto.request;

import java.util.List;
import lombok.Data;

@Data
public class DeleteCommentRequest {
    private List<String> commentsIds;
}
