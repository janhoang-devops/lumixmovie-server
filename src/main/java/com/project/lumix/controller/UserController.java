package com.project.lumix.controller;
import com.project.lumix.dto.request.AuthenticationRequest;
import com.project.lumix.dto.request.UserCreateRequest;
import com.project.lumix.dto.request.UserUpdateRequest;
import com.project.lumix.dto.response.ApiResponse;
import com.project.lumix.dto.response.MovieDetailResponse;
import com.project.lumix.dto.response.UserResponse;
import com.project.lumix.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreateRequest request) {
        log.info("Create user request: {}", request.getUsername());
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getUsers() {
        log.info("Get all users");
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUser())
                .build();
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUserById(@PathVariable String userId) {
        log.info("Get user by id: {}", userId);
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo(userId))
                .build();
    }

    // Lấy info user đang đăng nhập
    @GetMapping("/me")
    public ApiResponse<UserResponse> getMyInfo(AuthenticationRequest authentication) {
        String userId = authentication.getUsername(); // hoặc lấy từ JWT token
        log.info("Get my info, userId: {}", userId);
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo(userId))
                .build();
    }

    @PutMapping("/{userId}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable String userId,
            @RequestBody @Valid UserUpdateRequest request) {
        log.info("Update user request, id: {}", userId);
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<String> deleteUser(@PathVariable String userId) {
        log.info("Delete user request, id: {}", userId);
        userService.deleteUser(userId);
        return ApiResponse.<String>builder()
                .result("User has been deleted!")
                .build();
    }

    // ===== FAVORITE MOVIES =====

    @GetMapping("/{userId}/favorites")
    public ApiResponse<List<MovieDetailResponse>> getFavoriteMovies(@PathVariable String userId) {
        log.info("Get favorite movies of user: {}", userId);
        return ApiResponse.<List<MovieDetailResponse>>builder()
                .result(userService.getFavoriteMovies(userId))
                .build();
    }

    @PostMapping("/{userId}/favorites")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addFavorite(@PathVariable String userId, @RequestBody Map<String, String> body) {
        String movieId = body.get("movieId");
        log.info("Add favorite movie, userId: {}, movieId: {}", userId, movieId);
        userService.addMovieToFavorites(userId, movieId);
    }

    @DeleteMapping("/{userId}/favorites/{movieId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(@PathVariable String userId, @PathVariable String movieId) {
        log.info("Remove favorite movie, userId: {}, movieId: {}", userId, movieId);
        userService.removeMovieFromFavorites(userId, movieId);
    }
}

