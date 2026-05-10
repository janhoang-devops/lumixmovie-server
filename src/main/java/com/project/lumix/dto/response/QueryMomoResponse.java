package com.project.lumix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryMomoResponse {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private String extraData;
    private Long amount;
    private Long transId;
    private String payType;
    private Integer resultCode;
    private String message;
    private String responseTime;
}
