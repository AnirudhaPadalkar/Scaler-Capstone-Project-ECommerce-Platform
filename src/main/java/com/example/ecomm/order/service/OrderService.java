package com.example.ecomm.order.service;

import com.example.ecomm.order.dto.OrderResponseDto;

import java.util.List;

public interface OrderService {
    OrderResponseDto getById(String orderId, String userId);
    List<OrderResponseDto> listByUser(String userId);
}
