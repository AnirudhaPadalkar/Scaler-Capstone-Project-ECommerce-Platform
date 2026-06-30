package com.example.ecomm.payment.dto;

import com.example.ecomm.payment.model.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentResponseDto {
    private String        paymentId;
    private String        orderId;
    private BigDecimal    amount;
    private String        currency;
    private PaymentStatus status;
    private String        gatewayOrderId;
}
