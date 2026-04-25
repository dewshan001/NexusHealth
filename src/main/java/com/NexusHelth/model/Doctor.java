package com.NexusHelth.model;

import java.io.Serializable;

public class Doctor implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private String fullName;
    private String specialization;
    private String profilePicture;
    private int consultationDurationMin;
    private String workingHoursStart;
    private String workingHoursEnd;
    private int yearsExperience;
    private double rating;
    private String availabilityStatus;
    private double consultationFee;

    public Doctor() {
    }

    public Doctor(int id, int userId, String fullName, String specialization) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.specialization = specialization;
    }

    public Doctor(int id, int userId, String fullName, String specialization, String profilePicture) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.specialization = specialization;
        this.profilePicture = profilePicture;
    }

    public Doctor(int id, int userId, String fullName, String specialization, String profilePicture,
                  int consultationDurationMin, String workingHoursStart, String workingHoursEnd,
                  int yearsExperience, double rating, String availabilityStatus, double consultationFee) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.specialization = specialization;
        this.profilePicture = profilePicture;
        this.consultationDurationMin = consultationDurationMin;
        this.workingHoursStart = workingHoursStart;
        this.workingHoursEnd = workingHoursEnd;
        this.yearsExperience = yearsExperience;
        this.rating = rating;
        this.availabilityStatus = availabilityStatus;
        this.consultationFee = consultationFee;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public int getConsultationDurationMin() {
        return consultationDurationMin;
    }

    public void setConsultationDurationMin(int consultationDurationMin) {
        this.consultationDurationMin = consultationDurationMin;
    }

    public String getWorkingHoursStart() {
        return workingHoursStart;
    }

    public void setWorkingHoursStart(String workingHoursStart) {
        this.workingHoursStart = workingHoursStart;
    }

    public String getWorkingHoursEnd() {
        return workingHoursEnd;
    }

    public void setWorkingHoursEnd(String workingHoursEnd) {
        this.workingHoursEnd = workingHoursEnd;
    }

    public int getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(int yearsExperience) {
        this.yearsExperience = yearsExperience;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }
}
