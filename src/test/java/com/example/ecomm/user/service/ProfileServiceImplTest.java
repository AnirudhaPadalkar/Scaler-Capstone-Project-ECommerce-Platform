package com.example.ecomm.user.service;

import com.example.ecomm.user.dto.UpdateProfileRequestDto;
import com.example.ecomm.user.exception.UserNotFoundException;
import com.example.ecomm.user.model.User;
import com.example.ecomm.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private ProfileServiceImpl profileService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .active(true)
                .build();
    }

    @Test
    void getProfile_returnsCorrectData() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));

        var result = profileService.getProfile("user-1");

        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("Test");
    }

    @Test
    void getProfile_unknownUser_throwsNotFound() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfile("bad-id"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateProfile_updatesOnlyProvidedFields() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        var req = new UpdateProfileRequestDto();
        req.setFirstName("Updated");

        var result = profileService.updateProfile("user-1", req);

        verify(userRepository).save(argThat(u -> u.getFirstName().equals("Updated")));
    }
}
