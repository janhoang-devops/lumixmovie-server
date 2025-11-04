package com.project.lumix.controller;

import com.project.lumix.dto.request.MovieCreateRequest;
import com.project.lumix.dto.request.MovieUpdateRequest;
import com.project.lumix.dto.response.ApiResponse;
import com.project.lumix.dto.response.CloudinaryResponse;
import com.project.lumix.dto.response.MovieDetailResponse;
import com.project.lumix.service.CloudinaryService;
import com.project.lumix.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
@RestController
@RequestMapping("/admin/movie")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@RequiredArgsConstructor
public class AdminMovieController {
    private final MovieService movieService;
    private final CloudinaryService cloudinaryService;

    private static final String IMAGE_PATTERN = "^.+\\.(?i)(jpg|jpeg|png|gif|bmp|webp|svg)$";
    private static final String VIDEO_PATTERN = "^.+\\.(?i)(mp4|mov|avi|mkv|wmv)$";

    // --- CRUD MOVIE ---

    @PostMapping
    public ApiResponse<MovieDetailResponse> create(@RequestBody @Valid MovieCreateRequest request) {
        log.info("Admin creating movie: {}", request.getTitle());
        return ApiResponse.<MovieDetailResponse>builder()
                .result(movieService.create(request))
                .build();
    }

    @PutMapping("/{movieId}")
    public ApiResponse<MovieDetailResponse> update(
            @PathVariable String movieId,
            @RequestBody @Valid MovieUpdateRequest request) {
        log.info("Admin updating movie: {}", movieId);
        return ApiResponse.<MovieDetailResponse>builder()
                .result(movieService.update(movieId, request))
                .build();
    }

    @DeleteMapping("/{movieId}")
    public ApiResponse<String> delete(@PathVariable String movieId) {
        log.info("Admin deleting movie: {}", movieId);
        movieService.delete(movieId);
        return ApiResponse.<String>builder()
                .result("Movie deleted successfully")
                .build();
    }

    // --- UPLOAD FILES ---

    @PostMapping("/upload/image")
    public ApiResponse<CloudinaryResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("Admin uploading image: {}", file.getOriginalFilename());
        return ApiResponse.<CloudinaryResponse>builder()
                .result(cloudinaryService.uploadFile(file, IMAGE_PATTERN))
                .build();
    }

    @PostMapping("/upload/video")
    public ApiResponse<CloudinaryResponse> uploadVideo(@RequestParam("file") MultipartFile file) {
        log.info("Admin uploading video: {}", file.getOriginalFilename());
        return ApiResponse.<CloudinaryResponse>builder()
                .result(cloudinaryService.uploadFile(file, VIDEO_PATTERN))
                .build();
    }
}

