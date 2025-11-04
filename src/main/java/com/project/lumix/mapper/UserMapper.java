package com.project.lumix.mapper;

import com.project.lumix.dto.request.AdminCreateUserRequest;
import com.project.lumix.dto.request.UserCreateRequest;
import com.project.lumix.dto.request.UserUpdateRequest;
import com.project.lumix.dto.response.UserResponse;
import com.project.lumix.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUsers(UserCreateRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    @Mapping(target = "roles", ignore = true)
    User toUserFromAdminRequest(AdminCreateUserRequest request);
}
