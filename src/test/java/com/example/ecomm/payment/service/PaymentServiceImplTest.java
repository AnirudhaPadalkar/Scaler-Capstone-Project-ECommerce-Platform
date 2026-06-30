package com.example.ecomm.payment.service;

import com.example.ecomm.order.model.Order;
import com.example.ecomm.order.model.OrderStatus;
import com.example.ecomm.order.repository.OrderRepository;
import com.example.ecomm.order.service.OrderServiceImpl;
import com.example.ecomm.payment.dto.ConfirmPaymentRequestDto;
import com.example.ecomm.payment.dto.InitiatePaymentRequestDto;
import com.example.ecomm.payment.exception.PaymentException;
import com.example.ecomm.payment.model.Payment;
import com.example.ecomm.payment.model.PaymentStatus;
import com.example.ecomm.payment.repository.PaymentRepository;
import com.example.ecomm.payment.strategy.PaymentStrategy;
import com.example.ecomm.payment.strategy.PaymentStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentRepository      paymentRepository;
    @Mock private OrderRepository        orderRepository;
    @Mock private PaymentStrategyFactory strategyFactory;
    @Mock private PaymentStrategy        mockStrategy;
    @Mock private OrderServiceImpl       orderService;

    @InjectMocks private PaymentServiceImpl paymentService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .userId("u1")
                .status(OrderStatus.PENDING)
                .total(new BigDecimal("999.00"))
                .paymentMethod("card")
                .build();
        testOrder.setId("order-1");
    }

    @Test
    void initiatePayment_success() {
        var req = new InitiatePaymentRequestDto();
        req.setOrderId("order-1");
        req.setAmount(new BigDecimal("999.00"));

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));
        when(strategyFactory.getStrategy("card")).thenReturn(mockStrategy);
        when(mockStrategy.initiatePayment(any(), any(), any())).thenReturn("gw_order_123");
        when(paymentRepository.save(any())).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId("pay-1");
            return p;
        });

        var result = paymentService.initiatePayment("u1", req);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getGatewayOrderId()).isEqualTo("gw_order_123");
        verify(strategyFactory).getStrategy("card");
    }

    @Test
    void initiatePayment_orderNotFound_throws() {
        when(orderRepository.findById(anyString())).thenReturn(Optional.empty());

        var req = new InitiatePaymentRequestDto();
        req.setOrderId("bad-order");
        req.setAmount(BigDecimal.TEN);

        assertThatThrownBy(() -> paymentService.initiatePayment("u1", req))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    void confirmPayment_verificationFailed_throws() {
        Payment pending = Payment.builder()
                .orderId("order-1").userId("u1")
                .amount(new BigDecimal("999")).currency("INR")
                .status(PaymentStatus.PENDING).gateway("card")
                .gatewayOrderId("gw_order_123")
                .build();

        when(paymentRepository.findByOrderIdAndStatus("order-1", PaymentStatus.PENDING))
                .thenReturn(Optional.of(pending));
        when(strategyFactory.getStrategy("card")).thenReturn(mockStrategy);
        when(mockStrategy.verifyPayment(any(), any())).thenReturn(false);
        when(paymentRepository.save(any())).thenReturn(pending);

        var req = new ConfirmPaymentRequestDto();
        req.setOrderId("order-1");
        req.setGatewayPaymentId("bad_pay_id");

        assertThatThrownBy(() -> paymentService.confirmPayment(req))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("verification failed");
    }

    @Test
    void confirmPayment_success_triggersOrderConfirmation() {
        Payment pending = Payment.builder()
                .orderId("order-1").userId("u1")
                .amount(new BigDecimal("999")).currency("INR")
                .status(PaymentStatus.PENDING).gateway("card")
                .gatewayOrderId("gw_order_123")
                .build();
        pending.setId("pay-1");

        when(paymentRepository.findByOrderIdAndStatus("order-1", PaymentStatus.PENDING))
                .thenReturn(Optional.of(pending));
        when(strategyFactory.getStrategy("card")).thenReturn(mockStrategy);
        when(mockStrategy.verifyPayment(any(), any())).thenReturn(true);
        when(paymentRepository.save(any())).thenReturn(pending);

        var req = new ConfirmPaymentRequestDto();
        req.setOrderId("order-1");
        req.setGatewayPaymentId("rzp_pay_valid");

        var result = paymentService.confirmPayment(req);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(orderService).confirmOrder("order-1");
    }
}
