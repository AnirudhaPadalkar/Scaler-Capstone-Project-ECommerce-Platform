package com.example.ecomm.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CheckoutResponseDto {
    private String     orderId;
    private BigDecimal total;
    private String     message;
}
