package com.example.ecomm.user.controller;

import com.example.ecomm.user.dto.ProfileResponseDto;
import com.example.ecomm.user.dto.UpdateProfileRequestDto;
import com.example.ecomm.user.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponseDto> getProfile(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @PatchMapping
    public ResponseEntity<ProfileResponseDto> updateProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody UpdateProfileRequestDto request) {
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }
}
