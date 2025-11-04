package com.project.lumix.service;

import com.project.lumix.dto.request.ProgressUpdateRequest;
import com.project.lumix.dto.response.ContinueWatchingResponse;
import com.project.lumix.entity.Movie;
import com.project.lumix.entity.User;
import com.project.lumix.entity.WatchHistory;
import com.project.lumix.exception.AppException;
import com.project.lumix.exception.ErrorCode;
import com.project.lumix.mapper.MovieMapper;
import com.project.lumix.repository.MovieRepository;
import com.project.lumix.repository.UserRepository;
import com.project.lumix.repository.WatchHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WatchHistoryService {

    private static final Logger log = LoggerFactory.getLogger(WatchHistoryService.class);

    private final WatchHistoryRepository watchHistoryRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;


    /**
     * Cập nhật tiến trình xem phim
     */
    @Transactional
    public void updateWatchProgress(String movieId, ProgressUpdateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        WatchHistory watchHistory = watchHistoryRepository.findByUserAndMovieId(user, movieId)
                .orElseGet(() -> {
                    Movie movie = movieRepository.findById(movieId)
                            .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
                    return WatchHistory.builder()
                            .user(user)
                            .movie(movie)
                            .build();
                });

        watchHistory.setProgressInSeconds(request.getProgressInSeconds());
        watchHistory.setFinished(request.isFinished());

        watchHistoryRepository.save(watchHistory);
        log.info("Updated watch progress for user={}, movieId={}, progress={}",
                username, movieId, request.getProgressInSeconds());
    }

    /**
     * Lấy danh sách phim đang xem dở
     */
    public List<ContinueWatchingResponse> getContinueWatchingList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<WatchHistory> historyList = watchHistoryRepository
                .findByUserAndIsFinishedFalseOrderByLastWatchedAtDesc(user);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return historyList.stream()
                .map(history -> ContinueWatchingResponse.builder()
                        .movie(movieMapper.toMovieDetailResponse(history.getMovie()))
                        .progressInSeconds(history.getProgressInSeconds())
                        .lastWatchedAt(history.getLastWatchedAt().format(formatter))
                        .build()
                )
                .collect(Collectors.toList());
    }

    /**
     * Xóa lịch sử xem phim theo movieId
     */
    public void deleteWatchHistoryByMovieId(String movieId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        WatchHistory watchHistory = watchHistoryRepository.findByUserAndMovieId(user, movieId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

        watchHistoryRepository.delete(watchHistory);
        log.info("Deleted watch history for user={}, movieId={}", username, movieId);
    }
}
