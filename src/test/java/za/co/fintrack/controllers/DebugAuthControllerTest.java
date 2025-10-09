package za.co.fintrack.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import za.co.fintrack.auth.SignUpRequest;
import za.co.fintrack.config.TestEmailConfig;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(locations = "classpath:application.properties")
@Import(TestEmailConfig.class)
class DebugAuthControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void debugRegistrationError() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        SignUpRequest signUpRequest = SignUpRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("MyStr0ng!T3st123")
                .confirmPassword("MyStr0ng!T3st123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andReturn();

        System.out.println("=== DEBUG REGISTRATION RESPONSE ===");
        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Content Type: " + result.getResponse().getContentType());
        System.out.println("Response Body: " + result.getResponse().getContentAsString());
        System.out.println("Headers: " + result.getResponse().getHeaderNames());

        if (result.getResolvedException() != null) {
            System.out.println("Exception: " + result.getResolvedException().getMessage());
            result.getResolvedException().printStackTrace();
        }
    }
}
