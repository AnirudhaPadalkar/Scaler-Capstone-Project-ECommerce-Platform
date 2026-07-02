package com.example.ecomm.notification.service;

import com.example.ecomm.notification.dto.EmailNotificationDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock  private EmailService emailService;
    @InjectMocks private NotificationService notificationService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationService, "baseUrl", "http://localhost:8080");
    }

    @Test
    void sendWelcome_callsEmailServiceWithCorrectSubject() {
        notificationService.sendWelcome("a@b.com", "Alice");

        ArgumentCaptor<EmailNotificationDto> captor = ArgumentCaptor.forClass(EmailNotificationDto.class);
        verify(emailService).send(captor.capture());

        assertThat(captor.getValue().getTo()).isEqualTo("a@b.com");
        assertThat(captor.getValue().getSubject()).contains("Alice");
    }

    @Test
    void sendPasswordReset_includesTokenInBody() {
        notificationService.sendPasswordReset("a@b.com", "Alice", "raw-token-123");

        ArgumentCaptor<EmailNotificationDto> captor = ArgumentCaptor.forClass(EmailNotificationDto.class);
        verify(emailService).send(captor.capture());

        assertThat(captor.getValue().getBody()).contains("raw-token-123");
        assertThat(captor.getValue().getSubject()).contains("Reset");
    }

    @Test
    void sendOrderConfirmation_includesOrderId() {
        notificationService.sendOrderConfirmation("a@b.com", "Alice", "order-uuid-1234", "999.00");

        ArgumentCaptor<EmailNotificationDto> captor = ArgumentCaptor.forClass(EmailNotificationDto.class);
        verify(emailService).send(captor.capture());

        assertThat(captor.getValue().getBody()).contains("ORDER-UUID");
        assertThat(captor.getValue().getBody()).contains("999.00");
    }
}
