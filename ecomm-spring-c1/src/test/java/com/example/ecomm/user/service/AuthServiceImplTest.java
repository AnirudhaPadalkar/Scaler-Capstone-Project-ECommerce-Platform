package com.example.ecomm.user.service;

import com.example.ecomm.shared.config.JwtService;
import com.example.ecomm.user.dto.LoginRequestDto;
import com.example.ecomm.user.dto.RegisterRequestDto;
import com.example.ecomm.user.exception.UserAlreadyExistsException;
import com.example.ecomm.user.model.User;
import com.example.ecomm.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthServiceImpl authService;

    private RegisterRequestDto registerRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password1");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");

        existingUser = User.builder()
                .email("test@example.com")
                .passwordHash("$2a$12$hashedpassword")
                .firstName("Test")
                .lastName("User")
                .active(true)
                .build();
    }

    @Test
    void register_success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        var result = authService.register(registerRequest);

        assertThat(result.getMessage()).isEqualTo("Registration successful");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("test@example.com");
    }

    @Test
    void login_success() {
        when(userRepository.findByEmailAndActiveTrue(anyString()))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(any(), any())).thenReturn("jwt-token");

        var result = authService.login(new LoginRequestDto() {{
            setEmail("test@example.com");
            setPassword("Password1");
        }});

        assertThat(result.getAccessToken()).isEqualTo("jwt-token");
    }

    @Test
    void login_unknownEmail_throwsBadCredentials() {
        when(userRepository.findByEmailAndActiveTrue(anyString()))
                .thenReturn(Optional.empty());

        var req = new LoginRequestDto();
        req.setEmail("unknown@example.com");
        req.setPassword("pass");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_wrongPassword_throwsBadCredentials() {
        when(userRepository.findByEmailAndActiveTrue(anyString()))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        var req = new LoginRequestDto();
        req.setEmail("test@example.com");
        req.setPassword("wrongpass");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }
}
