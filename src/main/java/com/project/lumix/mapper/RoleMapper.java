package com.project.lumix.mapper;

import com.project.lumix.dto.request.RoleCreateRequest;
import com.project.lumix.dto.request.RoleUpdateRequest;
import com.project.lumix.dto.response.RoleResponse;
import com.project.lumix.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
        componentModel = "spring"
)
public interface RoleMapper {
    @Mapping(
            target = "permissions",
            ignore = true
    )
    Role toRole(RoleCreateRequest request);

    RoleResponse toRoleResponse(Role role);

    void updateRole(@MappingTarget Role role, RoleUpdateRequest request);
}
