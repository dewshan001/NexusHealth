package com.NexusHelth.model;

import java.io.Serializable;

public class Doctor implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private String fullName;
    private String specialization;

    public Doctor() {
    }

    public Doctor(int id, int userId, String fullName, String specialization) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.specialization = specialization;
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
}
