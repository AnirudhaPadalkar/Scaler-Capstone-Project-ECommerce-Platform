package com.example.ecomm.payment.controller;

import com.example.ecomm.payment.dto.ConfirmPaymentRequestDto;
import com.example.ecomm.payment.dto.InitiatePaymentRequestDto;
import com.example.ecomm.payment.dto.PaymentResponseDto;
import com.example.ecomm.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponseDto> initiate(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody InitiatePaymentRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.initiatePayment(userId, request));
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResponseDto> confirm(
            @Valid @RequestBody ConfirmPaymentRequestDto request) {
        return ResponseEntity.ok(paymentService.confirmPayment(request));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponseDto> getByOrder(
            @AuthenticationPrincipal String userId,
            @PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.getByOrderId(orderId, userId));
    }
}
