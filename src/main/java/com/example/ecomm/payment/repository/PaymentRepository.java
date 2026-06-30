package com.example.ecomm.payment.repository;

import com.example.ecomm.payment.model.Payment;
import com.example.ecomm.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByOrderIdAndStatus(String orderId, PaymentStatus status);
    Optional<Payment> findByOrderId(String orderId);
}
