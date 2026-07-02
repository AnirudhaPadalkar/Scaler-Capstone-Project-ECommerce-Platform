package com.example.ecomm.notification.service;

import com.example.ecomm.notification.dto.EmailNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Override
    public void send(EmailNotificationDto notification) {
        if (isDevMode()) {
            log.info("[DEV] Email suppressed — to={} subject={}", notification.getTo(), notification.getSubject());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(notification.getTo());
        message.setSubject(notification.getSubject());
        message.setText(notification.getBody());
        mailSender.send(message);
        log.info("Email sent to: {}", notification.getTo());
    }

    private boolean isDevMode() {
        String env = System.getenv("SPRING_PROFILES_ACTIVE");
        return env == null || !env.contains("prod");
    }
}
