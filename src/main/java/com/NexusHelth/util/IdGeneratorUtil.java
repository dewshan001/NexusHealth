package com.NexusHelth.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Utility class for generating unique identifiers
 */
public class IdGeneratorUtil {
    
    /**
     * Generates a unique patient code in format: PAT-YYYYMMDD-XXXXX
     * Example: PAT-20260328-A7X2K
     * 
     * @return a unique patient code
     */
    public static String generatePatientCode() {
        // Get current date
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String dateStr = today.format(formatter);
        
        // Generate random suffix (5 characters)
        String randomSuffix = generateRandomAlphanumeric(5);
        
        return "PAT-" + dateStr + "-" + randomSuffix;
    }
    
    /**
     * Generates a unique session ID
     * 
     * @return a UUID-based session ID
     */
    public static String generateSessionId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Generates a random alphanumeric string of specified length
     * 
     * @param length the length of the string to generate
     * @return a random alphanumeric string
     */
    private static String generateRandomAlphanumeric(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            sb.append(characters.charAt(index));
        }
        
        return sb.toString();
    }
}

