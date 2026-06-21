package com.example.ecomm.product.service;

import com.example.ecomm.product.dto.CategoryResponseDto;

import java.util.List;

public interface CategoryService {
    List<CategoryResponseDto> listAll();
}
