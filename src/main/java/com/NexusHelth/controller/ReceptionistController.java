package com.NexusHelth.controller;

import com.NexusHelth.model.User;
import com.NexusHelth.model.Patient;
import com.NexusHelth.model.Invoice;
import com.NexusHelth.service.PatientService;
import com.NexusHelth.service.AppointmentService;
import com.NexusHelth.service.BillingService;
import com.NexusHelth.service.UserService;
import com.NexusHelth.dto.AppointmentRequest;
import com.NexusHelth.dto.AppointmentResponse;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/receptionist")
public class ReceptionistController {

    private final PatientService patientService = new PatientService();
    private final AppointmentService appointmentService = new AppointmentService();
    private final BillingService billingService = new BillingService();
    private final UserService userService = new UserService();

    /**
     * Check if user is authenticated as receptionist
     */
    private boolean isReceptionist(User user) {
        return user != null && "receptionist".equals(user.getRole());
    }

    // ===================== PATIENT ENDPOINTS =====================

    /**
     * Get all patients (with optional search filter)
     */
    @GetMapping("/patients")
    public StandardResponse getPatients(
            @RequestParam(required = false) String search,
            HttpSession session) {
        System.out.println("\n[📋] GET /api/receptionist/patients");

        User user = (User) session.getAttribute("user");
        if (!isReceptionist(user)) {
            return new StandardResponse(false, "Unauthorized - receptionist role required", null);
        }

        List<Patient> patients;
        if (search != null && !search.isEmpty()) {
            patients = patientService.searchPatients(search);
        } else {
            patients = patientService.getAllPatients();
        }

        return new StandardResponse(true, "Patients retrieved successfully", patients);
    }

    /**
     * Register a new patient from receptionist dashboard
     */
    @PostMapping("/register-patient")
    public StandardResponse registerPatient(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String dateOfBirth,
            @RequestParam String gender,
            HttpSession session) {
        System.out.println("\n[➕] POST /api/receptionist/register-patient");

        User user = (User) session.getAttribute("user");
        if (!isReceptionist(user)) {
            return new StandardResponse(false, "Unauthorized - receptionist role required", null);
        }

        // Check if email already exists
        if (patientService.emailExists(email)) {
            return new StandardResponse(false, "Email already exists in the system", null);
        }

        // Generate a temporary password
        String tempPassword = "Temp@" + System.currentTimeMillis();

        String fullName = firstName + " " + lastName;
        Patient newPatient = patientService.registerPatientFromReceptionist(
                fullName, email, tempPassword, phone, dateOfBirth, gender);

        if (newPatient != null) {
            return new StandardResponse(true, "Patient registered successfully", newPatient);
        } else {
            return new StandardResponse(false, "Failed to register patient", null);
        }
    }

    // ===================== APPOINTMENT ENDPOINTS =====================

    /**
     * Get appointments for today
     */
    @GetMapping("/appointments/today")
    public StandardResponse getAppointmentsToday(HttpSession session) {
        System.out.println("\n[📅] GET /api/receptionist/appointments/today");

        User user = (User) session.getAttribute("user");
        if (!isReceptionist(user)) {
            return new StandardResponse(false, "Unauthorized - receptionist role required", null);
        }

        String today = java.time.LocalDate.now().toString();
        List<Map<String, Object>> appointments = appointmentService.getAppointmentsByDate(today);

        return new StandardResponse(true, "Today's appointments retrieved", appointments);
    }

    /**
     * Get appointments filtered by date and (optionally) doctor
     */
    @GetMapping("/appointments")
    public StandardResponse getAppointments(
            @RequestParam String date,
            @RequestParam(required = false) Integer doctorId,
            HttpSession session) {
        System.out.println("\n[📅] GET /api/receptionist/appointments");

        User user = (User) session.getAttribute("user");
        if (!isReceptionist(user)) {
            return new StandardResponse(false, "Unauthorized - receptionist role required", null);
        }

        List<Map<String, Object>> appointments = appointmentService.getAppointmentsByDate(date);

        // Filter by doctor if specified
        if (doctorId != null && doctorId > 0) {
            appointments.removeIf(appt -> !appt.get("doctorId").equals(doctorId));
        }

        return new StandardResponse(true, "Appointments retrieved", appointments);
    }

    /**
     * Reschedule an appointment - use PUT for idempotent operation
     */
    @PutMapping("/appointments/{appointmentId}/reschedule")
    public AppointmentResponse rescheduleAppointment(
            @PathVariable int appointmentId,
            @RequestBody AppointmentRequest request,
            HttpSession session) {
        System.out.println("\n[🔄] PUT /api/receptionist/appointments/" + appointmentId + "/reschedule");

        User user = (User) session.getAttribute("user");
        if (!isReceptionist(user)) {
            return AppointmentResponse.unauthorized();
        }

        // Validate input
        if (!request.isValidDateFormat()) {
            return AppointmentResponse.badRequest("Invalid date format", "Date must be YYYY-MM-DD format");
        }
        if (!request.isValidTimeFormat()) {
            return AppointmentResponse.badRequest("Invalid time format", "Time must be HH:mm format");
        }

        // Validate date is not in the past
        try {
            LocalDate requestedDate = LocalDate.parse(request.getAppointmentDate());
            if (requestedDate.isBefore(LocalDate.now())) {
                return AppointmentResponse.badRequest("Invalid date", "Cannot reschedule to a past date");
            }
        } catch (DateTimeParseException e) {
            return AppointmentResponse.badRequest("Invalid date format", "Date must be YYYY-MM-DD format");
        }

        // Check if appointment exists and can be rescheduled
        if (!appointmentService.canReschedule(appointmentId)) {
            Map<String, Object> appt = appointmentService.getAppointmentById(appointmentId);
            if (appt == null) {
                return AppointmentResponse.notFound("Appointment not found");
            }
            String status = (String) appt.get("status");
            return AppointmentResponse.conflict("Cannot reschedule appointment", 
                    "Appointment is " + status + " and cannot be rescheduled");
        }

        // Try to reschedule
        boolean success = appointmentService.rescheduleAppointment(appointmentId, 
                request.getAppointmentDate(), request.getAppointmentTime());

        if (success) {
            Map<String, Object> updatedAppt = appointmentService.getAppointmentById(appointmentId);
            return AppointmentResponse.success("Appointment rescheduled successfully", updatedAppt);
        } else {
            return AppointmentResponse.conflict("Cannot reschedule appointment", 
                    "Selected time slot is not available");
        }
    }

    /**
     * Reschedule an appointment - POST endpoint for backward compatibility
     */
    @PostMapping("/appointments/reschedule")
    public StandardResponse rescheduleAppointmentPost(
            @RequestParam int appointmentId,
            @RequestParam String newDate,
            @RequestParam String newTime,
            HttpSession session) {
        System.out.println("\n[🔄] POST /api/receptionist/appointments/reschedule");

        User user = (User) session.getAttribute("user");
        if (!isReceptionist(user)) {
            return new StandardResponse(false, "Unauthorized - receptionist role required", null);
        }

        boolean success = appointmentService.rescheduleAppointment(appointmentId, newDate, newTime);

        if (success) {
            return new StandardResponse(true, "Appointment rescheduled successfully", null);
        } else {
            return new StandardResponse(false, "Failed to reschedule appointment", null);
        }
    }

    /**
     * Cancel an appointment - use PUT for idempotent operation
     */
    @PutMapping("/appointments/{appointmentId}/cancel")
    public AppointmentResponse cancelAppointment(
            @PathVariable int appointmentId,
            HttpSession session) {
        System.out.println("\n[❌] PUT /api/receptionist/appointments/" + appointmentId + "/cancel");

        User user = (User) session.getAttribute("user");
        if (!isReceptionist(user)) {
            return AppointmentResponse.unauthorized();
        }

        // Check if appointment exists and can be cancelled
        if (!appointmentService.canCancel(appointmentId)) {
            Map<String, Object> appt = appointmentService.getAppointmentById(appointmentId);
            if (appt == null) {
                return AppointmentResponse.notFound("Appointment not found");
            }
            String status = (String) appt.get("status");
            return AppointmentResponse.conflict("Cannot cancel appointment", 
                    "Appointment is " + status + " and cannot be cancelled");
        }

        boolean success = appointmentService.cancelAppointment(appointmentId);

        if (success) {
            Map<String, Object> updatedAppt = appointmentService.getAppointmentById(appointmentId);
            return AppointmentResponse.success("Appointment cancelled successfully", updatedAppt);
        } else {
            return AppointmentResponse.internalError("Failed to cancel appointment");
        }
    }

    /**
     * Cancel an appointment - POST endpoint for backward compatibility
     */
    @PostMapping("/appointments/cancel")
    public StandardResponse cancelAppointmentPost(
            @RequestParam int appointmentId,
            HttpSession session) {
        System.out.println("\n[❌] POST /api/receptionist/appointments/cancel");

        User user = (User) session.getAttribute("user");
        if (!isReceptionist(user)) {
            return new StandardResponse(false, "Unauthorized - receptionist role required", null);
        }

        boolean success = appointmentService.cancelAppointment(appointmentId);

        if (success) {
            return new StandardResponse(true, "Appointment cancelled successfully", null);
        } else {
            return new StandardResponse(false, "Failed to cancel appointment", null);
        }
    }

    /**
     * Check in a patient for their appointment - use PUT for idempotent operation
     */
    @PutMapping("/appointments/{appointmentId}/check-in")
    public AppointmentResponse checkInAppointment(
            @PathVariable int appointmentId,
            HttpSession session) {
        System.out.println("\n[✅] PUT /api/receptionist/appointments/" + appointmentId + "/check-in");

        User user = (User) session.getAttribute("user");
        if (!isReceptionist(user)) {
            return AppointmentResponse.unauthorized();
        }

        // Check if appointment exists
        Map<String, Object> appt = appointmentService.getAppointmentById(appointmentId);
        if (appt == null) {
            return AppointmentResponse.notFound("Appointment not found");
        }

        String status = (String) appt.get("status");
        if (status.equals("completed") || status.equals("cancelled")) {
            return AppointmentResponse.conflict("Cannot check in appointment", 
                    "Appointment is " + status);
        }

        boolean success = appointmentService.checkInPatient(appointmentId);

        if (success) {
            Map<String, Object> updatedAppt = appointmentService.getAppointmentById(appointmentId);
            return AppointmentResponse.success("Patient checked in successfully", updatedAppt);
        } else {
            return AppointmentResponse.internalError("Failed to check in patient");
        }
    }

    /**
     * Check in a patient for their appointment - POST endpoint for backward compatibility
     */
    @PostMapping("/appointments/check-in")
    public StandardResponse checkInAppointmentPost(
            @RequestParam int appointmentId,
            HttpSession session) {
        System.out.println("\n[✅] POST /api/receptionist/appointments/check-in");

        User user = (User) session.getAttribute("user");
        if (!isReceptionist(user)) {
            return new StandardResponse(false, "Unauthorized - receptionist role required", null);
        }

        boolean success = appointmentService.checkInPatient(appointmentId);

        if (success) {
            return new StandardResponse(true, "Patient checked in successfully", null);
        } else {
            return new StandardResponse(false, "Failed to check in patient", null);
        }
    }

    /**
     * Get available doctors
     */
    @GetMapping("/appointments/doctors")
    public StandardResponse getAvailableDoctors(HttpSession session) {
        System.out.println("\n[👨‍⚕️] GET /api/receptionist/appointments/doctors");

        User user = (User) session.getAttribute("user");
        if (!isReceptionist(user)) {
            return new StandardResponse(false, "Unauthorized - receptionist role required", null);
        }

        var doctors = appointmentService.getAvailableDoctors();
        return new StandardResponse(true, "Doctors retrieved successfully", doctors);
    }

    // ===================== BILLING ENDPOINTS =====================

    /**
     * Get all bills (with optional status filter)
     */
    @GetMapping("/bills")
    public StandardResponse getBills(
            @RequestParam(required = false) String status,
            HttpSession session) {
        System.out.println("\n[💳] GET /api/receptionist/bills");

        User user = (User) session.getAttribute("user");
        if (!isReceptionist(user)) {
            return new StandardResponse(false, "Unauthorized - receptionist role required", null);
        }

        List<Invoice> bills = billingService.getAllInvoices(status);
        return new StandardResponse(true, "Bills retrieved successfully", bills);
    }

    /**
     * Generate a new bill
     */
    @PostMapping("/bills/generate")
    public StandardResponse generateBill(
            @RequestParam int patientId,
            @RequestParam String patientName,
            @RequestParam int doctorId,
            @RequestParam String consultationType,
            @RequestParam double consultationAmount,
            @RequestParam(required = false, defaultValue = "0") double pharmacyAddons,
            @RequestParam(required = false, defaultValue = "none") String discountType,
            HttpSession session) {
        System.out.println("\n[📄] POST /api/receptionist/bills/generate");

        User user = (User) session.getAttribute("user");
        if (!isReceptionist(user)) {
            return new StandardResponse(false, "Unauthorized - receptionist role required", null);
        }

        Invoice invoice = billingService.generateBill(patientId, patientName, doctorId,
                consultationType, consultationAmount, pharmacyAddons, discountType);

        if (invoice != null) {
            return new StandardResponse(true, "Bill generated successfully", invoice);
        } else {
            return new StandardResponse(false, "Failed to generate bill", null);
        }
    }

    /**
     * Record payment for a bill
     */
    @PostMapping("/bills/pay")
    public StandardResponse recordPayment(
            @RequestParam int invoiceId,
            @RequestParam String paymentMethod,
            @RequestParam double paidAmount,
            HttpSession session) {
        System.out.println("\n[💰] POST /api/receptionist/bills/pay");

        User user = (User) session.getAttribute("user");
        if (!isReceptionist(user)) {
            return new StandardResponse(false, "Unauthorized - receptionist role required", null);
        }

        boolean success = billingService.recordPayment(invoiceId, paymentMethod, paidAmount);

        if (success) {
            return new StandardResponse(true, "Payment recorded successfully", null);
        } else {
            return new StandardResponse(false, "Failed to record payment", null);
        }
    }

    // ===================== RECEPTIONIST PROFILE ENDPOINTS =====================

    /**
     * Get receptionist's own profile
     */
    @GetMapping("/profile")
    public StandardResponse getReceptionistProfile(HttpSession session) {
        System.out.println("\n[👤] GET /api/receptionist/profile");

        User user = (User) session.getAttribute("user");
        if (!isReceptionist(user)) {
            return new StandardResponse(false, "Unauthorized - receptionist role required", null);
        }

        User receptionistProfile = userService.getUserById(user.getId());
        return new StandardResponse(true, "Profile retrieved successfully", receptionistProfile);
    }

    /**
     * Update receptionist profile
     */
    @PostMapping("/profile/update")
    public StandardResponse updateProfile(
            @RequestParam String fullName,
            @RequestParam(required = false) String phone,
            HttpSession session) {
        System.out.println("\n[✏️] POST /api/receptionist/profile/update");

        User user = (User) session.getAttribute("user");
        if (!isReceptionist(user)) {
            return new StandardResponse(false, "Unauthorized - receptionist role required", null);
        }

        boolean success = userService.updateUserProfile(user.getId(), fullName, phone);

        if (success) {
            // Update session user object
            user.setFullName(fullName);
            session.setAttribute("user", user);
            return new StandardResponse(true, "Profile updated successfully", null);
        } else {
            return new StandardResponse(false, "Failed to update profile", null);
        }
    }

    /**
     * Change receptionist password
     */
    @PostMapping("/profile/password")
    public StandardResponse changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session) {
        System.out.println("\n[🔐] POST /api/receptionist/profile/password");

        User user = (User) session.getAttribute("user");
        if (!isReceptionist(user)) {
            return new StandardResponse(false, "Unauthorized - receptionist role required", null);
        }

        // Validate password match
        if (!newPassword.equals(confirmPassword)) {
            return new StandardResponse(false, "New passwords do not match", null);
        }

        // Validate password strength (min 8 chars)
        if (newPassword.length() < 8) {
            return new StandardResponse(false, "Password must be at least 8 characters", null);
        }

        boolean success = userService.changePassword(user.getId(), currentPassword, newPassword);

        if (success) {
            return new StandardResponse(true, "Password changed successfully", null);
        } else {
            return new StandardResponse(false, "Failed to change password - current password may be incorrect", null);
        }
    }

    // ===================== INNER CLASSES =====================

    /**
     * Standard API response format
     */
    public static class StandardResponse {
        public boolean success;
        public String message;
        public Object data;

        public StandardResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }
    }
}
