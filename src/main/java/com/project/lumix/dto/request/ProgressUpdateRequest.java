package com.project.lumix.dto.request;
import lombok.Data;

@Data
public class ProgressUpdateRequest {
    private int progressInSeconds;
    private boolean isFinished;
}
