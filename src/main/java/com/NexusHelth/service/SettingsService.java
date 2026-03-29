package com.NexusHelth.service;

import com.NexusHelth.util.DatabaseConnection;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsService {
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean updateProfile(int userId, String fullName, String phone, String profilePicture) {
        String updateUser = "UPDATE users SET full_name = ?, phone = ?"
                + (profilePicture != null && !profilePicture.isEmpty() ? ", profile_picture = ?" : "")
                + " WHERE id = ?";
        String updatePatient = "UPDATE patients SET phone = ? WHERE user_id = ?";
        String updateDoctor = "UPDATE doctors SET phone = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Ensure the phone column exists in doctors table without crashing if it
            // already does
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE doctors ADD COLUMN phone TEXT");
            } catch (Exception ignored) {
                // Ignored, column likely already exists
            }

            // Ensure the phone column exists in users table without crashing if it
            // already does
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE users ADD COLUMN phone TEXT");
            } catch (Exception ignored) {
                // Ignored, column likely already exists
            }

            conn.setAutoCommit(false); // Transaction block

            try (PreparedStatement uStmt = conn.prepareStatement(updateUser);
                    PreparedStatement pStmt = conn.prepareStatement(updatePatient);
                    PreparedStatement dStmt = conn.prepareStatement(updateDoctor)) {

                uStmt.setString(1, fullName);
                uStmt.setString(2, phone);
                int paramIndex = 3;
                if (profilePicture != null && !profilePicture.isEmpty()) {
                    uStmt.setString(paramIndex++, profilePicture);
                }
                uStmt.setInt(paramIndex, userId);

                pStmt.setString(1, phone);
                pStmt.setInt(2, userId);

                dStmt.setString(1, phone);
                dStmt.setInt(2, userId);

                uStmt.executeUpdate();
                pStmt.executeUpdate();
                dStmt.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error updating profile settings: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePassword(int userId, String currentPassword, String newPassword) {
        String getPassQuery = "SELECT password_hash FROM users WHERE id = ?";
        String updatePassQuery = "UPDATE users SET password_hash = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check old password
            try (PreparedStatement pstmt = conn.prepareStatement(getPassQuery)) {
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password_hash");
                        if (!passwordEncoder.matches(currentPassword, storedHash)) {
                            return false; // Wrong current password
                        }
                    } else {
                        return false; // User not found
                    }
                }
            }

            // Encrypt and save new password
            String hash = passwordEncoder.encode(newPassword);
            try (PreparedStatement pstmt = conn.prepareStatement(updatePassQuery)) {
                pstmt.setString(1, hash);
                pstmt.setInt(2, userId);
                pstmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
