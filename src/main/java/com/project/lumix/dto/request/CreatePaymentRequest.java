package com.project.lumix.dto.request;

import com.project.lumix.enums.PlanType;
import lombok.*;

/**
 * DTO nhận từ Frontend khi muốn tạo QR thanh toán MoMo để mua gói hội viên.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    /**
     * Loại gói hội viên muốn đăng ký (MONTHLY / QUARTERLY / YEARLY)
     */
    private PlanType planType;

    /**
     * ID người dùng thực hiện thanh toán
     */
    private String userId;
}
