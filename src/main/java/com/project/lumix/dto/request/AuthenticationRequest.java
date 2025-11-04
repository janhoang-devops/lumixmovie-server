package com.project.lumix.dto.request;
import lombok.Data;

@Data
public class AuthenticationRequest {
    private String username;
    private String password;
}
