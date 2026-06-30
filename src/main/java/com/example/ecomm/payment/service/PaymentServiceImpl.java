package com.example.ecomm.payment.service;

import com.example.ecomm.order.model.Order;
import com.example.ecomm.order.repository.OrderRepository;
import com.example.ecomm.order.service.OrderServiceImpl;
import com.example.ecomm.payment.dto.ConfirmPaymentRequestDto;
import com.example.ecomm.payment.dto.InitiatePaymentRequestDto;
import com.example.ecomm.payment.dto.PaymentResponseDto;
import com.example.ecomm.payment.exception.PaymentException;
import com.example.ecomm.payment.model.Payment;
import com.example.ecomm.payment.model.PaymentStatus;
import com.example.ecomm.payment.repository.PaymentRepository;
import com.example.ecomm.payment.strategy.PaymentStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository     paymentRepository;
    private final OrderRepository       orderRepository;
    private final PaymentStrategyFactory strategyFactory;
    private final OrderServiceImpl      orderService;

    @Override
    @Transactional
    public PaymentResponseDto initiatePayment(String userId, InitiatePaymentRequestDto request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new PaymentException("Order not found: " + request.getOrderId()));

        // Select payment strategy based on order's payment method
        var strategy       = strategyFactory.getStrategy(order.getPaymentMethod());
        String gwOrderId   = strategy.initiatePayment(order.getId(), request.getAmount(), "INR");

        Payment payment = Payment.builder()
                .orderId(order.getId())
                .userId(userId)
                .amount(request.getAmount())
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .gateway(order.getPaymentMethod())
                .gatewayOrderId(gwOrderId)
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment initiated: paymentId={} orderId={}", saved.getId(), order.getId());

        return toDto(saved);
    }

    @Override
    @Transactional
    public PaymentResponseDto confirmPayment(ConfirmPaymentRequestDto request) {
        Payment payment = paymentRepository
                .findByOrderIdAndStatus(request.getOrderId(), PaymentStatus.PENDING)
                .orElseThrow(() -> new PaymentException("No pending payment for order: " + request.getOrderId()));

        var strategy = strategyFactory.getStrategy(payment.getGateway());
        boolean verified = strategy.verifyPayment(payment.getGatewayOrderId(), request.getGatewayPaymentId());

        if (!verified) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new PaymentException("Payment verification failed");
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setGatewayPaymentId(request.getGatewayPaymentId());
        Payment saved = paymentRepository.save(payment);

        // Trigger order confirmation
        orderService.confirmOrder(request.getOrderId());

        log.info("Payment confirmed: paymentId={} orderId={}", saved.getId(), request.getOrderId());
        return toDto(saved);
    }

    @Override
    public PaymentResponseDto getByOrderId(String orderId, String userId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException("Payment not found for order: " + orderId));
        return toDto(payment);
    }

    private PaymentResponseDto toDto(Payment p) {
        return PaymentResponseDto.builder()
                .paymentId(p.getId())
                .orderId(p.getOrderId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus())
                .gatewayOrderId(p.getGatewayOrderId())
                .build();
    }
}
