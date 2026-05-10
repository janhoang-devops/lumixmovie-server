package com.project.lumix.dto.response;

import lombok.*;

/**
 * Thống kê tổng hợp doanh thu / đơn hàng – hiển thị trên dashboard Admin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatsResponse {

    /** Tổng số giao dịch */
    private long totalOrders;

    /** Số giao dịch thành công */
    private long successOrders;

    /** Số giao dịch đang chờ */
    private long pendingOrders;

    /** Số giao dịch thất bại */
    private long failedOrders;

    /** Tổng doanh thu (chỉ tính đơn SUCCESS) – đơn vị VNĐ */
    private long totalRevenue;
}
