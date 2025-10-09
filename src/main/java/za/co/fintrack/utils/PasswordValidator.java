package za.co.fintrack.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    public static ValidationResult validate(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.length() < MIN_LENGTH) {
            errors.add("Password must be at least " + MIN_LENGTH + " characters long");
        }

        if (password != null) {
            if (!UPPERCASE_PATTERN.matcher(password).find()) {
                errors.add("Password must contain at least one uppercase letter");
            }

            if (!LOWERCASE_PATTERN.matcher(password).find()) {
                errors.add("Password must contain at least one lowercase letter");
            }

            if (!DIGIT_PATTERN.matcher(password).find()) {
                errors.add("Password must contain at least one digit");
            }

            if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
                errors.add("Password must contain at least one special character");
            }

            // Check for common weak passwords
            if (isCommonPassword(password)) {
                errors.add("Password is too common. Please choose a stronger password");
            }
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    private static boolean isCommonPassword(String password) {
        String lowerPassword = password.toLowerCase();
        String[] commonPasswords = {
            "password", "123456", "password123", "admin", "qwerty",
            "letmein", "welcome", "monkey", "1234567890", "abc123"
        };

        for (String common : commonPasswords) {
            if (lowerPassword.contains(common)) {
                return true;
            }
        }
        return false;
    }

    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }
}
