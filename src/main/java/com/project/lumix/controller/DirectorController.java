package com.project.lumix.controller;
import java.util.List;

import com.project.lumix.dto.response.ApiResponse;
import com.project.lumix.dto.response.DirectorResponse;
import com.project.lumix.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/movie/director"})
@RequiredArgsConstructor
@Slf4j
public class DirectorController {
    private final MovieService movieService;

    @GetMapping
    ApiResponse<List<DirectorResponse>> getDirector() {
        log.info("Get all directors");
        return ApiResponse.<List<DirectorResponse>>builder()
                .result(this.movieService.getDirector())
                .build();
    }
}
