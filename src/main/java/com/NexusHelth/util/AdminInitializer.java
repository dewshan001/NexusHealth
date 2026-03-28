package com.NexusHelth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Initializes a default admin account in the database on first startup.
 * This allows users to access the admin dashboard without manual database setup.
 */
@Component
public class AdminInitializer {
    
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // Default admin credentials
    private static final String ADMIN_EMAIL = "admin@clinic.com";
    private static final String ADMIN_NAME = "System Administrator";
    private static final String ADMIN_PASSWORD = "Admin@123";  // Default password - users should change this!
    
    /**
     * Creates a default admin account if one doesn't already exist.
     * This method is called during application startup.
     */
    public static void initializeAdminAccount() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if admin account already exists
            if (adminAccountExists(conn)) {
                System.out.println("ℹ️  Admin account already exists");
                return;
            }
            
            // Create new admin account
            String hashedPassword = passwordEncoder.encode(ADMIN_PASSWORD);
            String query = "INSERT INTO users (full_name, email, password_hash, role, status) VALUES (?, ?, ?, 'admin', 'active')";
            
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, ADMIN_NAME);
                pstmt.setString(2, ADMIN_EMAIL);
                pstmt.setString(3, hashedPassword);
                pstmt.executeUpdate();
                
                System.out.println("\n" + "=".repeat(60));
                System.out.println("✅ DEFAULT ADMIN ACCOUNT CREATED");
                System.out.println("=".repeat(60));
                System.out.println("📧 Email:    " + ADMIN_EMAIL);
                System.out.println("🔐 Password: " + ADMIN_PASSWORD);
                System.out.println("⚠️  IMPORTANT: Please change this password after first login!");
                System.out.println("=".repeat(60) + "\n");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error initializing admin account: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Checks if an admin account already exists in the database.
     */
    private static boolean adminAccountExists(Connection conn) {
        String query = "SELECT COUNT(*) FROM users WHERE role = 'admin'";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking for existing admin account: " + e.getMessage());
        }
        return false;
    }
}

