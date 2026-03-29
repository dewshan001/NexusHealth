package com.NexusHelth.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Invoice implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int prescriptionId;
    private int patientId;
    private int appointmentId;
    private double totalAmount;
    private double discount;
    private double amountPaid;
    private String paymentStatus;
    private LocalDateTime createdAt;
    private List<InvoiceItem> items;

    // Constructors
    public Invoice() {
    }

    public Invoice(int prescriptionId, int patientId, int appointmentId, 
                   double totalAmount, double discount, String paymentStatus) {
        this.prescriptionId = prescriptionId;
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.totalAmount = totalAmount;
        this.discount = discount;
        this.paymentStatus = paymentStatus;
    }

    public Invoice(int id, int prescriptionId, int patientId, int appointmentId,
                   double totalAmount, double discount, double amountPaid,
                   String paymentStatus, LocalDateTime createdAt) {
        this.id = id;
        this.prescriptionId = prescriptionId;
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.totalAmount = totalAmount;
        this.discount = discount;
        this.amountPaid = amountPaid;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItem> items) {
        this.items = items;
    }
}
