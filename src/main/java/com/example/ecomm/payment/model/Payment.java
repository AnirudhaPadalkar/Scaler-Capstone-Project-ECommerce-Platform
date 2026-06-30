package com.example.ecomm.payment.model;

import com.example.ecomm.shared.model.BaseModel;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseModel {

    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false, length = 50)
    private String gateway;

    @Column(name = "gateway_order_id", length = 255)
    private String gatewayOrderId;

    @Column(name = "gateway_payment_id", length = 255, unique = true)
    private String gatewayPaymentId;
}
