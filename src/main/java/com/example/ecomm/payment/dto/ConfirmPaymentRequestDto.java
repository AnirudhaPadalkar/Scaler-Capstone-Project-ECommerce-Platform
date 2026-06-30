package com.example.ecomm.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfirmPaymentRequestDto {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Gateway payment ID is required")
    private String gatewayPaymentId;
}
