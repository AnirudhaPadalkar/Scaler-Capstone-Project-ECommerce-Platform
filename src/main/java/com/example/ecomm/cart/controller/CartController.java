package com.example.ecomm.cart.controller;

import com.example.ecomm.cart.dto.*;
import com.example.ecomm.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponseDto> getCart(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponseDto> addItem(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody AddItemRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addItem(userId, request));
    }

    @PatchMapping("/items/{productId}")
    public ResponseEntity<CartResponseDto> updateItem(
            @AuthenticationPrincipal String userId,
            @PathVariable String productId,
            @Valid @RequestBody UpdateItemRequestDto request) {
        return ResponseEntity.ok(cartService.updateItem(userId, productId, request));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponseDto> removeItem(
            @AuthenticationPrincipal String userId,
            @PathVariable String productId) {
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponseDto> checkout(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CheckoutRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.checkout(userId, request));
    }
}
