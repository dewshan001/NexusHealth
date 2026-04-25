package com.NexusHelth.controller;

import com.NexusHelth.model.Doctor;
import com.NexusHelth.model.Patient;
import com.NexusHelth.model.User;
import com.NexusHelth.service.AppointmentService;
import com.NexusHelth.service.PatientService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService = new AppointmentService();
    private final PatientService patientService = new PatientService();

    @GetMapping("/doctors")
    public DoctorsResponse getDoctors(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String availabilityStatus) {
        List<Doctor> doctors = appointmentService.getAvailableDoctors(search, specialization, minRating,
                availabilityStatus);
        return new DoctorsResponse(true, doctors, null);
    }

    @GetMapping("/doctors/{doctorId}/availability")
    public DoctorAvailabilityResponse getDoctorAvailability(
            @PathVariable int doctorId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<Map<String, Object>> data = appointmentService.getDoctorAvailabilityForRange(doctorId, startDate, endDate);
        return new DoctorAvailabilityResponse(true, data, null);
    }

    @GetMapping("/booked")
    public BookedSlotsResponse getBookedSlots(@RequestParam int doctorId, @RequestParam String date) {
        List<String> bookedSlots = appointmentService.getBookedTimeSlots(doctorId, date);
        return new BookedSlotsResponse(true, bookedSlots, null);
    }

    @PostMapping(value = "/book", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public StandardResponse bookAppointmentForm(
            @RequestParam int doctorId,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam(required = false) String symptoms,
            @RequestParam(required = false) String preferredLanguage,
            @RequestParam(required = false) String emergencyContactName,
            @RequestParam(required = false) String emergencyContactPhone,
            @RequestParam(required = false) Boolean consentAccepted,
            HttpSession session) {

        return doBookAppointment(
                doctorId,
                null,
                date,
                time,
                symptoms,
                preferredLanguage,
                emergencyContactName,
                emergencyContactPhone,
                consentAccepted,
                session);
    }

    @PostMapping(value = "/book", consumes = MediaType.APPLICATION_JSON_VALUE)
    public StandardResponse bookAppointmentJson(@RequestBody AppointmentBookingPayload payload, HttpSession session) {
        if (payload == null) {
            return new StandardResponse(false, "Invalid request payload");
        }
        return doBookAppointment(
                payload.doctorId == null ? 0 : payload.doctorId,
                payload.patientId,
                payload.appointmentDate,
                payload.appointmentTime,
                payload.notes,
                payload.preferredLanguage,
                payload.emergencyContactName,
                payload.emergencyContactPhone,
                payload.consentAccepted,
                session);
    }

    private StandardResponse doBookAppointment(
            int doctorId,
            Integer requestedPatientId,
            String date,
            String time,
            String notes,
            String preferredLanguage,
            String emergencyContactName,
            String emergencyContactPhone,
            Boolean consentAccepted,
            HttpSession session) {

        if (doctorId <= 0 || date == null || date.trim().isEmpty() || time == null || time.trim().isEmpty()) {
            return new StandardResponse(false, "Doctor, date and time are required");
        }

        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null) {
            return new StandardResponse(false, "Authentication required");
        }

        Patient patient;
        boolean receptionistBookingForPatient = "receptionist".equals(user.getRole())
                && requestedPatientId != null
                && requestedPatientId > 0;

        if (receptionistBookingForPatient) {
            patient = new Patient();
            patient.setId(requestedPatientId);
        } else if ("patient".equals(user.getRole())) {
            patient = patientService.getPatientByUserId(user.getId());
        } else {
            return new StandardResponse(false, "Only patient or receptionist can book appointments");
        }

        if (patient == null) {
            return new StandardResponse(false, "Patient profile not found");
        }

        boolean consent = consentAccepted == null || consentAccepted;
        int appointmentId = appointmentService.bookAppointmentDetailed(
                patient.getId(),
                doctorId,
                date,
                time,
                user.getId(),
                notes,
                preferredLanguage,
                emergencyContactName,
                emergencyContactPhone,
                consent);

        if (appointmentId <= 0) {
            return new StandardResponse(false, "Time slot is no longer available or an error occurred");
        }

        boolean invoiceCreated = appointmentService.createAppointmentInvoice(
                appointmentId,
                patient.getId(),
                doctorId,
                user.getFullName());

        if (invoiceCreated) {
            return new StandardResponse(true, "Appointment booked successfully and payment processed", appointmentId);
        }
        return new StandardResponse(true, "Appointment booked successfully (invoice generation pending)",
                appointmentId);
    }

    @GetMapping("/clinic-settings")
    public ClinicSettingsResponse getClinicSettings() {
        return new ClinicSettingsResponse(true, appointmentService.getClinicSettingsSummary(), null);
    }

    @PostMapping("/{appointmentId}/reschedule")
    public StandardResponse reschedulePatientAppointment(
            @PathVariable int appointmentId,
            @RequestParam String newDate,
            @RequestParam String newTime,
            HttpSession session) {

        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null || !"patient".equals(user.getRole())) {
            return new StandardResponse(false, "Authentication required or invalid role");
        }

        Patient patient = patientService.getPatientByUserId(user.getId());
        if (patient == null) {
            return new StandardResponse(false, "Patient profile not found");
        }

        Map<String, Object> appointmentData = appointmentService.getAppointmentById(appointmentId);
        if (appointmentData == null) {
            return new StandardResponse(false, "Appointment not found");
        }

        int apptPatientId = (Integer) appointmentData.get("patientId");
        if (apptPatientId != patient.getId()) {
            return new StandardResponse(false, "Unauthorized to reschedule this appointment");
        }

        boolean success = appointmentService.rescheduleAppointment(appointmentId, newDate, newTime);
        if (success) {
            return new StandardResponse(true, "Appointment rescheduled successfully");
        } else {
            return new StandardResponse(false, "Failed to reschedule. The time slot might be unavailable.");
        }
    }

    @GetMapping("/{appointmentId}")
    public AppointmentDetailsResponse getAppointment(@PathVariable int appointmentId, HttpSession session) {
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null) {
            return new AppointmentDetailsResponse(false, null, "Authentication required");
        }

        Map<String, Object> data = appointmentService.getAppointmentById(appointmentId);
        if (data == null) {
            return new AppointmentDetailsResponse(false, null, "Appointment not found");
        }
        return new AppointmentDetailsResponse(true, data, null);
    }

    @GetMapping("/doctor/schedule")
    public DoctorScheduleResponse getDoctorSchedule(@RequestParam(required = false) String date, HttpSession session) {
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null || !"doctor".equals(user.getRole())) {
            return new DoctorScheduleResponse(false, null, "Authentication required or invalid role");
        }

        Doctor doctor = appointmentService.getDoctorByUserId(user.getId());
        if (doctor == null) {
            return new DoctorScheduleResponse(false, null, "Doctor profile not found");
        }

        if (date == null || date.isEmpty()) {
            date = java.time.LocalDate.now().toString();
        }

        List<Map<String, Object>> appointments = appointmentService.getDoctorAppointments(doctor.getId(), date);
        return new DoctorScheduleResponse(true, appointments, null);
    }

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

    public static class DoctorAvailabilityResponse {
        public boolean success;
        public List<Map<String, Object>> data;
        public String error;

        public DoctorAvailabilityResponse(boolean success, List<Map<String, Object>> data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }
    }

    public static class BookedSlotsResponse {
        public boolean success;
        public List<String> slots;
        public List<String> bookedSlots;
        public String error;

        public BookedSlotsResponse(boolean success, List<String> slots, String error) {
            this.success = success;
            this.slots = slots;
            this.bookedSlots = slots;
            this.error = error;
        }
    }

    public static class StandardResponse {
        public boolean success;
        public String message;
        public Integer appointmentId;

        public StandardResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.appointmentId = null;
        }

        public StandardResponse(boolean success, String message, Integer appointmentId) {
            this.success = success;
            this.message = message;
            this.appointmentId = appointmentId;
        }
    }

    public static class AppointmentBookingPayload {
        public Integer patientId;
        public Integer doctorId;
        public String appointmentDate;
        public String appointmentTime;
        public String notes;
        public String preferredLanguage;
        public String emergencyContactName;
        public String emergencyContactPhone;
        public Boolean consentAccepted;
    }

    public static class ClinicSettingsResponse {
        public boolean success;
        public Map<String, Object> data;
        public String error;

        public ClinicSettingsResponse(boolean success, Map<String, Object> data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }
    }

    public static class AppointmentDetailsResponse {
        public boolean success;
        public Map<String, Object> data;
        public String error;

        public AppointmentDetailsResponse(boolean success, Map<String, Object> data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
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
