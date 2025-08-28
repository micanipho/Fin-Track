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
import za.co.fintrack.TestDataCreatorUtil;
import za.co.fintrack.models.entities.Account;
import za.co.fintrack.models.entities.User;
import za.co.fintrack.services.AccountService;
import za.co.fintrack.services.UserService;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class AccountControllersTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    public final UserService userService;
    private final AccountService accountService;

    @Autowired
    public AccountControllersTest(MockMvc mockMvc,
                                  ObjectMapper objectMapper, UserService userService, AccountService accountService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userService = userService;

        this.accountService = accountService;
    }

    @Test
    void testThatCreatedAccountSuccessfullyReturnsHttp201Created() throws Exception {

        User testUser = TestDataCreatorUtil.createTestUser(userService);

        Account account = TestDataCreatorUtil.createTestAccount(testUser);

        String accountJson = objectMapper.writeValueAsString(account);

        mockMvc.perform(
                post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountJson)
        ).andExpect(status().isCreated());
    }

    @Test
    void testThatCreatedAccountSuccessfullyReturnsCreatedAccount() throws Exception {

        User testUser = userService.saveUser(TestDataCreatorUtil.createTestUser(userService));

        Account account = TestDataCreatorUtil.createTestAccount(testUser);


        String accountJson = objectMapper.writeValueAsString(account);

        mockMvc.perform(
                post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountJson)
        ).andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.name").value("Main"))
                .andExpect(jsonPath("$.type").value("SAVINGS"))
                .andExpect(jsonPath("$.balance").value(BigDecimal.valueOf(12345)))
                .andExpect(jsonPath("$.user").value(testUser));
    }

    @Test
    void testThatListAccountsReturnsHttpStatus200() throws Exception {

        mockMvc.perform(
                get("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void testThatListAccountsReturnsListOfAccounts() throws Exception {

        User testUser = TestDataCreatorUtil.createTestUser(userService);

        accountService.saveAccount(TestDataCreatorUtil.createTestAccount(testUser));

        mockMvc.perform(
                get("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].name").value("Main"))
                .andExpect(jsonPath("$[0].type").value("SAVINGS"))
                .andExpect(jsonPath("$[0].balance").exists())
                .andExpect(jsonPath("$[0].user").value(testUser));
    }

    @Test
    void testThatGetAccountsReturnsHttpStatus200WhenItExists() throws Exception {


        accountService.saveAccount(TestDataCreatorUtil.createTestAccount(TestDataCreatorUtil.createTestUser(userService)));

        mockMvc.perform(
                get("/api/v1/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void testThatGetAccountsReturnsHttpStatus404WhenItDoesNotExist() throws Exception {

        mockMvc.perform(
                get("/api/v1/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }

    @Test
    void testThatGetAccountsReturnsCorrectSavedAccount() throws Exception {

        User testUser = TestDataCreatorUtil.createTestUser(userService);

        accountService.saveAccount(TestDataCreatorUtil.createTestAccount(testUser));

        mockMvc.perform(
                        get("/api/v1/accounts/1")
                                .contentType(MediaType.APPLICATION_JSON)
                ).andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.name").value("Main"))
                .andExpect(jsonPath("$.type").value("SAVINGS"))
                .andExpect(jsonPath("$.balance").exists())
                .andExpect(jsonPath("$.user").value(testUser));
    }
}