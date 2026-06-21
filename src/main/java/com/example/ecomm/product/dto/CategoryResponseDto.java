package com.example.ecomm.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryResponseDto {
    private String id;
    private String name;
    private String slug;
    private String parentId;
}
