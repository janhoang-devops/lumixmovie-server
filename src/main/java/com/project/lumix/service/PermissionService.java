package com.project.lumix.service;

import com.project.lumix.dto.request.PermissionRequest;
import com.project.lumix.dto.response.PermissionResponse;
import com.project.lumix.entity.Permission;
import com.project.lumix.exception.AppException;
import com.project.lumix.exception.ErrorCode;
import com.project.lumix.mapper.PermissionMapper;
import com.project.lumix.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;


    public PermissionResponse create(PermissionRequest request) {
        if (permissionRepository.existsById(request.getName())) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        Permission permission = this.permissionMapper.toPermission(request);
        Permission savedPermission = this.permissionRepository.save(permission);

        return this.permissionMapper.toPermissionResponse(savedPermission);
    }

    public List<PermissionResponse> getAll() {
        return this.permissionRepository.findAll()
                .stream()
                .map(this.permissionMapper::toPermissionResponse)
                .collect(Collectors.toList());
    }

    public void delete(String permissionName) {
        Permission permission = this.permissionRepository.findById(permissionName)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));

        this.permissionRepository.delete(permission);
    }
}
