package com.project.lumix.dto.response;

import com.project.lumix.enums.PaymentStatus;
import com.project.lumix.enums.PlanType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO dành riêng cho Admin – trả về thông tin chi tiết giao dịch
 * kèm theo thông tin User (userId, email, username).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAdminResponse {

    private String paymentId;
    private String orderId;
    private Long amount;
    private String orderInfo;
    private PaymentStatus status;
    private PlanType planType;

    // Thông tin User liên kết
    private String userId;
    private String userEmail;
    private String username;

    // Thông tin giao dịch MoMo
    private Long transId;
    private Integer resultCode;
    private String message;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
