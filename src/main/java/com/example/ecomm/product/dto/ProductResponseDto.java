package com.example.ecomm.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ProductResponseDto {
    private String id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private int stock;
    private String categoryId;
    private String categoryName;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}
