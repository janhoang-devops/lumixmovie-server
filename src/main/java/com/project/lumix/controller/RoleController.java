package com.project.lumix.controller;

import java.util.List;

import com.project.lumix.dto.request.RoleCreateRequest;
import com.project.lumix.dto.request.RoleUpdateRequest;
import com.project.lumix.dto.response.ApiResponse;
import com.project.lumix.dto.response.RoleResponse;
import com.project.lumix.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/roles")
@Slf4j
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoleResponse> create(@RequestBody @Valid RoleCreateRequest request) {
        log.info("Create role request: {}", request.getName());
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<RoleResponse>> getAll() {
        log.info("Get all roles");
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }

    @PutMapping("/{roleName}")
    public ApiResponse<RoleResponse> update(@PathVariable String roleName,
                                            @RequestBody @Valid RoleUpdateRequest request) {
        log.info("Update role: {}", roleName);
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.update(roleName, request))
                .build();
    }

    @DeleteMapping("/{roleName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String roleName) {
        log.info("Delete role: {}", roleName);
        roleService.delete(roleName);
    }
}

