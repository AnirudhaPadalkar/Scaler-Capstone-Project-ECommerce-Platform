package com.example.ecomm.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * C8 fix: price removed — fetched from ProductRepository in CartServiceImpl.
 * Clients no longer supply price; doing so would allow price manipulation.
 */
@Data
public class AddItemRequestDto {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
