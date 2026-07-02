package com.example.ecomm.notification.listener;

import com.example.ecomm.notification.service.NotificationService;
import com.example.ecomm.order.event.OrderConfirmedEvent;
import com.example.ecomm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

    private final NotificationService notificationService;
    private final UserRepository      userRepository;

    @Async
    @EventListener
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        try {
            userRepository.findById(event.getUserId()).ifPresent(user ->
                    notificationService.sendOrderConfirmation(
                            user.getEmail(),
                            user.getFirstName(),
                            event.getOrderId(),
                            event.getTotal().toPlainString()));
        } catch (Exception e) {
            log.error("Failed to send order confirmation for orderId={}: {}",
                    event.getOrderId(), e.getMessage());
        }
    }
}
