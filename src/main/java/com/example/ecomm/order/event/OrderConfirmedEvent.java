package com.example.ecomm.order.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class OrderConfirmedEvent extends ApplicationEvent {

    private final String     orderId;
    private final String     userId;
    private final BigDecimal total;

    public OrderConfirmedEvent(Object source, String orderId, String userId, BigDecimal total) {
        super(source);
        this.orderId = orderId;
        this.userId  = userId;
        this.total   = total;
    }
}
