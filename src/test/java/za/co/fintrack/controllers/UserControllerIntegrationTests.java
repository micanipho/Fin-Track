// Java
package za.co.fintrack.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import za.co.fintrack.auth.SignUpRequest;
import za.co.fintrack.config.TestEmailConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@Import(TestEmailConfig.class)
class UserControllerIntegrationTests {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    UserControllerIntegrationTests(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    void testThatRegisteredUserSuccessfullyReturnsHttp201Created() throws Exception {
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .username("testUser")
                .email("test@email.com")
                .password("MyStr0ng!T3st123")
                .confirmPassword("MyStr0ng!T3st123")
                .build();

        String userJson = objectMapper.writeValueAsString(signUpRequest);

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
        ).andExpect(status().isCreated());
    }

    @Test
    void testThatRegisteredUserSuccessfullyReturnsSavedUser() throws Exception {
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .username("testUser")
                .email("test@email.com")
                .password("MyStr0ng!T3st123")
                .confirmPassword("MyStr0ng!T3st123")
                .build();

        String userJson = objectMapper.writeValueAsString(signUpRequest);

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
        ).andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.message").value("Registration successful. Please check your email to verify your account."));
    }
}