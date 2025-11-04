package com.project.lumix.controller;
import java.util.List;

import com.project.lumix.dto.request.GenreRequest;
import com.project.lumix.dto.response.ApiResponse;
import com.project.lumix.dto.response.GenreResponse;
import com.project.lumix.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/movie/genre"})
@RequiredArgsConstructor
@Slf4j
public class GenreController {
    private final MovieService movieService;

    @GetMapping
    ApiResponse<List<GenreResponse>> getGenre() {
        log.info("Get all genres");
        return ApiResponse.<List<GenreResponse>>builder()
                .result(this.movieService.getGenre())
                .build();
    }

    @PostMapping
    ApiResponse<GenreResponse> createGenre(@RequestBody GenreRequest request){
        return ApiResponse.<GenreResponse>builder()
                .result(this.movieService.createGenre(request))
                .build();
    }

    @PutMapping("/{genreId}")
    ApiResponse<GenreResponse> updateGenre(@PathVariable String genreId,@RequestBody GenreRequest request){
        return ApiResponse.<GenreResponse>builder()
                .result(this.movieService.updateGenre(genreId,request))
                .build();
    }

    @DeleteMapping("/{genreId}")
    ApiResponse<Void> deleteGenre(@PathVariable String genreId){
        this.movieService.deleteGenre(genreId);
        return ApiResponse.<Void>builder()
                .message("Delete genre successfully!")
                .build();
    }
}