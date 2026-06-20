package com.example.ecomm.user.service;

import com.example.ecomm.user.dto.ProfileResponseDto;
import com.example.ecomm.user.dto.UpdateProfileRequestDto;

public interface ProfileService {
    ProfileResponseDto getProfile(String userId);
    ProfileResponseDto updateProfile(String userId, UpdateProfileRequestDto request);
}
