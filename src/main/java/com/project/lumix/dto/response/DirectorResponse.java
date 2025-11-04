package com.project.lumix.dto.response;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectorResponse {
    private String id;
    @Column(unique = true, nullable = false)
    private String name;
}
