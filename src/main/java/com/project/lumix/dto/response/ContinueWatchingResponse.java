package com.project.lumix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContinueWatchingResponse {
    private MovieDetailResponse movie;
    private int progressInSeconds;
    private String lastWatchedAt;
}
