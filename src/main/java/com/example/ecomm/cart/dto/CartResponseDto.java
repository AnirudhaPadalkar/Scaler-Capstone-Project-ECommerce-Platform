package com.example.ecomm.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CartResponseDto {
    private String          userId;
    private List<ItemDto>   items;
    private BigDecimal      total;

    @Data
    @AllArgsConstructor
    public static class ItemDto {
        private String     productId;
        private String     productName;
        private BigDecimal price;
        private int        quantity;
        private BigDecimal subtotal;
    }
}
