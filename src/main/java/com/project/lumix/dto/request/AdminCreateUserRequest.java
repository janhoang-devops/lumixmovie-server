package com.project.lumix.dto.request;

import java.util.List;
import lombok.Data;

@Data
public class AdminCreateUserRequest {
    private String username;
    private String password;
    private String email;
    private List<String> roles;
    private boolean enabled;
}
