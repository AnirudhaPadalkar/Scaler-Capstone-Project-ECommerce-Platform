package com.example.ecomm.product.controller;

import com.example.ecomm.product.dto.CategoryResponseDto;
import com.example.ecomm.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> listAll() {
        return ResponseEntity.ok(categoryService.listAll());
    }
}
