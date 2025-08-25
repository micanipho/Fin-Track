package za.co.fintrack.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import za.co.fintrack.enums.Role;
import za.co.fintrack.models.entities.User;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        // Given
        User user = User.builder()
            .username("testuser")
            .email("test@example.com")
            .password("password")
            .role(Role.USER)
            .build();

        // When
        User saved = userRepository.save(user);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(userRepository.findById(saved.getId())).isPresent();
    }
}