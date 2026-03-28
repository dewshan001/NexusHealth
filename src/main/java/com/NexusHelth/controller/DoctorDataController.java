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
public class DoctorDataController {

    @GetMapping("/api/doctor/profile")
    public DoctorProfileResponse getDoctorProfile(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("doctor")) {
            return new DoctorProfileResponse(false, null, "Not authenticated as doctor");
        }

        DoctorDTO doctor = new DoctorDTO();
        doctor.fullName = user.getFullName();

        // Ensure the phone column exists in doctors table without crashing if it
        // already does
        try (Connection conn = DatabaseConnection.getConnection();
                java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE doctors ADD COLUMN phone TEXT");
        } catch (Exception ignored) {
            // Ignored, column likely already exists
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT d.phone, u.profile_picture FROM users u LEFT JOIN doctors d ON u.id = d.user_id WHERE u.id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, user.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        doctor.phone = rs.getString("phone");
                        doctor.profilePicture = rs.getString("profile_picture");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new DoctorProfileResponse(true, doctor, null);
    }

    public static class DoctorDTO {
        public String fullName;
        public String profilePicture;
        public String phone;
    }

    public static class DoctorProfileResponse {
        public boolean success;
        public DoctorDTO doctor;
        public String error;

        public DoctorProfileResponse(boolean success, DoctorDTO doctor, String error) {
            this.success = success;
            this.doctor = doctor;
            this.error = error;
        }
    }
}
