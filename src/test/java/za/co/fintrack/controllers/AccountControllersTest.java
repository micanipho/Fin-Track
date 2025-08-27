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
import za.co.fintrack.enums.AccountStatus;
import za.co.fintrack.enums.AccountType;
import za.co.fintrack.enums.Role;
import za.co.fintrack.models.entities.Account;
import za.co.fintrack.models.entities.User;
import za.co.fintrack.repositories.UserRepository;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class AccountControllersTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Autowired
    public AccountControllersTest(MockMvc mockMvc,
                                  ObjectMapper objectMapper,
                                  UserRepository userRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    @Test
    void testThatCreatedAccountSuccessfullyReturnsHttp201Created() throws Exception {
        User testUser = userRepository.save(
                User.builder()
                        .role(Role.USER)
                        .email("test@email.com")
                        .username("testUser")
                        .password("pass")
                        .build()
        );

        Account account = Account.builder()
                .type(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .user(testUser)
                .balance(BigDecimal.valueOf(12345))
                .name("Main")
                .build();

        String accountJson = objectMapper.writeValueAsString(account);

        mockMvc.perform(
                post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountJson)
        ).andExpect(status().isCreated());
    }
}