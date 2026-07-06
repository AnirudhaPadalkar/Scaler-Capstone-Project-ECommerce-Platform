package com.example.ecomm.cart.service;

import com.example.ecomm.cart.dto.AddItemRequestDto;
import com.example.ecomm.cart.dto.CheckoutRequestDto;
import com.example.ecomm.cart.exception.CartException;
import com.example.ecomm.cart.model.Cart;
import com.example.ecomm.cart.model.CartItem;
import com.example.ecomm.cart.repository.CartRepository;
import com.example.ecomm.product.model.Category;
import com.example.ecomm.product.model.Product;
import com.example.ecomm.product.repository.ProductRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock private CartRepository            cartRepository;
    @Mock private ProductRepository         productRepository;
    @Mock private StringRedisTemplate       redisTemplate;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private ObjectMapper              objectMapper;
    @Mock private ValueOperations<String, String> valueOps;

    @InjectMocks private CartServiceImpl cartService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null);

        Category cat = Category.builder().name("Electronics").slug("electronics").build();
        testProduct = Product.builder()
                .name("Test Phone")
                .slug("test-phone")
                .price(new BigDecimal("999.00"))
                .stock(10)
                .active(true)
                .category(cat)
                .images(List.of())
                .build();
        testProduct.setId("prod-1");
    }

    // ── FIX 1: price from DB, not client ──────────────────────────────────

    @Test
    void addItem_usesPriceFromDB_notClientInput() {
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId("u1")).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AddItemRequestDto req = new AddItemRequestDto();
        req.setProductId("prod-1");
        req.setQuantity(1);
        // Note: no price set on request — client no longer sends it

        var result = cartService.addItem("u1", req);

        assertThat(result.getItems()).hasSize(1);
        // Price must come from DB (999.00), not from any client value
        assertThat(result.getItems().get(0).getPrice())
                .isEqualByComparingTo(new BigDecimal("999.00"));
    }

    @Test
    void addItem_productNotFound_throwsCartException() {
        when(productRepository.findById("bad-prod")).thenReturn(Optional.empty());

        AddItemRequestDto req = new AddItemRequestDto();
        req.setProductId("bad-prod");
        req.setQuantity(1);

        assertThatThrownBy(() -> cartService.addItem("u1", req))
                .isInstanceOf(CartException.class)
                .hasMessageContaining("Product not found");
    }

    // ── FIX 2: stock validation ────────────────────────────────────────────

    @Test
    void addItem_insufficientStock_throwsCartException() {
        testProduct.setStock(2);
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(testProduct));

        AddItemRequestDto req = new AddItemRequestDto();
        req.setProductId("prod-1");
        req.setQuantity(5); // requesting more than available

        assertThatThrownBy(() -> cartService.addItem("u1", req))
                .isInstanceOf(CartException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void addItem_exactStock_succeeds() {
        testProduct.setStock(3);
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(testProduct));
        when(cartRepository.findByUserId("u1")).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AddItemRequestDto req = new AddItemRequestDto();
        req.setProductId("prod-1");
        req.setQuantity(3); // exactly available stock

        assertThatCode(() -> cartService.addItem("u1", req)).doesNotThrowAnyException();
    }

    // ── FIX 3: checkout total returns correct value after cart cleared ─────

    @Test
    void checkout_returnsCorrectTotalAfterCartCleared() throws Exception {
        Cart cart = Cart.builder().userId("u1").build();
        cart.addItem(CartItem.builder()
                .productId("prod-1").productName("Phone")
                .price(new BigDecimal("999.00")).quantity(2)
                .build());

        // Total before clear = 999.00 * 2 = 1998.00
        BigDecimal expectedTotal = new BigDecimal("1998.00");

        when(cartRepository.findByUserId("u1")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        CheckoutRequestDto req = new CheckoutRequestDto();
        req.setPaymentMethod("card");
        CheckoutRequestDto.DeliveryAddress addr = new CheckoutRequestDto.DeliveryAddress();
        addr.setLine1("1 Main"); addr.setCity("Pune");
        addr.setState("MH"); addr.setPincode("411001");
        req.setDeliveryAddress(addr);

        var result = cartService.checkout("u1", req);

        // FIX verified: total is correct even though cart was cleared
        assertThat(result.getTotal()).isEqualByComparingTo(expectedTotal);
        assertThat(result.getOrderId()).isNotNull();
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void checkout_emptyCart_throwsCartException() {
        Cart emptyCart = Cart.builder().userId("u1").build();
        when(cartRepository.findByUserId("u1")).thenReturn(Optional.of(emptyCart));

        CheckoutRequestDto req = new CheckoutRequestDto();
        req.setPaymentMethod("card");
        CheckoutRequestDto.DeliveryAddress addr = new CheckoutRequestDto.DeliveryAddress();
        addr.setLine1("1 Main"); addr.setCity("Pune");
        addr.setState("MH"); addr.setPincode("411001");
        req.setDeliveryAddress(addr);

        assertThatThrownBy(() -> cartService.checkout("u1", req))
                .isInstanceOf(CartException.class)
                .hasMessageContaining("empty");
    }
}
