package com.NexusHelth.util;

/**
 * Utility class for validating password strength and input validation
 */
public class ValidationUtil {
    
    // Password requirements
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 128;
    
    /**
     * Validates if password meets minimum requirements
     * @param password the password to validate
     * @return true if password is valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        
        // Check minimum length
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        
        // Check maximum length
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates if email format is correct
     * @param email the email to validate
     * @return true if email format is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Simple email regex pattern
        String emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailPattern);
    }
    
    /**
     * Validates if phone number is not empty
     * @param phone the phone number to validate
     * @return true if phone is valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && !phone.trim().isEmpty() && phone.length() >= 7;
    }
    
    /**
     * Validates if full name is not empty and reasonable length
     * @param fullName the full name to validate
     * @return true if name is valid, false otherwise
     */
    public static boolean isValidFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return false;
        }
        
        // Check length (between 3 and 100 characters)
        String trimmed = fullName.trim();
        return trimmed.length() >= 3 && trimmed.length() <= 100;
    }
    
    /**
     * Validates if address is not empty
     * @param address the address to validate
     * @return true if address is valid, false otherwise
     */
    public static boolean isValidAddress(String address) {
        return address != null && !address.trim().isEmpty() && address.length() >= 5;
    }
    
    /**
     * Validates if gender is one of allowed values
     * @param gender the gender to validate
     * @return true if gender is valid, false otherwise
     */
    public static boolean isValidGender(String gender) {
        String normalized = normalizeGender(gender);
        return "male".equals(normalized) || "female".equals(normalized) || "other".equals(normalized);
    }

    /**
     * Normalizes gender inputs to values accepted by the DB constraint.
     * The SQLite schema uses: CHECK(gender IN ('male','female','other'))
     *
     * @param gender raw gender input (any casing / may include whitespace)
     * @return normalized gender (male/female/other), or null if blank, or the
     *         lowercased value if not recognized.
     */
    public static String normalizeGender(String gender) {
        if (gender == null) {
            return null;
        }

        String g = gender.trim().toLowerCase();
        if (g.isEmpty()) {
            return null;
        }

        // Common short forms
        if (g.equals("m")) {
            return "male";
        }
        if (g.equals("f")) {
            return "female";
        }

        return g;
    }
    
    /**
     * Validates if blood type is one of allowed values
     * @param bloodType the blood type to validate
     * @return true if blood type is valid, false otherwise
     */
    public static boolean isValidBloodType(String bloodType) {
        if (bloodType == null || bloodType.trim().isEmpty()) {
            return false;
        }
        
        String[] validTypes = {"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"};
        for (String type : validTypes) {
            if (type.equals(bloodType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Sanitizes input string to prevent XSS attacks
     * @param input the input to sanitize
     * @return sanitized string
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        return input.trim()
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;")
                   .replace("/", "&#x2F;");
    }
}

