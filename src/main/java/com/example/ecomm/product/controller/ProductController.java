package com.example.ecomm.product.controller;

import com.example.ecomm.product.dto.ProductResponseDto;
import com.example.ecomm.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDto>> search(
            @RequestParam String q,
            @RequestParam(required = false) String categoryId) {
        return ResponseEntity.ok(productService.search(q, categoryId));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> listByCategory(
            @RequestParam String categoryId) {
        return ResponseEntity.ok(productService.getByCategory(categoryId));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductResponseDto> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getBySlug(slug));
    }
}
