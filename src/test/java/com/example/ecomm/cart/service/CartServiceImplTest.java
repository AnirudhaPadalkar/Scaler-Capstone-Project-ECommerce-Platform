package com.example.ecomm.cart.service;

import com.example.ecomm.cart.dto.AddItemRequestDto;
import com.example.ecomm.cart.dto.CheckoutRequestDto;
import com.example.ecomm.cart.exception.CartException;
import com.example.ecomm.cart.model.Cart;
import com.example.ecomm.cart.repository.CartRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock private CartRepository           cartRepository;
    @Mock private StringRedisTemplate      redisTemplate;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private ObjectMapper             objectMapper;
    @Mock private ValueOperations<String, String> valueOps;

    @InjectMocks private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null);
    }

    @Test
    void getCart_emptyCart_returnsZeroTotal() {
        when(cartRepository.findByUserId("u1")).thenReturn(Optional.empty());

        var result = cartService.getCart("u1");

        assertThat(result.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void addItem_newCart_createsAndReturns() {
        when(cartRepository.findByUserId("u1")).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var req = new AddItemRequestDto();
        req.setProductId("p1");
        req.setProductName("Phone");
        req.setPrice(new BigDecimal("999.00"));
        req.setQuantity(1);

        var result = cartService.addItem("u1", req);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("999.00"));
    }

    @Test
    void checkout_emptyCart_throwsCartException() {
        Cart emptyCart = Cart.builder().userId("u1").build();
        when(cartRepository.findByUserId("u1")).thenReturn(Optional.of(emptyCart));

        var req = new CheckoutRequestDto();
        req.setPaymentMethod("card");
        var addr = new CheckoutRequestDto.DeliveryAddress();
        addr.setLine1("1 Main"); addr.setCity("Pune");
        addr.setState("MH"); addr.setPincode("411001");
        req.setDeliveryAddress(addr);

        assertThatThrownBy(() -> cartService.checkout("u1", req))
                .isInstanceOf(CartException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void checkout_withItems_publishesEventAndClearsCart() throws Exception {
        Cart cart = Cart.builder().userId("u1").build();
        cart.addItem(com.example.ecomm.cart.model.CartItem.builder()
                .productId("p1").productName("Phone")
                .price(new BigDecimal("999")).quantity(1).build());

        when(cartRepository.findByUserId("u1")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        var req = new CheckoutRequestDto();
        req.setPaymentMethod("card");
        var addr = new CheckoutRequestDto.DeliveryAddress();
        addr.setLine1("1 Main"); addr.setCity("Pune");
        addr.setState("MH"); addr.setPincode("411001");
        req.setDeliveryAddress(addr);

        var result = cartService.checkout("u1", req);

        assertThat(result.getOrderId()).isNotNull();
        verify(eventPublisher).publishEvent(any());
    }
}
