package com.project.lumix.mapper;

import com.project.lumix.dto.request.PermissionRequest;
import com.project.lumix.dto.response.PermissionResponse;
import com.project.lumix.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring"
)
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
