package com.example.ecomm.payment.strategy;

import java.math.BigDecimal;

/**
 * Strategy Pattern — defines the contract for all payment gateway implementations.
 * Concrete strategies: RazorpayPaymentStrategy, NetBankingPaymentStrategy, UpiPaymentStrategy.
 * PaymentServiceImpl selects the correct strategy at runtime via PaymentStrategyFactory.
 */
public interface PaymentStrategy {

    /**
     * Initiate a payment with the gateway and return the gateway order ID.
     */
    String initiatePayment(String orderId, BigDecimal amount, String currency);

    /**
     * Verify that a gateway payment ID is valid for this gateway order.
     */
    boolean verifyPayment(String gatewayOrderId, String gatewayPaymentId);

    /**
     * Returns the payment method name this strategy handles.
     */
    String getPaymentMethod();
}
