package com.example.ecomm.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDto {
    private String accessToken;
    private String tokenType = "Bearer";
    private String userId;
    private String email;

    public AuthResponseDto(String accessToken, String userId, String email) {
        this.accessToken = accessToken;
        this.userId      = userId;
        this.email       = email;
    }
}
