package com.NexusHelth.controller;

import com.NexusHelth.model.User;
import com.NexusHelth.model.Patient;
import com.NexusHelth.service.PatientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PatientDataController {

    private PatientService patientService = new PatientService();
    private com.NexusHelth.service.PatientDashboardService patientDashboardService = new com.NexusHelth.service.PatientDashboardService();

    @GetMapping("/patient-info")
    public PatientDataResponse getPatientInfo(HttpSession session) {
        System.out.println("\n==================== API PATIENT-INFO REQUEST ====================");
        System.out.println("📡 API patient-info endpoint called");
        System.out.println("🔐 Session ID: " + session.getId());
        System.out.println("🔍 Session Attributes: " + java.util.Collections.list(session.getAttributeNames()));

        // Get user from Spring HttpSession (not from custom SessionManager)
        User user = (User) session.getAttribute("user");

        if (user == null) {
            System.out.println("❌ No user in HttpSession - returning authentication error");
            System.out.println("=================================================================\n");
            return new PatientDataResponse(false, null, "Not authenticated");
        }

        System.out.println("✅ User found in session: " + user.getEmail());
        Patient patient = patientService.getPatientByUserId(user.getId());

        if (patient != null) {
            System.out.println("✅ Patient found: " + patient.getId());
            System.out.println("=================================================================\n");
            return new PatientDataResponse(true, patient, null);
        } else {
            System.out.println("❌ Patient not found for user: " + user.getId());
            System.out.println("=================================================================\n");
            return new PatientDataResponse(false, null, "Patient not found");
        }
    }

    @GetMapping("/patient/dashboard")
    public DashboardResponse getDashboardData(HttpSession session) {
        System.out.println("\n==================== API PATIENT-DASHBOARD REQUEST ====================");

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return new DashboardResponse(false, null, null, "Not authenticated");
        }

        Patient patient = patientService.getPatientByUserId(user.getId());

        if (patient != null) {
            com.NexusHelth.service.PatientDashboardService.DashboardData metrics = patientDashboardService
                    .getDashboardData(patient.getId());
            return new DashboardResponse(true, patient, metrics, null);
        } else {
            return new DashboardResponse(false, null, null, "Patient not found");
        }
    }

    // Inner class for response
    public static class PatientDataResponse {
        public boolean success;
        public Patient data;
        public String error;

        public PatientDataResponse(boolean success, Patient data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }

        public boolean isSuccess() {
            return success;
        }

        public Patient getData() {
            return data;
        }

        public String getError() {
            return error;
        }
    }

    // Get all patients for admin dashboard
    @GetMapping("/admin/patients")
    public PatientsListResponse getAllPatients(HttpSession session) {
        System.out.println("\n==================== API GET-ALL-PATIENTS REQUEST ====================");
        System.out.println("📡 API admin/patients endpoint called");

        // Get user from session to verify admin access
        User user = (User) session.getAttribute("user");

        if (user == null || !user.getRole().equals("admin")) {
            System.out.println("❌ Unauthorized access attempt - not an admin");
            System.out.println("========================================================================\n");
            return new PatientsListResponse(false, null, "Unauthorized access");
        }

        System.out.println("✅ Admin verified: " + user.getEmail());
        List<Patient> patients = patientService.getAllPatients();

        System.out.println("✅ Retrieved " + patients.size() + " patients");
        System.out.println("========================================================================\n");
        return new PatientsListResponse(true, patients, null);
    }

    // Update patient account status
    @PostMapping("/admin/patient/status")
    public StatusUpdateResponse updatePatientStatus(
            @RequestParam int patientId,
            @RequestParam String status,
            HttpSession session) {
        System.out.println("\n==================== API UPDATE-PATIENT-STATUS REQUEST ====================");
        System.out.println("📡 Updating patient status for ID: " + patientId);

        // Get user from session to verify admin access
        User user = (User) session.getAttribute("user");

        if (user == null || !user.getRole().equals("admin")) {
            System.out.println("❌ Unauthorized access attempt - not an admin");
            System.out.println("===========================================================================\n");
            return new StatusUpdateResponse(false, "Unauthorized access");
        }

        System.out.println("✅ Admin verified: " + user.getEmail());
        System.out.println("📝 New status: " + status);

        // Validate status value
        if (!status.equals("active") && !status.equals("deactivated") && !status.equals("archived")) {
            System.out.println("❌ Invalid status value: " + status);
            return new StatusUpdateResponse(false, "Invalid status value");
        }

        boolean success = patientService.updatePatientStatus(patientId, status);

        if (success) {
            System.out.println("✅ Patient status updated successfully");
            System.out.println("===========================================================================\n");
            return new StatusUpdateResponse(true, "Patient status updated successfully");
        } else {
            System.out.println("❌ Failed to update patient status");
            System.out.println("===========================================================================\n");
            return new StatusUpdateResponse(false, "Failed to update patient status");
        }
    }

    // Delete patient account
    @PostMapping("/admin/patient/delete")
    public StatusUpdateResponse deletePatient(
            @RequestParam int patientId,
            HttpSession session) {
        System.out.println("\n==================== API DELETE-PATIENT REQUEST ====================");
        System.out.println("🗑️  Deleting patient ID: " + patientId);

        // Get user from session to verify admin access
        User user = (User) session.getAttribute("user");

        if (user == null || !user.getRole().equals("admin")) {
            System.out.println("❌ Unauthorized access attempt - not an admin");
            System.out.println("====================================================================\n");
            return new StatusUpdateResponse(false, "Unauthorized access");
        }

        System.out.println("✅ Admin verified: " + user.getEmail());
        boolean success = patientService.deletePatient(patientId);

        if (success) {
            System.out.println("✅ Patient deleted successfully");
            System.out.println("====================================================================\n");
            return new StatusUpdateResponse(true, "Patient deleted successfully");
        } else {
            System.out.println("❌ Failed to delete patient");
            System.out.println("====================================================================\n");
            return new StatusUpdateResponse(false, "Failed to delete patient");
        }
    }

    // Create new patient account (admin)
    @PostMapping("/admin/patient/create")
    public StatusUpdateResponse createPatient(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String phone,
            @RequestParam(required = false, defaultValue = "") String dateOfBirth,
            @RequestParam(required = false, defaultValue = "") String gender,
            @RequestParam(required = false, defaultValue = "") String bloodType,
            @RequestParam(required = false, defaultValue = "") String address,
            HttpSession session) {

        System.out.println("\n==================== API CREATE-PATIENT REQUEST ====================");

        // Verifying Admin Access
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("admin")) {
            System.out.println("❌ Unauthorized access attempt - not an admin");
            return new StatusUpdateResponse(false, "Unauthorized access");
        }

        System.out.println("✅ Admin verified: " + user.getEmail());

        // Input validation
        if (fullName == null || fullName.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                phone == null || phone.trim().isEmpty()) {
            return new StatusUpdateResponse(false, "Name, email, password, and phone are required");
        }

        // We use registerPatient with the newly added parameters
        boolean success = patientService.registerPatient(fullName, email, password, phone, dateOfBirth, gender,
                bloodType, address);

        if (success) {
            System.out.println("✅ Patient created successfully by admin");
            System.out.println("====================================================================\n");
            return new StatusUpdateResponse(true, "Patient created successfully");
        } else {
            System.out.println("❌ Failed to create patient");
            System.out.println("====================================================================\n");
            return new StatusUpdateResponse(false, "Failed to create patient. Email might already exist.");
        }
    }

    // Inner class for patients list response
    public static class PatientsListResponse {
        public boolean success;
        public List<Patient> data;
        public String error;

        public PatientsListResponse(boolean success, List<Patient> data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }

        public boolean isSuccess() {
            return success;
        }

        public List<Patient> getData() {
            return data;
        }

        public String getError() {
            return error;
        }
    }

    // Inner class for status update response
    public static class StatusUpdateResponse {
        public boolean success;
        public String message;

        public StatusUpdateResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    // Debug endpoint to check session
    @GetMapping("/debug/session")
    public DebugResponse getSessionDebug(HttpSession session) {
        System.out.println("\n==================== DEBUG SESSION ====================");
        System.out.println("Session ID: " + session.getId());
        System.out.println("Session Attributes: " + java.util.Collections.list(session.getAttributeNames()));

        User user = (User) session.getAttribute("user");
        System.out.println("User Object: " + user);
        if (user != null) {
            System.out.println("  - ID: " + user.getId());
            System.out.println("  - Email: " + user.getEmail());
            System.out.println("  - Role: " + user.getRole());
            System.out.println("  - Status: " + user.getStatus());
        }
        System.out.println("======================================================\n");

        return new DebugResponse(
                session.getId(),
                java.util.Collections.list(session.getAttributeNames()).toString(),
                user != null ? user.getEmail() : "null",
                user != null ? user.getRole() : "null");
    }

    // Debug response class
    public static class DebugResponse {
        public String sessionId;
        public String attributes;
        public String userEmail;
        public String userRole;

        public DebugResponse(String sessionId, String attributes, String userEmail, String userRole) {
            this.sessionId = sessionId;
            this.attributes = attributes;
            this.userEmail = userEmail;
            this.userRole = userRole;
        }
    }

    public static class DashboardResponse {
        public boolean success;
        public Patient patient;
        public com.NexusHelth.service.PatientDashboardService.DashboardData metrics;
        public String error;

        public DashboardResponse(boolean success, Patient patient,
                com.NexusHelth.service.PatientDashboardService.DashboardData metrics, String error) {
            this.success = success;
            this.patient = patient;
            this.metrics = metrics;
            this.error = error;
        }

        public boolean isSuccess() {
            return success;
        }

        public Patient getPatient() {
            return patient;
        }

        public com.NexusHelth.service.PatientDashboardService.DashboardData getMetrics() {
            return metrics;
        }

        public String getError() {
            return error;
        }
    }
}
