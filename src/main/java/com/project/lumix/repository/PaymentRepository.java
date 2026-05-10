package com.project.lumix.repository;

import com.project.lumix.entity.Payment;
import com.project.lumix.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByOrderId(String orderId);

    boolean existsByOrderIdAndStatus(String orderId, PaymentStatus status);

    /** Lấy toàn bộ đơn thanh toán, mới nhất trước – dùng cho Admin */
    List<Payment> findAllByOrderByCreatedAtDesc();

    /** Lọc theo trạng thái – dùng cho Admin */
    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    /** Phân trang – dùng cho Admin dashboard lớn */
    Page<Payment> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Đếm số đơn theo trạng thái */
    long countByStatus(PaymentStatus status);

    /** Tính tổng doanh thu (chỉ đơn SUCCESS) */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'SUCCESS'")
    Long sumSuccessAmount();

    /** Lấy các đơn theo userId */
    @Query("SELECT p FROM Payment p WHERE p.user.userId = :userId ORDER BY p.createdAt DESC")
    List<Payment> findByUserId(@Param("userId") String userId);
}
