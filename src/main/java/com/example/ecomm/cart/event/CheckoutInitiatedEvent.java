package com.example.ecomm.cart.event;

import com.example.ecomm.cart.model.CartItem;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class CheckoutInitiatedEvent extends ApplicationEvent {

    private final String        orderId;
    private final String        userId;
    private final List<CartItem> items;
    private final BigDecimal    total;
    private final String        deliveryAddress;
    private final String        paymentMethod;

    public CheckoutInitiatedEvent(Object source, String orderId, String userId,
                                   List<CartItem> items, BigDecimal total,
                                   String deliveryAddress, String paymentMethod) {
        super(source);
        this.orderId         = orderId;
        this.userId          = userId;
        this.items           = items;
        this.total           = total;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod   = paymentMethod;
    }
}
