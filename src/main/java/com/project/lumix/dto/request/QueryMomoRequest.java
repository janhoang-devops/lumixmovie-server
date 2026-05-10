package com.project.lumix.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryMomoRequest {
    private String partnerCode;
    private String requestId;
    private String orderId;
    private String signature;
    private String lang;
}
