package com.example.ecomm.user.service;

import com.example.ecomm.user.dto.AuthResponseDto;
import com.example.ecomm.user.dto.LoginRequestDto;
import com.example.ecomm.user.dto.RegisterRequestDto;
import com.example.ecomm.user.dto.RegisterResponseDto;

public interface AuthService {
    RegisterResponseDto register(RegisterRequestDto request);
    AuthResponseDto login(LoginRequestDto request);
}
