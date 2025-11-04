package com.project.lumix.controller;

import com.project.lumix.dto.request.PermissionRequest;
import com.project.lumix.dto.response.ApiResponse;
import com.project.lumix.dto.response.PermissionResponse;
import com.project.lumix.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/permissions")
public class PermissionController {
    private final PermissionService permissionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PermissionResponse> create(@RequestBody @Valid PermissionRequest request) {
        log.info("Create permission request: {}", request.getName());
        return ApiResponse.<PermissionResponse>builder()
                .result(permissionService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<PermissionResponse>> getAll() {
        log.info("Get all permissions");
        return ApiResponse.<List<PermissionResponse>>builder()
                .result(permissionService.getAll())
                .build();
    }

    @DeleteMapping("/{permissionName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String permissionName) {
        log.info("Delete permission: {}", permissionName);
        permissionService.delete(permissionName);
    }
}

