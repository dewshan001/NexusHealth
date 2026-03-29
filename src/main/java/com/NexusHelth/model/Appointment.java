package com.NexusHelth.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Appointment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int patientId;
    private int doctorId;
    private int bookedBy;
    private String appointmentDate;
    private String appointmentTime;
    private String status;
    private String notes;
    private LocalDateTime createdAt;

    // Constructors
    public Appointment() {
    }

    public Appointment(int patientId, int doctorId, String appointmentDate, String appointmentTime) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = "scheduled";
    }

    public Appointment(int patientId, int doctorId, int bookedBy, String appointmentDate, 
                      String appointmentTime, String status, String notes) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.bookedBy = bookedBy;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public int getBookedBy() {
        return bookedBy;
    }

    public void setBookedBy(int bookedBy) {
        this.bookedBy = bookedBy;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", appointmentDate='" + appointmentDate + '\'' +
                ", appointmentTime='" + appointmentTime + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
