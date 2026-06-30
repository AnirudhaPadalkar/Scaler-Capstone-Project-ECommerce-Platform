package com.example.ecomm.order.service;

import com.example.ecomm.order.exception.OrderNotFoundException;
import com.example.ecomm.order.model.Order;
import com.example.ecomm.order.model.OrderStatus;
import com.example.ecomm.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository        orderRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private OrderServiceImpl orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .userId("u1")
                .status(OrderStatus.PENDING)
                .total(new BigDecimal("999.00"))
                .paymentMethod("card")
                .deliveryAddress("{\"city\":\"Pune\"}")
                .items(List.of())
                .build();
        testOrder.setId("order-1");
    }

    @Test
    void getById_returnsOrder() {
        when(orderRepository.findByIdAndUserId("order-1", "u1"))
                .thenReturn(Optional.of(testOrder));

        var result = orderService.getById("order-1", "u1");

        assertThat(result.getId()).isEqualTo("order-1");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void getById_wrongUser_throwsNotFound() {
        when(orderRepository.findByIdAndUserId("order-1", "u2"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getById("order-1", "u2"))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void confirmOrder_updatesStatusAndPublishesEvent() {
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any())).thenReturn(testOrder);

        orderService.confirmOrder("order-1");

        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.CONFIRMED));
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void listByUser_returnsAllOrders() {
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("u1"))
                .thenReturn(List.of(testOrder));

        var results = orderService.listByUser("u1");

        assertThat(results).hasSize(1);
    }
}
