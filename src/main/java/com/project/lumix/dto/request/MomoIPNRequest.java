package com.project.lumix.dto.request;

import lombok.*;

/**
 * DTO nhận từ MoMo khi họ gọi về IPN URL (Instant Payment Notification).
 * MoMo sẽ POST JSON này sang endpoint /api/momo/ipn-handler của chúng ta.
 *
 * Tài liệu tham khảo:
 * https://developers.momo.vn/#/docs/en/aiov2/?id=payment-notification
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MomoIPNRequest {

    private String partnerCode;
    private String orderId;
    private String requestId;
    private Long amount;
    private String orderInfo;
    private String orderType;
    private Long transId;
    private Integer resultCode;
    private String message;
    private String payType;
    private Long responseTime;
    private String extraData;

    /**
     * Signature do MoMo gửi kèm — PHẢI được xác minh trước khi xử lý
     */
    private String signature;
}
