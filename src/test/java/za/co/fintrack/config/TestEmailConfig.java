package za.co.fintrack.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import za.co.fintrack.services.EmailService;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestEmailConfig {

    @Bean
    @Primary
    public EmailService emailService() {
        return mock(EmailService.class);
    }
}
