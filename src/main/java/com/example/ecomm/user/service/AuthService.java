package com.example.ecomm.user.service;

import com.example.ecomm.user.dto.*;

public interface AuthService {
    RegisterResponseDto register(RegisterRequestDto request);
    AuthResponseDto login(LoginRequestDto request);
    AuthResponseDto refresh(RefreshTokenRequestDto request);
    void logout(String userId, String refreshToken);
    void forgotPassword(ForgotPasswordRequestDto request);
    void resetPassword(ResetPasswordRequestDto request);
}
