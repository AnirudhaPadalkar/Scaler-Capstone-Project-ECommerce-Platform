package com.example.ecomm.product.service;

import com.example.ecomm.product.dto.ProductResponseDto;

import java.util.List;

public interface ProductService {
    ProductResponseDto getBySlug(String slug);
    List<ProductResponseDto> getByCategory(String categoryId);
    List<ProductResponseDto> search(String query, String categoryId);
}
