package com.NexusHelth.controller;

import com.NexusHelth.model.User;
import com.NexusHelth.util.DatabaseConnection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@RestController
public class PharmacistDataController {

    @GetMapping("/api/pharmacist/profile")
    public PharmacistProfileResponse getPharmacistProfile(HttpSession session) {
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null || !user.getRole().equals("pharmacist")) {
            return new PharmacistProfileResponse(false, null, "Not authenticated as pharmacist");
        }

        PharmacistDTO pharmacist = new PharmacistDTO();
        pharmacist.fullName = user.getFullName();

        // Ensure the phone column exists in users table locally without crashing
        try (Connection conn = DatabaseConnection.getConnection();
                java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE users ADD COLUMN phone TEXT");
        } catch (Exception ignored) {
            // Ignored, column likely already exists
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT phone, profile_picture FROM users WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, user.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        pharmacist.phone = rs.getString("phone");
                        pharmacist.profilePicture = rs.getString("profile_picture");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new PharmacistProfileResponse(true, pharmacist, null);
    }

    public static class PharmacistDTO {
        public String fullName;
        public String profilePicture;
        public String phone;
    }

    public static class PharmacistProfileResponse {
        public boolean success;
        public PharmacistDTO pharmacist;
        public String error;

        public PharmacistProfileResponse(boolean success, PharmacistDTO pharmacist, String error) {
            this.success = success;
            this.pharmacist = pharmacist;
            this.error = error;
        }
    }
}

