package com.project.lumix.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateRequest {
    private @Size(min = 3, message = "USER_INVALID")
    String username;
    private @Size(min = 6, message = "USER_PASSWORD")
    String password;
    private String email;
}
