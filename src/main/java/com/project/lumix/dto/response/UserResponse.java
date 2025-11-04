package com.project.lumix.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String userId;
    private String username;
    private String email;
    @JsonFormat(
            pattern = "dd-MM-yyyy"
    )
    private LocalDateTime createdAt;
    private String enabled;
    private String provider;
    private Set<RoleResponse> roles;
}
