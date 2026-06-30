package com.example.ecomm.payment.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class RazorpayPaymentStrategy implements PaymentStrategy {

    @Value("${app.payment.razorpay.key-id:}")
    private String keyId;

    @Value("${app.payment.razorpay.key-secret:}")
    private String keySecret;

    @Override
    public String initiatePayment(String orderId, BigDecimal amount, String currency) {
        // In production: call Razorpay Orders API
        // razorpayClient.orders.create({ amount: amount * 100, currency, receipt: orderId })
        if (keyId.isBlank()) {
            String devOrderId = "dev_rzp_" + UUID.randomUUID().toString().substring(0, 8);
            log.info("[DEV] Razorpay order created: {}", devOrderId);
            return devOrderId;
        }
        // TODO: integrate Razorpay SDK in production
        return "rzp_order_" + orderId.substring(0, 8);
    }

    @Override
    public boolean verifyPayment(String gatewayOrderId, String gatewayPaymentId) {
        // In production: verify HMAC signature from Razorpay webhook
        if (keySecret.isBlank()) {
            log.info("[DEV] Payment verification bypassed for: {}", gatewayPaymentId);
            return true;
        }
        // TODO: HMAC verification in production
        return gatewayPaymentId != null && !gatewayPaymentId.isBlank();
    }

    @Override
    public String getPaymentMethod() {
        return "card";
    }
}
