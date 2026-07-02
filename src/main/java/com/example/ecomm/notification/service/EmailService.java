package com.example.ecomm.notification.service;

import com.example.ecomm.notification.dto.EmailNotificationDto;

public interface EmailService {
    void send(EmailNotificationDto notification);
}
