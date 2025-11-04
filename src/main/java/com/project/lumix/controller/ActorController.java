package com.project.lumix.controller;

import java.util.List;

import com.project.lumix.dto.response.ActorResponse;
import com.project.lumix.dto.response.ApiResponse;
import com.project.lumix.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/movie/actor"})
@RequiredArgsConstructor
@Slf4j
public class ActorController {
    private final MovieService movieService;

    @GetMapping
    ApiResponse<List<ActorResponse>> getActor() {
        log.info("Get all actors");
        return ApiResponse.<List<ActorResponse>>builder()
                .result(this.movieService.getActor())
                .build();
    }
}
