package com.project.lumix.dto.response;

import com.project.lumix.enums.PaymentStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response trả về cho Frontend sau khi tạo QR thành công,
 * hoặc khi query trạng thái thanh toán.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String paymentId;
    private String orderId;
    private Long amount;
    private String orderInfo;
    private PaymentStatus status;

    /** URL để redirect người dùng sang trang thanh toán MoMo (web) */
    private String payUrl;

    /** Deeplink mở thẳng app MoMo (mobile) */
    private String deeplink;

    /** URL QR code để hiển thị (nếu muốn tự render QR) */
    private String qrCodeUrl;

    private LocalDateTime createdAt;
}
