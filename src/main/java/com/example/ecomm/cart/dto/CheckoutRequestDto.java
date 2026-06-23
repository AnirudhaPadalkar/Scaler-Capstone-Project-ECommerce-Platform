package com.example.ecomm.cart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CheckoutRequestDto {

    @Valid
    @NotNull
    private DeliveryAddress deliveryAddress;

    @NotBlank
    @Pattern(regexp = "card|netbanking|upi", message = "Payment method must be card, netbanking, or upi")
    private String paymentMethod;

    @Data
    public static class DeliveryAddress {
        @NotBlank private String line1;
        @NotBlank private String city;
        @NotBlank private String state;
        @NotBlank private String pincode;
    }
}
