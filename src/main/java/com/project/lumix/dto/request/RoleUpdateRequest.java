package com.project.lumix.dto.request;

import java.util.List;
import lombok.Data;

@Data
public class RoleUpdateRequest {
    private List<String> roles;
}
