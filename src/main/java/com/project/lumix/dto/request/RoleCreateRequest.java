package com.project.lumix.dto.request;

import java.util.Set;
import lombok.Data;

@Data
public class RoleCreateRequest {
    private String name;
    private String description;
    private Set<String> permissions;
}
