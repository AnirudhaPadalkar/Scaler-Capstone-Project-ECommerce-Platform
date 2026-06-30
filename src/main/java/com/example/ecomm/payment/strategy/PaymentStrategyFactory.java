package com.example.ecomm.payment.strategy;

import com.example.ecomm.payment.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory Pattern — resolves the correct PaymentStrategy at runtime
 * based on the payment method string. Spring injects all PaymentStrategy
 * implementations automatically via the List constructor.
 */
@Component
@RequiredArgsConstructor
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategies;

    public PaymentStrategyFactory(List<PaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(PaymentStrategy::getPaymentMethod, Function.identity()));
    }

    public PaymentStrategy getStrategy(String paymentMethod) {
        PaymentStrategy strategy = strategies.get(paymentMethod);
        if (strategy == null) {
            throw new PaymentException("Unsupported payment method: " + paymentMethod);
        }
        return strategy;
    }
}
