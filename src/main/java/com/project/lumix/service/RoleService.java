package com.project.lumix.service;

import com.project.lumix.dto.request.RoleCreateRequest;
import com.project.lumix.dto.request.RoleUpdateRequest;
import com.project.lumix.dto.response.RoleResponse;
import com.project.lumix.entity.Permission;
import com.project.lumix.entity.Role;
import com.project.lumix.exception.AppException;
import com.project.lumix.exception.ErrorCode;
import com.project.lumix.mapper.RoleMapper;
import com.project.lumix.repository.PermissionRepository;
import com.project.lumix.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionRepository permissionRepository;

    public RoleResponse create(RoleCreateRequest request) {
        Role role = this.roleMapper.toRole(request);

        List<Permission> permissions = this.permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));

        Role savedRole = this.roleRepository.save(role);
        return this.roleMapper.toRoleResponse(savedRole);
    }

    public List<RoleResponse> getAll() {
        return this.roleRepository.findAll()
                .stream()
                .map(this.roleMapper::toRoleResponse)
                .collect(Collectors.toList());
    }

    public RoleResponse update(String roleName, RoleUpdateRequest request) {
        Role role = this.roleRepository.findById(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));

        this.roleMapper.updateRole(role, request);

        Role updatedRole = this.roleRepository.save(role);
        return this.roleMapper.toRoleResponse(updatedRole);
    }

    public void delete(String roleName) {
        Role role = this.roleRepository.findById(roleName)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
        this.roleRepository.delete(role);
    }
}
