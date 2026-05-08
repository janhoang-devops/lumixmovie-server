package com.project.lumix.enums;

public enum PaymentStatus {
    PENDING,    // Đã tạo QR, chưa thanh toán
    SUCCESS,    // MoMo xác nhận thanh toán thành công
    FAILED,     // Thanh toán thất bại
    CANCELLED   // Người dùng huỷ
}
