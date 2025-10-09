package za.co.fintrack.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import za.co.fintrack.services.EmailService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendEmailVerification(String to, String token, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromEmail);
            message.setSubject("FinTrack - Verify Your Email Address");

            String verificationUrl = frontendUrl + "/verify-email?token=" + token;
            String text = String.format(
                "Hello %s,\n\n" +
                "Thank you for registering with FinTrack!\n\n" +
                "Please click the link below to verify your email address:\n" +
                "%s\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you didn't create this account, please ignore this email.\n\n" +
                "Best regards,\n" +
                "The FinTrack Team",
                username, verificationUrl
            );

            message.setText(text);
            mailSender.send(message);
            log.info("Email verification sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email verification to: {}", to, e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String token, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromEmail);
            message.setSubject("FinTrack - Password Reset Request");

            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            String text = String.format(
                "Hello %s,\n\n" +
                "We received a request to reset your password for your FinTrack account.\n\n" +
                "Please click the link below to reset your password:\n" +
                "%s\n\n" +
                "This link will expire in 1 hour.\n\n" +
                "If you didn't request this password reset, please ignore this email.\n\n" +
                "Best regards,\n" +
                "The FinTrack Team",
                username, resetUrl
            );

            message.setText(text);
            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
        }
    }

    @Override
    public void sendWelcomeEmail(String to, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromEmail);
            message.setSubject("Welcome to FinTrack!");

            String text = String.format(
                "Hello %s,\n\n" +
                "Welcome to FinTrack - Your Personal Finance Management Platform!\n\n" +
                "Your account has been successfully created and verified. You can now:\n" +
                "• Track your expenses and income\n" +
                "• Set and monitor financial goals\n" +
                "• Create and manage budgets\n" +
                "• View detailed financial reports\n\n" +
                "Get started by logging into your account at: %s\n\n" +
                "If you have any questions, feel free to contact our support team.\n\n" +
                "Best regards,\n" +
                "The FinTrack Team",
                username, frontendUrl
            );

            message.setText(text);
            mailSender.send(message);
            log.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", to, e);
        }
    }
}
