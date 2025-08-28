package za.co.fintrack;

import za.co.fintrack.enums.AccountStatus;
import za.co.fintrack.enums.AccountType;
import za.co.fintrack.enums.Role;
import za.co.fintrack.models.entities.Account;
import za.co.fintrack.models.entities.User;
import za.co.fintrack.services.UserService;

import java.math.BigDecimal;

public final class TestDataCreatorUtil {


    private TestDataCreatorUtil() {
    }


    public static User createTestUser(UserService userService) {
        User usr = User.builder()
                .role(Role.USER)
                .email("test@email.com")
                .username("testUser")
                .password("pass")
                .build();
        return userService.saveUser(usr);
    }

    public static Account createTestAccount(User testUser) {
        return Account.builder()
                .type(AccountType.SAVINGS)
                .user(testUser)
                .status(AccountStatus.ACTIVE)
                .balance(BigDecimal.valueOf(12345))
                .name("Main")
                .build();
    }
}
