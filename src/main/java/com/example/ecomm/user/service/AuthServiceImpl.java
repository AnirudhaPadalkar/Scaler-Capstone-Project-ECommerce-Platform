package com.example.ecomm.user.service;

import com.example.ecomm.user.dto.AuthResponseDto;
import com.example.ecomm.user.dto.LoginRequestDto;
import com.example.ecomm.user.dto.RegisterRequestDto;
import com.example.ecomm.user.dto.RegisterResponseDto;
import com.example.ecomm.user.exception.UserAlreadyExistsException;
import com.example.ecomm.user.exception.UserNotFoundException;
import com.example.ecomm.user.model.User;
import com.example.ecomm.user.repository.UserRepository;
import com.example.ecomm.shared.config.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public RegisterResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("User registered: {}", saved.getId());

        return new RegisterResponseDto(saved.getId(), "Registration successful");
    }

    @Override
    public AuthResponseDto login(LoginRequestDto request) {
        User user = userRepository
                .findByEmailAndActiveTrue(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        log.info("User logged in: {}", user.getId());

        return new AuthResponseDto(token, user.getId(), user.getEmail());
    }
}
