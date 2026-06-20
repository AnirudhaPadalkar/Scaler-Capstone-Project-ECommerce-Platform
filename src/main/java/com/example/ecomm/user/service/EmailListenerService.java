package com.example.ecomm.user.service;

import com.example.ecomm.user.event.PasswordResetRequestedEvent;
import com.example.ecomm.user.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailListenerService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.base-url}")
    private String baseUrl;

    @Async
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(event.getEmail());
            message.setSubject("Welcome to Ecomm, " + event.getFirstName() + "!");
            message.setText(
                "Hi " + event.getFirstName() + ",\n\n" +
                "Welcome! Your account has been created successfully.\n\n" +
                "Start shopping at: " + baseUrl + "\n\n" +
                "The Ecomm Team"
            );
            mailSender.send(message);
            log.info("Welcome email sent to: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", event.getEmail(), e);
        }
    }

    @Async
    @EventListener
    public void handlePasswordResetRequested(PasswordResetRequestedEvent event) {
        try {
            String resetUrl = baseUrl + "/reset-password?token=" + event.getResetToken();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(event.getEmail());
            message.setSubject("Reset your Ecomm password");
            message.setText(
                "Hi " + event.getFirstName() + ",\n\n" +
                "You requested a password reset. Click the link below (expires in 30 minutes):\n\n" +
                resetUrl + "\n\n" +
                "If you didn't request this, ignore this email.\n\n" +
                "The Ecomm Team"
            );
            mailSender.send(message);
            log.info("Password reset email sent to: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", event.getEmail(), e);
        }
    }
}
