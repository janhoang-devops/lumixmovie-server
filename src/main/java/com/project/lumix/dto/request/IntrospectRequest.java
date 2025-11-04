package com.project.lumix.dto.request;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IntrospectRequest {
    private String token;
}
