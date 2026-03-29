package com.NexusHelth.controller;

import com.NexusHelth.model.Doctor;
import com.NexusHelth.model.User;
import com.NexusHelth.service.AppointmentService;
import com.NexusHelth.service.ConsultationService;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationService consultationService = new ConsultationService();
    private final AppointmentService appointmentService = new AppointmentService();

    @GetMapping("/history")
    public ConsultationResponse getHistory(@RequestParam String patientCode, HttpSession session) {
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null || !user.getRole().equals("doctor")) {
            return new ConsultationResponse(false, null, "Authentication required or invalid role");
        }

        List<Map<String, Object>> history = consultationService.getPatientHistory(patientCode);
        return new ConsultationResponse(true, history, null);
    }

    @GetMapping("/doctor-history")
    public ConsultationResponse getDoctorHistory(HttpSession session) {
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null || !user.getRole().equals("doctor")) {
            return new ConsultationResponse(false, null, "Authentication required or invalid role");
        }

        Doctor doctor = appointmentService.getDoctorByUserId(user.getId());
        if (doctor == null) {
            return new ConsultationResponse(false, null, "Doctor profile not found");
        }

        List<Map<String, Object>> history = consultationService.getDoctorConsultationHistory(doctor.getId());
        return new ConsultationResponse(true, history, null);
    }

    @PostMapping("/save")
    public StandardResponse saveConsultation(
            @RequestParam int appointmentId,
            @RequestParam String patientCode,
            @RequestParam String diagnosis,
            @RequestParam String notes,
            HttpSession session) {

        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null || !user.getRole().equals("doctor")) {
            return new StandardResponse(false, "Authentication required or invalid role");
        }

        Doctor doctor = appointmentService.getDoctorByUserId(user.getId());
        if (doctor == null) {
            return new StandardResponse(false, "Doctor profile not found");
        }

        boolean success = consultationService.saveConsultation(appointmentId, doctor.getId(), patientCode, diagnosis,
                notes);

        if (success) {
            return new StandardResponse(true, "Consultation saved successfully");
        } else {
            return new StandardResponse(false, "Failed to save consultation");
        }
    }

    @GetMapping("/patient-vitals")
    public VitalsResponse getVitals(@RequestParam String patientCode, HttpSession session) {
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null || !user.getRole().equals("doctor")) {
            return new VitalsResponse(false, null, "Authentication required");
        }
        Map<String, Object> vitals = consultationService.getPatientVitals(patientCode);
        return new VitalsResponse(true, vitals, null);
    }

    @PostMapping("/patient-vitals")
    public StandardResponse updateVitals(
            @RequestParam String patientCode,
            @RequestParam(required = false) String bloodType,
            @RequestParam(required = false) String height,
            @RequestParam(required = false) String weight,
            @RequestParam(required = false) String heartRate,
            HttpSession session) {

        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null || !user.getRole().equals("doctor")) {
            return new StandardResponse(false, "Authentication required");
        }

        boolean success = consultationService.updatePatientVitals(patientCode, bloodType, height, weight, heartRate);
        if (success) {
            return new StandardResponse(true, "Vitals updated successfully");
        } else {
            return new StandardResponse(false, "Failed to update vitals");
        }
    }

    // --- Inner classes for JSON Responses ---

    public static class ConsultationResponse {
        public boolean success;
        public List<Map<String, Object>> data;
        public String error;

        public ConsultationResponse(boolean success, List<Map<String, Object>> data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }
    }

    public static class VitalsResponse {
        public boolean success;
        public Map<String, Object> data;
        public String error;

        public VitalsResponse(boolean success, Map<String, Object> data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }
    }

    public static class StandardResponse {
        public boolean success;
        public String message;

        public StandardResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}

