package za.co.fintrack.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import za.co.fintrack.models.dtos.ChangePasswordDto;
import za.co.fintrack.models.dtos.UserProfileDto;
import za.co.fintrack.models.entities.User;
import za.co.fintrack.repositories.UserRepository;
import za.co.fintrack.services.EmailService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .emailVerified(false)
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getUserProfile_ShouldReturnUserProfile_WhenUserExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserProfileDto result = userService.getUserProfile(1L);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getFirstName(), result.getFirstName());
        assertEquals(testUser.getLastName(), result.getLastName());
    }

    @Test
    void getUserProfile_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.getUserProfile(1L));
    }

    @Test
    void changePassword_ShouldUpdatePassword_WhenCurrentPasswordIsCorrect() {
        // Given
        ChangePasswordDto changePasswordDto = ChangePasswordDto.builder()
                .currentPassword("currentPassword")
                .newPassword("MyStr0ng!P@ssw0rd")
                .confirmPassword("MyStr0ng!P@ssw0rd")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("MyStr0ng!P@ssw0rd")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.changePassword(1L, changePasswordDto);

        // Then
        verify(passwordEncoder).encode("MyStr0ng!P@ssw0rd");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_ShouldThrowException_WhenCurrentPasswordIsIncorrect() {
        // Given
        ChangePasswordDto changePasswordDto = ChangePasswordDto.builder()
                .currentPassword("wrongPassword")
                .newPassword("MyStr0ng!P@ssw0rd")
                .confirmPassword("MyStr0ng!P@ssw0rd")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.changePassword(1L, changePasswordDto));
    }

    @Test
    void changePassword_ShouldThrowException_WhenPasswordsDoNotMatch() {
        // Given
        ChangePasswordDto changePasswordDto = ChangePasswordDto.builder()
                .currentPassword("currentPassword")
                .newPassword("MyStr0ng!P@ssw0rd")
                .confirmPassword("Different!P@ssw0rd123")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPassword", "encodedPassword")).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.changePassword(1L, changePasswordDto));
    }

    @Test
    void initiatePasswordReset_ShouldSendEmail_WhenUserExists() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.initiatePasswordReset("test@example.com");

        // Then
        verify(userRepository).save(any(User.class));
        verify(emailService).sendPasswordResetEmail(eq("test@example.com"), anyString(), eq("testuser"));
    }

    @Test
    void initiatePasswordReset_ShouldNotThrowException_WhenUserDoesNotExist() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertDoesNotThrow(() -> userService.initiatePasswordReset("nonexistent@example.com"));
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    void recordLoginAttempt_ShouldResetFailedAttempts_WhenLoginIsSuccessful() {
        // Given
        testUser.setFailedLoginAttempts(3);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.recordLoginAttempt(1L, true);

        // Then
        verify(userRepository).save(argThat(user ->
            user.getFailedLoginAttempts() == 0 &&
            user.getLastLogin() != null &&
            user.getAccountLockedUntil() == null
        ));
    }

    @Test
    void recordLoginAttempt_ShouldIncrementFailedAttempts_WhenLoginFails() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.recordLoginAttempt(1L, false);

        // Then
        verify(userRepository).save(argThat(user -> user.getFailedLoginAttempts() == 1));
    }

    @Test
    void recordLoginAttempt_ShouldLockAccount_WhenMaxAttemptsReached() {
        // Given
        testUser.setFailedLoginAttempts(4); // One less than max (5)
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.recordLoginAttempt(1L, false);

        // Then
        verify(userRepository).save(argThat(user ->
            user.getFailedLoginAttempts() == 5 &&
            user.getAccountLockedUntil() != null
        ));
    }

    @Test
    void isAccountLocked_ShouldReturnTrue_WhenAccountIsLocked() {
        // Given
        testUser.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.isAccountLocked(1L);

        // Then
        assertTrue(result);
    }

    @Test
    void isAccountLocked_ShouldReturnFalse_WhenAccountIsNotLocked() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.isAccountLocked(1L);

        // Then
        assertFalse(result);
    }
}
