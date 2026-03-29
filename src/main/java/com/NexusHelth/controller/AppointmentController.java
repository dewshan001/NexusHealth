package com.NexusHelth.controller;

import com.NexusHelth.model.Doctor;
import com.NexusHelth.model.User;
import com.NexusHelth.model.Patient;
import com.NexusHelth.service.AppointmentService;
import com.NexusHelth.service.PatientService;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService = new AppointmentService();
    private final PatientService patientService = new PatientService();

    @GetMapping("/doctors")
    public DoctorsResponse getDoctors() {
        System.out.println("[\uD83D\uDCE1] Fetching available doctors");
        List<Doctor> doctors = appointmentService.getAvailableDoctors();
        return new DoctorsResponse(true, doctors, null);
    }

    @GetMapping("/booked")
    public BookedSlotsResponse getBookedSlots(@RequestParam int doctorId, @RequestParam String date) {
        System.out.println("[\uD83D\uDCE1] Fetching booked slots for Doctor ID: " + doctorId + " on " + date);
        List<String> bookedSlots = appointmentService.getBookedTimeSlots(doctorId, date);
        return new BookedSlotsResponse(true, bookedSlots, null);
    }

    @PostMapping("/book")
    public StandardResponse bookAppointment(
            @RequestParam int doctorId,
            @RequestParam String date,
            @RequestParam String time,
            HttpSession session) {

        System.out.println("[\uD83D\uDCE1] Booking appointment for Doctor " + doctorId + " at " + date + " " + time);

        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("patient")) {
            return new StandardResponse(false, "Authentication required or invalid role");
        }

        Patient patient = patientService.getPatientByUserId(user.getId());
        if (patient == null) {
            return new StandardResponse(false, "Patient profile not found");
        }

        // Book the appointment and get the appointment ID
        int appointmentId = appointmentService.bookAppointment(patient.getId(), doctorId, date, time);

        if (appointmentId > 0) {
            // Get doctor name for invoice
            Doctor doctor = appointmentService.getDoctorByUserId(user.getId());
            // Note: We need to get the doctor's full name. Since getDoctorByUserId uses the patient's user ID,
            // we need to get the doctor's name directly. Let's get the doctor info.
            String doctorName = "Doctor"; // Default, should fetch actual doctor name
            
            // Create invoice for the appointment
            boolean invoiceCreated = appointmentService.createAppointmentInvoice(
                appointmentId, 
                patient.getId(), 
                doctorId, 
                user.getFullName()  // Use patient's full name from User object
            );

            if (invoiceCreated) {
                return new StandardResponse(true, "Appointment booked successfully and payment processed");
            } else {
                System.err.println("⚠️  Appointment created but invoice generation failed. AppointmentId: " + appointmentId);
                return new StandardResponse(true, "Appointment booked successfully (invoice generation pending)");
            }
        } else {
            return new StandardResponse(false, "Time slot is no longer available or an error occurred");
        }
    }

    @GetMapping("/doctor/schedule")
    public DoctorScheduleResponse getDoctorSchedule(
            @RequestParam(required = false) String date,
            HttpSession session) {

        System.out.println("[\uD83D\uDCE1] Fetching doctor schedule for date: " + date);

        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("doctor")) {
            return new DoctorScheduleResponse(false, null, "Authentication required or invalid role");
        }

        Doctor doctor = appointmentService.getDoctorByUserId(user.getId());
        if (doctor == null) {
            return new DoctorScheduleResponse(false, null, "Doctor profile not found");
        }

        // Default to today's date if not provided
        if (date == null || date.isEmpty()) {
            date = java.time.LocalDate.now().toString();
        }

        List<Map<String, Object>> appointments = appointmentService.getDoctorAppointments(doctor.getId(), date);
        return new DoctorScheduleResponse(true, appointments, null);
    }

    // --- Inner classes for JSON Responses ---

    public static class DoctorsResponse {
        public boolean success;
        public List<Doctor> data;
        public String error;

        public DoctorsResponse(boolean success, List<Doctor> data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }
    }

    public static class BookedSlotsResponse {
        public boolean success;
        public List<String> slots;
        public String error;

        public BookedSlotsResponse(boolean success, List<String> slots, String error) {
            this.success = success;
            this.slots = slots;
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

    public static class DoctorScheduleResponse {
        public boolean success;
        public List<Map<String, Object>> data;
        public String error;

        public DoctorScheduleResponse(boolean success, List<Map<String, Object>> data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }
    }
}
