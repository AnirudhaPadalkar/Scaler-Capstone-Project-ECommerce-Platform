package com.example.ecomm.user.service;

import com.example.ecomm.shared.config.JwtService;
import com.example.ecomm.user.dto.*;
import com.example.ecomm.user.exception.InvalidTokenException;
import com.example.ecomm.user.exception.UserAlreadyExistsException;
import com.example.ecomm.user.model.PasswordResetToken;
import com.example.ecomm.user.model.User;
import com.example.ecomm.user.model.UserSession;
import com.example.ecomm.user.repository.PasswordResetTokenRepository;
import com.example.ecomm.user.repository.UserRepository;
import com.example.ecomm.user.repository.UserSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository              userRepository;
    @Mock private PasswordResetTokenRepository resetTokenRepository;
    @Mock private UserSessionRepository       sessionRepository;
    @Mock private PasswordEncoder             passwordEncoder;
    @Mock private JwtService                  jwtService;
    @Mock private ApplicationEventPublisher   eventPublisher;

    @InjectMocks private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "resetExpiryMinutes", 30);

        testUser = User.builder()
                .email("test@example.com")
                .passwordHash("$2a$12$hashed")
                .firstName("Test")
                .lastName("User")
                .active(true)
                .build();
    }

    // ── Register ──────────────────────────────────────────────────────────

    @Test
    void register_success_publishesEvent() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(testUser);

        var req = new RegisterRequestDto();
        req.setEmail("test@example.com");
        req.setPassword("Password1");
        req.setFirstName("Test");
        req.setLastName("User");

        var result = authService.register(req);

        assertThat(result.getMessage()).isEqualTo("Registration successful");
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void register_duplicateEmail_throws409() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        var req = new RegisterRequestDto();
        req.setEmail("test@example.com");
        req.setPassword("Password1");
        req.setFirstName("Test");
        req.setLastName("User");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    // ── Login ─────────────────────────────────────────────────────────────

    @Test
    void login_success_returnsTokenPair() {
        when(userRepository.findByEmailAndActiveTrue(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(any(), any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(), any())).thenReturn("refresh-token");
        when(sessionRepository.save(any())).thenReturn(new UserSession());

        var req = new LoginRequestDto();
        req.setEmail("test@example.com");
        req.setPassword("Password1");

        var result = authService.login(req);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void login_wrongPassword_throwsBadCredentials() {
        when(userRepository.findByEmailAndActiveTrue(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        var req = new LoginRequestDto();
        req.setEmail("test@example.com");
        req.setPassword("wrong");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }

    // ── Forgot Password ───────────────────────────────────────────────────

    @Test
    void forgotPassword_unknownEmail_doesNotThrow() {
        when(userRepository.findByEmailAndActiveTrue(anyString())).thenReturn(Optional.empty());

        var req = new ForgotPasswordRequestDto();
        req.setEmail("ghost@example.com");

        assertThatCode(() -> authService.forgotPassword(req)).doesNotThrowAnyException();
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void forgotPassword_knownEmail_publishesEvent() {
        when(userRepository.findByEmailAndActiveTrue(anyString())).thenReturn(Optional.of(testUser));
        when(resetTokenRepository.save(any())).thenReturn(new PasswordResetToken());

        var req = new ForgotPasswordRequestDto();
        req.setEmail("test@example.com");

        authService.forgotPassword(req);

        verify(eventPublisher).publishEvent(any());
        verify(resetTokenRepository).save(any());
    }

    // ── Reset Password ────────────────────────────────────────────────────

    @Test
    void resetPassword_invalidToken_throwsInvalidToken() {
        when(resetTokenRepository.findByTokenHashAndUsedFalse(anyString()))
                .thenReturn(Optional.empty());

        var req = new ResetPasswordRequestDto();
        req.setToken("bad-token");
        req.setNewPassword("NewPass1");

        assertThatThrownBy(() -> authService.resetPassword(req))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void resetPassword_expiredToken_throwsInvalidToken() {
        PasswordResetToken expired = PasswordResetToken.builder()
                .user(testUser)
                .tokenHash("hash")
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();

        when(resetTokenRepository.findByTokenHashAndUsedFalse(anyString()))
                .thenReturn(Optional.of(expired));

        var req = new ResetPasswordRequestDto();
        req.setToken("expired-token");
        req.setNewPassword("NewPass1");

        assertThatThrownBy(() -> authService.resetPassword(req))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expired");
    }
}
