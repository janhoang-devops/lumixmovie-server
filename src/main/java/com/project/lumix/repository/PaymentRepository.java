package com.project.lumix.repository;

import com.project.lumix.entity.Payment;
import com.project.lumix.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByOrderId(String orderId);

    boolean existsByOrderIdAndStatus(String orderId, PaymentStatus status);
}
