package com.NexusHelth.controller;

import com.NexusHelth.model.User;
import com.NexusHelth.service.SettingsService;
import com.NexusHelth.util.DatabaseConnection;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService = new SettingsService();

    @GetMapping("/admin-profile")
    public Map<String, Object> getAdminProfile(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return response;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT full_name, email, profile_picture FROM users WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, user.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Map<String, String> userData = new HashMap<>();
                        userData.put("fullName", rs.getString("full_name"));
                        userData.put("email", rs.getString("email"));
                        userData.put("profilePicture", rs.getString("profile_picture"));
                        response.put("success", true);
                        response.put("user", userData);
                    } else {
                        response.put("success", false);
                        response.put("message", "User not found");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Database error");
        }
        return response;
    }

    public static class ProfileUpdateRequest {
        public String fullName;
        public String phone;
        public String profilePicture;
    }

    @PostMapping("/profile")
    public StandardResponse updateProfile(@RequestBody ProfileUpdateRequest req, HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new StandardResponse(false, "Authentication required");
        }

        System.out.println("Updating profile: " + req.fullName + ", Pic length: "
                + (req.profilePicture != null ? req.profilePicture.length() : "null"));

        boolean success = settingsService.updateProfile(user.getId(), req.fullName, req.phone, req.profilePicture);
        if (success) {
            user.setFullName(req.fullName); // Update session's cached name
            session.setAttribute("user", user);
            return new StandardResponse(true, "Profile updated successfully");
        }
        return new StandardResponse(false, "Failed to update profile");
    }

    public static class PasswordUpdateRequest {
        public String currentPassword;
        public String newPassword;
    }

    @PostMapping("/password")
    public StandardResponse updatePassword(@RequestBody PasswordUpdateRequest req, HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new StandardResponse(false, "Authentication required");
        }

        boolean success = settingsService.updatePassword(user.getId(), req.currentPassword, req.newPassword);
        if (success) {
            return new StandardResponse(true, "Password updated successfully");
        } else {
            return new StandardResponse(false, "Incorrect current password or server error");
        }
    }

    // A simple response wrapper
    public static class StandardResponse {
        public boolean success;
        public String message;

        public StandardResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
