package com.example.ecomm.user.service;

import com.example.ecomm.user.dto.ProfileResponseDto;
import com.example.ecomm.user.dto.UpdateProfileRequestDto;
import com.example.ecomm.user.exception.UserNotFoundException;
import com.example.ecomm.user.model.User;
import com.example.ecomm.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;

    @Override
    public ProfileResponseDto getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toDto(user);
    }

    @Override
    @Transactional
    public ProfileResponseDto updateProfile(String userId, UpdateProfileRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName()  != null) user.setLastName(request.getLastName());

        User updated = userRepository.save(user);
        log.info("Profile updated for user: {}", userId);
        return toDto(updated);
    }

    private ProfileResponseDto toDto(User user) {
        return new ProfileResponseDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreatedAt()
        );
    }
}
