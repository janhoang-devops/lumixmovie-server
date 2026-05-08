package com.project.lumix.entity;

import com.project.lumix.enums.PaymentStatus;
import com.project.lumix.enums.PlanType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * orderId do BE tự sinh (UUID), cũng chính là khóa tra cứu khi MoMo callback
     */
    @Column(nullable = false, unique = true)
    private String orderId;

    /**
     * requestId tương ứng với orderId khi gọi MoMo
     */
    @Column(nullable = false)
    private String requestId;

    /**
     * Số tiền thanh toán (đơn vị: VNĐ)
     */
    @Column(nullable = false)
    private Long amount;

    /**
     * Thông tin đơn hàng (mô tả ngắn)
     */
    private String orderInfo;

    /**
     * payUrl mà MoMo trả về để redirect người dùng
     */
    @Column(length = 1024)
    private String payUrl;

    /**
     * deeplink để mở thẳng app MoMo (nếu trên mobile)
     */
    @Column(length = 1024)
    private String deeplink;

    /**
     * qrCodeUrl dùng để hiển thị QR trên giao diện web
     */
    @Column(length = 1024)
    private String qrCodeUrl;

    /**
     * Mã giao dịch phía MoMo (transId), chỉ có sau khi thanh toán thành công
     */
    private Long transId;

    /**
     * Mã kết quả trả về từ MoMo (0 = thành công)
     */
    private Integer resultCode;

    /**
     * Thông điệp từ MoMo
     */
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * User liên kết với đơn thanh toán này (nullable nếu không cần auth)
     */
    /**
     * Loại gói hội viên (MONTHLY / QUARTERLY / YEARLY)
     */
    @Enumerated(EnumType.STRING)
    private PlanType planType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
