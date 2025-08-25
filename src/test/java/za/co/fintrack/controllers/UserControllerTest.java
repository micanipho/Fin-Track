// Java
package za.co.fintrack.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import za.co.fintrack.enums.Role;
import za.co.fintrack.models.entities.User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class UserControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    UserControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    void testThatRegisteredUserSuccessfullyReturnsHttp201Created() throws Exception {
        User testUser = User.builder()
                .role(Role.USER)
                .email("test@email.com")
                .username("testUser")
                .password("pass")
                .build();

        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(
                post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
        ).andExpect(status().isCreated());
    }

    @Test
    void testThatRegisteredUserSuccessfullyReturnsSavedUser() throws Exception {
        User testUser = User.builder()
                .role(Role.USER)
                .email("test@email.com")
                .username("testUser")
                .password("pass")
                .build();

        String userJson = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(
                post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
        ).andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.password").value("pass"))
                .andExpect(jsonPath("$.role").value("USER"));
    }
}