package com.example.ecomm.notification.service;

import com.example.ecomm.notification.dto.EmailNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;

    @Value("${app.mail.base-url:http://localhost:8080}")
    private String baseUrl;

    public void sendWelcome(String email, String firstName) {
        emailService.send(EmailNotificationDto.builder()
                .to(email)
                .subject("Welcome to Ecomm, " + firstName + "!")
                .body("Hi " + firstName + ",\n\n" +
                      "Welcome! Your account is ready.\n\n" +
                      "Start shopping: " + baseUrl + "\n\nThe Ecomm Team")
                .build());
    }

    public void sendPasswordReset(String email, String firstName, String token) {
        String resetUrl = baseUrl + "/reset-password?token=" + token;
        emailService.send(EmailNotificationDto.builder()
                .to(email)
                .subject("Reset your Ecomm password")
                .body("Hi " + firstName + ",\n\n" +
                      "Reset your password (expires in 30 minutes):\n" +
                      resetUrl + "\n\n" +
                      "If you didn't request this, ignore this email.\n\nThe Ecomm Team")
                .build());
    }

    public void sendPaymentReceipt(String email, String firstName,
                                    String orderId, String amount) {
        emailService.send(EmailNotificationDto.builder()
                .to(email)
                .subject("Payment confirmed — Order #" + orderId.substring(0, 8).toUpperCase())
                .body("Hi " + firstName + ",\n\n" +
                      "Your payment of ₹" + amount + " has been confirmed.\n" +
                      "Order ID: #" + orderId.substring(0, 8).toUpperCase() + "\n\n" +
                      "We'll notify you once your order ships.\n\nThe Ecomm Team")
                .build());
    }

    public void sendOrderConfirmation(String email, String firstName,
                                       String orderId, String total) {
        emailService.send(EmailNotificationDto.builder()
                .to(email)
                .subject("Order confirmed — #" + orderId.substring(0, 8).toUpperCase())
                .body("Hi " + firstName + ",\n\n" +
                      "Your order #" + orderId.substring(0, 8).toUpperCase() +
                      " is confirmed. Total: ₹" + total + "\n\n" +
                      "Track your order: " + baseUrl + "/orders/" + orderId + "\n\nThe Ecomm Team")
                .build());
    }
}
