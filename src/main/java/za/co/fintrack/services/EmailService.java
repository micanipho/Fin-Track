package za.co.fintrack.services;

public interface EmailService {
    void sendEmailVerification(String to, String token, String username);
    void sendPasswordResetEmail(String to, String token, String username);
    void sendWelcomeEmail(String to, String username);
}
