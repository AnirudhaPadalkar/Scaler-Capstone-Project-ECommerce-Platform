package com.example.ecomm.order.controller;

import com.example.ecomm.order.dto.OrderResponseDto;
import com.example.ecomm.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> listOrders(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(orderService.listByUser(userId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(
            @AuthenticationPrincipal String userId,
            @PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getById(orderId, userId));
    }
}
