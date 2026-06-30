package com.example.ecomm.payment.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class NetBankingPaymentStrategy implements PaymentStrategy {

    @Override
    public String initiatePayment(String orderId, BigDecimal amount, String currency) {
        String gatewayOrderId = "nb_" + UUID.randomUUID().toString().substring(0, 8);
        log.info("NetBanking payment initiated: {}", gatewayOrderId);
        return gatewayOrderId;
    }

    @Override
    public boolean verifyPayment(String gatewayOrderId, String gatewayPaymentId) {
        return gatewayPaymentId != null && !gatewayPaymentId.isBlank();
    }

    @Override
    public String getPaymentMethod() {
        return "netbanking";
    }
}
