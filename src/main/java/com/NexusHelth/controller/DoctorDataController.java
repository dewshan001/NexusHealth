package com.NexusHelth.controller;

import com.NexusHelth.model.User;
import com.NexusHelth.model.Medicine;
import com.NexusHelth.service.PharmacistInventoryService;
import com.NexusHelth.service.DoctorPrescriptionService;
import com.NexusHelth.service.AppointmentService;
import com.NexusHelth.model.Doctor;
import com.NexusHelth.util.DatabaseConnection;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
public class DoctorDataController {

    private final PharmacistInventoryService inventoryService = new PharmacistInventoryService();
    private final DoctorPrescriptionService prescriptionService = new DoctorPrescriptionService();
    private final AppointmentService appointmentService = new AppointmentService();

    @GetMapping("/api/doctor/profile")
    public DoctorProfileResponse getDoctorProfile(HttpSession session) {
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
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
            String sql = "SELECT d.phone, d.availability_status, u.profile_picture FROM users u LEFT JOIN doctors d ON u.id = d.user_id WHERE u.id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, user.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        doctor.phone = rs.getString("phone");
                        String availabilityStatus = rs.getString("availability_status");
                        doctor.availabilityStatus = (availabilityStatus == null || availabilityStatus.trim().isEmpty())
                                ? "available"
                                : availabilityStatus.trim().toLowerCase();
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
        public String availabilityStatus;
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

    @GetMapping("/api/doctor/medicines")
    public MedicineListResponse getMedicines(HttpSession session) {
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null || !user.getRole().equals("doctor")) {
            return new MedicineListResponse(false, null, "Not authenticated as doctor");
        }

        List<Medicine> data = inventoryService.getAllMedicines();
        return new MedicineListResponse(true, data, null);
    }

    public static class MedicineListResponse {
        public boolean success;
        public List<Medicine> data;
        public String error;

        public MedicineListResponse(boolean success, List<Medicine> data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }
    }

    // --- Prescription Creation Endpoint ---

    @PostMapping("/api/doctor/prescriptions")
    public ResponseEntity<?> createPrescription(@RequestBody PrescriptionRequest request, HttpSession session) {
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null || !"doctor".equals(user.getRole())) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Doctor doctor = appointmentService.getDoctorByUserId(user.getId());
        if (doctor == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Doctor profile not found"));
        }

        if (request.patientCode == null || request.patientCode.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Patient code is required"));
        }

        if (request.appointmentId == null || request.appointmentId <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Appointment id is required"));
        }

        if (request.medications == null || request.medications.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "At least one medication is required"));
        }

        DoctorPrescriptionService.PrescriptionResult result = prescriptionService.createPrescription(doctor.getId(),
            request.appointmentId, request.patientCode, request.medications);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.success);
        response.put("message", result.message);
        return ResponseEntity.ok(response);
    }

    public static class PrescriptionRequest {
        public String patientName;
        public String patientCode;
        public Integer appointmentId;
        public List<Map<String, String>> medications;
    }

    @PostMapping("/api/doctor/availability")
    public ResponseEntity<?> updateAvailability(@RequestBody AvailabilityRequest request, HttpSession session) {
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null || !"doctor".equals(user.getRole())) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        if (request == null || request.available == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Availability value is required"));
        }

        boolean updated = appointmentService.updateDoctorAvailabilityByUserId(user.getId(), request.available);
        if (!updated) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Unable to update availability"));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "availabilityStatus", request.available ? "available" : "unavailable"
        ));
    }

    public static class AvailabilityRequest {
        public Boolean available;
    }
}

