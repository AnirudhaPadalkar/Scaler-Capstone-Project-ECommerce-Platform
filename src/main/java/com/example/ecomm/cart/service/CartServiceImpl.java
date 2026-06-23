package com.example.ecomm.cart.service;

import com.example.ecomm.cart.dto.*;
import com.example.ecomm.cart.event.CheckoutInitiatedEvent;
import com.example.ecomm.cart.exception.CartException;
import com.example.ecomm.cart.model.Cart;
import com.example.ecomm.cart.model.CartItem;
import com.example.ecomm.cart.repository.CartRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository       cartRepository;
    private final StringRedisTemplate  redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper         objectMapper;

    private static final String CACHE_PREFIX = "cart:";
    private static final Duration CACHE_TTL  = Duration.ofHours(24);

    @Override
    public CartResponseDto getCart(String userId) {
        try {
            String cached = redisTemplate.opsForValue().get(CACHE_PREFIX + userId);
            if (cached != null) {
                return objectMapper.readValue(cached, CartResponseDto.class);
            }
        } catch (Exception e) {
            log.warn("Cache read failed for user {}", userId);
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElse(Cart.builder().userId(userId).build());

        CartResponseDto dto = toDto(cart);
        cacheCart(userId, dto);
        return dto;
    }

    @Override
    public CartResponseDto addItem(String userId, AddItemRequestDto request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElse(Cart.builder().userId(userId).updatedAt(LocalDateTime.now()).build());

        cart.addItem(CartItem.builder()
                .productId(request.getProductId())
                .productName(request.getProductName())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build());

        Cart saved = cartRepository.save(cart);
        invalidateCache(userId);
        return toDto(saved);
    }

    @Override
    public CartResponseDto updateItem(String userId, String productId, UpdateItemRequestDto request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartException("Cart not found"));

        cart.updateItemQuantity(productId, request.getQuantity());
        Cart saved = cartRepository.save(cart);
        invalidateCache(userId);
        return toDto(saved);
    }

    @Override
    public CartResponseDto removeItem(String userId, String productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartException("Cart not found"));

        cart.removeItem(productId);
        Cart saved = cartRepository.save(cart);
        invalidateCache(userId);
        return toDto(saved);
    }

    @Override
    public CheckoutResponseDto checkout(String userId, CheckoutRequestDto request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new CartException("Cart is empty");
        }

        String orderId       = UUID.randomUUID().toString();
        String addressJson   = serializeAddress(request.getDeliveryAddress());

        eventPublisher.publishEvent(new CheckoutInitiatedEvent(
                this, orderId, userId,
                cart.getItems(), cart.getTotal(),
                addressJson, request.getPaymentMethod()
        ));

        cart.clear();
        cartRepository.save(cart);
        invalidateCache(userId);

        log.info("Checkout initiated: orderId={} userId={}", orderId, userId);
        return new CheckoutResponseDto(orderId, cart.getTotal(), "Order placed. Payment processing.");
    }

    private CartResponseDto toDto(Cart cart) {
        List<CartResponseDto.ItemDto> items = cart.getItems().stream()
                .map(i -> new CartResponseDto.ItemDto(
                        i.getProductId(), i.getProductName(),
                        i.getPrice(), i.getQuantity(), i.getSubtotal()))
                .collect(Collectors.toList());

        return CartResponseDto.builder()
                .userId(cart.getUserId())
                .items(items)
                .total(cart.getTotal())
                .build();
    }

    private void cacheCart(String userId, CartResponseDto dto) {
        try {
            redisTemplate.opsForValue().set(
                    CACHE_PREFIX + userId,
                    objectMapper.writeValueAsString(dto),
                    CACHE_TTL);
        } catch (Exception e) {
            log.warn("Cache write failed for user {}", userId);
        }
    }

    private void invalidateCache(String userId) {
        redisTemplate.delete(CACHE_PREFIX + userId);
    }

    private String serializeAddress(CheckoutRequestDto.DeliveryAddress address) {
        try {
            return objectMapper.writeValueAsString(address);
        } catch (Exception e) {
            return "{}";
        }
    }
}
