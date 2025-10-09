package za.co.fintrack.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import za.co.fintrack.auth.LoginRequest;
import za.co.fintrack.auth.SignUpRequest;
import za.co.fintrack.config.TestEmailConfig;
import za.co.fintrack.models.dtos.ForgotPasswordDto;
import za.co.fintrack.models.entities.User;
import za.co.fintrack.repositories.UserRepository;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(locations = "classpath:application.properties")
@Import(TestEmailConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Clean up any existing test data
        userRepository.deleteAll();
    }

    @Test
    void register_ShouldCreateUser_WhenValidDataProvided() throws Exception {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .username("testuser" + uniqueId)
                .email("test" + uniqueId + "@example.com")
                .password("MyStr0ng!T3st123")
                .confirmPassword("MyStr0ng!T3st123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andDo(result -> {
                    System.out.println("Response Status: " + result.getResponse().getStatus());
                    System.out.println("Response Body: " + result.getResponse().getContentAsString());
                })
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful. Please check your email to verify your account."))
                .andExpect(jsonPath("$.userId").exists());

        // Verify user was created in database
        User createdUser = userRepository.findByEmail("test" + uniqueId + "@example.com").orElse(null);
        assert createdUser != null;
        assert createdUser.getUsername().equals("testuser" + uniqueId);
        assert !createdUser.isEmailVerified(); // Should be false initially
    }

    @Test
    void register_ShouldReturnError_WhenPasswordsDoNotMatch() throws Exception {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .username("testuser" + uniqueId)
                .email("test" + uniqueId + "@example.com")
                .password("MyStr0ng!T3st123")
                .confirmPassword("Different!P@ss456")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Passwords do not match"));
    }

    @Test
    void register_ShouldReturnError_WhenEmailAlreadyExists() throws Exception {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String testEmail = "test" + uniqueId + "@example.com";

        // Create user first
        User existingUser = User.builder()
                .username("existinguser" + uniqueId)
                .email(testEmail)
                .password("encodedPassword")
                .emailVerified(true)
                .build();
        userRepository.save(existingUser);

        SignUpRequest signUpRequest = SignUpRequest.builder()
                .username("newuser" + uniqueId)
                .email(testEmail)
                .password("MyStr0ng!T3st123")
                .confirmPassword("MyStr0ng!T3st123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email is already registered"));
    }

    @Test
    void forgotPassword_ShouldReturnSuccess_WhenEmailExists() throws Exception {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String testEmail = "test" + uniqueId + "@example.com";

        // Create user first
        User user = User.builder()
                .username("testuser" + uniqueId)
                .email(testEmail)
                .password("encodedPassword")
                .emailVerified(true)
                .build();
        userRepository.save(user);

        ForgotPasswordDto forgotPasswordDto = ForgotPasswordDto.builder()
                .email(testEmail)
                .build();

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotPasswordDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If an account with that email exists, a password reset link has been sent."));
    }

    @Test
    void forgotPassword_ShouldReturnSuccess_WhenEmailDoesNotExist() throws Exception {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        ForgotPasswordDto forgotPasswordDto = ForgotPasswordDto.builder()
                .email("nonexistent" + uniqueId + "@example.com")
                .build();

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(forgotPasswordDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If an account with that email exists, a password reset link has been sent."));
    }

    @Test
    void logout_ShouldReturnSuccess_WhenValidTokenProvided() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer valid-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    void refresh_ShouldReturnError_WhenNoTokenProvided() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Refresh token is required"));
    }
}
