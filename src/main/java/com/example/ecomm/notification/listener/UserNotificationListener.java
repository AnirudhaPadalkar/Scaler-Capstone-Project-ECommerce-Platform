package com.example.ecomm.notification.listener;

import com.example.ecomm.notification.service.NotificationService;
import com.example.ecomm.user.event.PasswordResetRequestedEvent;
import com.example.ecomm.user.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserNotificationListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        try {
            notificationService.sendWelcome(event.getEmail(), event.getFirstName());
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", event.getEmail(), e.getMessage());
        }
    }

    @Async
    @EventListener
    public void onPasswordResetRequested(PasswordResetRequestedEvent event) {
        try {
            notificationService.sendPasswordReset(
                    event.getEmail(), event.getFirstName(), event.getResetToken());
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", event.getEmail(), e.getMessage());
        }
    }
}
