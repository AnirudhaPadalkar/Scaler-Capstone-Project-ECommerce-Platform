package com.example.ecomm.order.dto;

import com.example.ecomm.order.model.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponseDto {
    private String      id;
    private String      userId;
    private OrderStatus status;
    private BigDecimal  total;
    private String      deliveryAddress;
    private String      paymentMethod;
    private LocalDateTime createdAt;
    private List<ItemDto> items;

    @Data
    @Builder
    public static class ItemDto {
        private String     productId;
        private String     productName;
        private BigDecimal price;
        private int        quantity;
    }
}
