package com.example.ecomm.cart.service;

import com.example.ecomm.cart.dto.*;

public interface CartService {
    CartResponseDto getCart(String userId);
    CartResponseDto addItem(String userId, AddItemRequestDto request);
    CartResponseDto updateItem(String userId, String productId, UpdateItemRequestDto request);
    CartResponseDto removeItem(String userId, String productId);
    CheckoutResponseDto checkout(String userId, CheckoutRequestDto request);
}
