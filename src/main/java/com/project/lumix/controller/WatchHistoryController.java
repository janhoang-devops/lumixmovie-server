package com.project.lumix.controller;

import java.util.List;

import com.project.lumix.dto.request.ProgressUpdateRequest;
import com.project.lumix.dto.response.ApiResponse;
import com.project.lumix.dto.response.ContinueWatchingResponse;
import com.project.lumix.service.WatchHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/history")
public class WatchHistoryController {
    private final WatchHistoryService watchHistoryService;

    // Cập nhật tiến trình xem phim
    @PostMapping("/progress/{movieId}")
    public ApiResponse<Void> updateProgress(
            @PathVariable String movieId,
            @Valid @RequestBody ProgressUpdateRequest request) {
        log.info("Updating watch progress for movieId={}, progress={}", movieId, request.getProgressInSeconds());
        watchHistoryService.updateWatchProgress(movieId, request);
        return ApiResponse.<Void>builder()
                .message("Tiến trình đã được cập nhật thành công")
                .build();
    }

    // Lấy danh sách phim đang xem dở (Continue Watching)
    @GetMapping("/continue-watching")
    public ApiResponse<List<ContinueWatchingResponse>> getContinueWatchingList() {
        log.info("Fetching continue watching list");
        return ApiResponse.<List<ContinueWatchingResponse>>builder()
                .result(watchHistoryService.getContinueWatchingList())
                .build();
    }

    // Xóa lịch sử xem phim theo movieId
    @DeleteMapping("/{movieId}")
    public ApiResponse<Void> deleteWatchHistoryById(@PathVariable String movieId) {
        log.info("Deleting watch history for movieId={}", movieId);
        watchHistoryService.deleteWatchHistoryByMovieId(movieId);
        return ApiResponse.<Void>builder()
                .message("Xóa lịch sử thành công")
                .build();
    }

}

