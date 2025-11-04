package com.project.lumix.dto.request;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class DirectorRequest {
    private String id;
    @Column(
            unique = true,
            nullable = false
    )
    private String name;
}
