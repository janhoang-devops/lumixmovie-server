package com.project.lumix.controller;
import java.util.List;

import com.project.lumix.dto.request.MovieSearchRequest;
import com.project.lumix.dto.response.ApiResponse;
import com.project.lumix.dto.response.CommentResponse;
import com.project.lumix.dto.response.MovieDetailResponse;
import com.project.lumix.service.CommentService;
import com.project.lumix.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movie")
@Slf4j
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;
    private final CommentService commentService;
    // ===== MOVIE DETAILS =====
    @GetMapping("/{movieId}")
    public ApiResponse<MovieDetailResponse> getMovie(@PathVariable String movieId) {
        log.info("Get movie details, id: {}", movieId);
        return ApiResponse.<MovieDetailResponse>builder()
                .result(movieService.getMovie(movieId))
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<MovieDetailResponse>> getAllMovies() {
        log.info("Get all movies");
        return ApiResponse.<List<MovieDetailResponse>>builder()
                .result(movieService.getAllMovies())
                .build();
    }

    // ===== SEARCH =====
    @GetMapping("/search")
    public ApiResponse<Page<MovieDetailResponse>> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @PageableDefault Pageable pageable,
            MovieSearchRequest request) {
        log.info("Search movies by title: {}, genre: {}", title, genre);
        request.setTitle(title);
        request.setGenre(genre);
        return ApiResponse.<Page<MovieDetailResponse>>builder()
                .result(movieService.searchMovies(request, pageable))
                .build();
    }

    // ===== POPULAR & TRENDING =====
    @GetMapping("/popular")
    public ApiResponse<List<MovieDetailResponse>> getPopularMovies() {
        log.info("Get popular movies");
        return ApiResponse.<List<MovieDetailResponse>>builder()
                .result(movieService.getPopularMovies())
                .build();
    }

    @GetMapping("/trending")
    public ApiResponse<List<MovieDetailResponse>> getTrendingMovies() {
        log.info("Get trending movies");
        return ApiResponse.<List<MovieDetailResponse>>builder()
                .result(movieService.getTrendingMovies())
                .build();
    }

    // ===== GENRES =====
    @GetMapping("/genre/{genreName}")
    public ApiResponse<List<MovieDetailResponse>> getMoviesByGenre(@PathVariable String genreName) {
        log.info("Get movies by genre: {}", genreName);
        return ApiResponse.<List<MovieDetailResponse>>builder()
                .result(movieService.getMoviesByGenres(genreName))
                .build();
    }

    @GetMapping("/by-genres")
    public ApiResponse<List<MovieDetailResponse>> getMoviesByGenreNames(@RequestParam List<String> genres) {
        log.info("Get movies by multiple genres: {}", genres);
        return ApiResponse.<List<MovieDetailResponse>>builder()
                .result(movieService.getMoviesByGenreNames(genres))
                .build();
    }

//    COMMENT
    @GetMapping("/comments")
    public ApiResponse<List<CommentResponse>> getAllComments() {
        log.info("Admin fetching all comments");
        return ApiResponse.<List<CommentResponse>>builder()
                .result(commentService.getAllComment())
                .build();
    }
}

