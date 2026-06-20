package com.example.ecomm.user.service;

import com.example.ecomm.shared.config.JwtService;
import com.example.ecomm.user.dto.*;
import com.example.ecomm.user.event.PasswordResetRequestedEvent;
import com.example.ecomm.user.event.UserRegisteredEvent;
import com.example.ecomm.user.exception.InvalidTokenException;
import com.example.ecomm.user.exception.UserAlreadyExistsException;
import com.example.ecomm.user.exception.UserNotFoundException;
import com.example.ecomm.user.model.PasswordResetToken;
import com.example.ecomm.user.model.User;
import com.example.ecomm.user.model.UserSession;
import com.example.ecomm.user.repository.PasswordResetTokenRepository;
import com.example.ecomm.user.repository.UserRepository;
import com.example.ecomm.user.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository             userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final UserSessionRepository      sessionRepository;
    private final PasswordEncoder            passwordEncoder;
    private final JwtService                 jwtService;
    private final ApplicationEventPublisher  eventPublisher;

    @Value("${app.password-reset.expiration-minutes}")
    private int resetExpiryMinutes;

    // ── Register ──────────────────────────────────────────────────────────
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

        eventPublisher.publishEvent(
                new UserRegisteredEvent(this, saved.getId(), saved.getEmail(), saved.getFirstName())
        );

        log.info("User registered: {}", saved.getId());
        return new RegisterResponseDto(saved.getId(), "Registration successful");
    }

    // ── Login ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponseDto login(LoginRequestDto request) {
        User user = userRepository
                .findByEmailAndActiveTrue(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String accessToken  = jwtService.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        sessionRepository.save(UserSession.builder()
                .user(user)
                .refreshToken(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build());

        log.info("User logged in: {}", user.getId());
        return new AuthResponseDto(accessToken, refreshToken, user.getId(), user.getEmail());
    }

    // ── Refresh ───────────────────────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponseDto refresh(RefreshTokenRequestDto request) {
        UserSession session = sessionRepository
                .findByRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired refresh token"));

        if (session.isExpired()) {
            sessionRepository.delete(session);
            throw new InvalidTokenException("Refresh token expired");
        }

        User user = session.getUser();

        // Rotate — delete old, issue new
        sessionRepository.delete(session);

        String newAccess  = jwtService.generateToken(user.getId(), user.getEmail());
        String newRefresh = jwtService.generateRefreshToken(user.getId(), user.getEmail());

        sessionRepository.save(UserSession.builder()
                .user(user)
                .refreshToken(newRefresh)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build());

        return new AuthResponseDto(newAccess, newRefresh, user.getId(), user.getEmail());
    }

    // ── Logout ────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void logout(String userId, String refreshToken) {
        sessionRepository.findByRefreshToken(refreshToken)
                .ifPresent(sessionRepository::delete);
        log.info("User logged out: {}", userId);
    }

    // ── Forgot Password ───────────────────────────────────────────────────
    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDto request) {
        // Always return success — prevent email enumeration
        userRepository.findByEmailAndActiveTrue(request.getEmail().toLowerCase())
                .ifPresent(user -> {
                    resetTokenRepository.invalidateAllForUser(user.getId());

                    String rawToken  = UUID.randomUUID().toString();
                    String tokenHash = sha256(rawToken);

                    resetTokenRepository.save(PasswordResetToken.builder()
                            .user(user)
                            .tokenHash(tokenHash)
                            .expiresAt(LocalDateTime.now().plusMinutes(resetExpiryMinutes))
                            .used(false)
                            .build());

                    eventPublisher.publishEvent(
                            new PasswordResetRequestedEvent(this, user.getEmail(), user.getFirstName(), rawToken)
                    );
                });

        log.info("Password reset requested for: {}", request.getEmail());
    }

    // ── Reset Password ────────────────────────────────────────────────────
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        String tokenHash = sha256(request.getToken());

        PasswordResetToken resetToken = resetTokenRepository
                .findByTokenHashAndUsedFalse(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        if (resetToken.isExpired()) {
            throw new InvalidTokenException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);

        // Invalidate all sessions
        sessionRepository.deleteAllByUserId(user.getId());

        log.info("Password reset completed for user: {}", user.getId());
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
