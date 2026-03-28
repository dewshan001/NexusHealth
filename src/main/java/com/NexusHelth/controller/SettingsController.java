package com.NexusHelth.controller;

import com.NexusHelth.model.User;
import com.NexusHelth.service.SettingsService;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService = new SettingsService();

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
