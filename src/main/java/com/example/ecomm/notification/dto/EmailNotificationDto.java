package com.example.ecomm.notification.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailNotificationDto {
    private String to;
    private String subject;
    private String body;
}
