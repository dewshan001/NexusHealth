package com.NexusHelth.dto;

/**
 * DTO for appointment request with input validation
 */
public class AppointmentRequest {
    
    private Integer appointmentId;
    private Integer patientId;
    private Integer doctorId;
    private String appointmentDate;
    private String appointmentTime;
    private String status;
    private String notes;

    // Constructors
    public AppointmentRequest() {
    }

    public AppointmentRequest(String appointmentDate, String appointmentTime) {
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
    }

    // Validation methods
    public boolean isValidDateFormat() {
        if (appointmentDate == null || appointmentDate.isEmpty()) {
            return false;
        }
        // Validate YYYY-MM-DD format
        return appointmentDate.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    public boolean isValidTimeFormat() {
        if (appointmentTime == null || appointmentTime.isEmpty()) {
            return false;
        }
        // Validate HH:mm format
        return appointmentTime.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
    }

    public boolean isValidAppointmentId() {
        return appointmentId != null && appointmentId > 0;
    }

    public boolean isValidPatientId() {
        return patientId != null && patientId > 0;
    }

    public boolean isValidDoctorId() {
        return doctorId != null && doctorId > 0;
    }

    public boolean isValidStatus() {
        if (status == null || status.isEmpty()) {
            return false;
        }
        return status.matches("^(scheduled|confirmed|completed|cancelled|no_show)$");
    }

    // Getters and Setters
    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
