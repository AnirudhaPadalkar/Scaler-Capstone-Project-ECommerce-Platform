package com.example.ecomm.payment.service;

import com.example.ecomm.payment.dto.ConfirmPaymentRequestDto;
import com.example.ecomm.payment.dto.InitiatePaymentRequestDto;
import com.example.ecomm.payment.dto.PaymentResponseDto;

public interface PaymentService {
    PaymentResponseDto initiatePayment(String userId, InitiatePaymentRequestDto request);
    PaymentResponseDto confirmPayment(ConfirmPaymentRequestDto request);
    PaymentResponseDto getByOrderId(String orderId, String userId);
}
