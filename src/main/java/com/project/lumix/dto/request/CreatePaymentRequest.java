package com.project.lumix.dto.request;

import lombok.*;

/**
 * DTO nhận từ Frontend khi muốn tạo QR thanh toán MoMo.
 * userId là optional — nếu hệ thống yêu cầu đăng nhập thì truyền vào,
 * nếu không thì bỏ qua.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    /**
     * Số tiền cần thanh toán (đơn vị VNĐ, tối thiểu 1000đ theo MoMo sandbox)
     */
    private Long amount;

    /**
     * Mô tả đơn hàng (hiển thị trên app MoMo)
     */
    private String orderInfo;

    /**
     * ID người dùng thực hiện thanh toán (optional)
     */
    private String userId;
}
