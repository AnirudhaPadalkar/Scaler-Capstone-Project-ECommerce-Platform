package com.example.ecomm.order.service;

import com.example.ecomm.cart.event.CheckoutInitiatedEvent;
import com.example.ecomm.cart.model.CartItem;
import com.example.ecomm.order.dto.OrderResponseDto;
import com.example.ecomm.order.event.OrderConfirmedEvent;
import com.example.ecomm.order.exception.OrderNotFoundException;
import com.example.ecomm.order.model.Order;
import com.example.ecomm.order.model.OrderItem;
import com.example.ecomm.order.model.OrderStatus;
import com.example.ecomm.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository       orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Transactional
    public void onCheckoutInitiated(CheckoutInitiatedEvent event) {
        Order order = Order.builder()
                .userId(event.getUserId())
                .status(OrderStatus.PENDING)
                .total(event.getTotal())
                .deliveryAddress(event.getDeliveryAddress())
                .paymentMethod(event.getPaymentMethod())
                .build();

        order.setId(event.getOrderId());

        List<OrderItem> items = event.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .order(order)
                        .productId(cartItem.getProductId())
                        .productName(cartItem.getProductName())
                        .price(cartItem.getPrice())
                        .quantity(cartItem.getQuantity())
                        .build())
                .collect(Collectors.toList());

        order.setItems(items);
        orderRepository.save(order);
        log.info("Order created from checkout: {}", order.getId());
    }

    @Transactional
    public void confirmOrder(String orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            eventPublisher.publishEvent(
                    new OrderConfirmedEvent(this, order.getId(), order.getUserId(), order.getTotal())
            );
            log.info("Order confirmed: {}", orderId);
        });
    }

    @Override
    public OrderResponseDto getById(String orderId, String userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
        return toDto(order);
    }

    @Override
    public List<OrderResponseDto> listByUser(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    private OrderResponseDto toDto(Order o) {
        return OrderResponseDto.builder()
                .id(o.getId())
                .userId(o.getUserId())
                .status(o.getStatus())
                .total(o.getTotal())
                .deliveryAddress(o.getDeliveryAddress())
                .paymentMethod(o.getPaymentMethod())
                .createdAt(o.getCreatedAt())
                .items(o.getItems().stream()
                        .map(i -> OrderResponseDto.ItemDto.builder()
                                .productId(i.getProductId())
                                .productName(i.getProductName())
                                .price(i.getPrice())
                                .quantity(i.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
