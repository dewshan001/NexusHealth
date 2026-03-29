package com.NexusHelth.model;

import java.io.Serializable;

public class Invoice implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int patientId;
    private int doctorId;
    private String patientName;
    private String consultationType;
    private double consultationAmount;
    private double pharmacyAddons;
    private double subtotal;
    private String discountType;
    private double discountAmount;
    private double totalAmount;
    private String status; // unpaid, paid
    private String paymentMethod; // cash, credit_card
    private String createdAt;
    private String paidAt;
    private String invoiceNumber;

    public Invoice() {
    }

    public Invoice(int patientId, int doctorId, String patientName, String consultationType,
                   double consultationAmount, double pharmacyAddons, String discountType) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.consultationType = consultationType;
        this.consultationAmount = consultationAmount;
        this.pharmacyAddons = pharmacyAddons;
        this.discountType = discountType;
        this.status = "unpaid";
        this.subtotal = consultationAmount + pharmacyAddons;
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

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getConsultationType() {
        return consultationType;
    }

    public void setConsultationType(String consultationType) {
        this.consultationType = consultationType;
    }

    public double getConsultationAmount() {
        return consultationAmount;
    }

    public void setConsultationAmount(double consultationAmount) {
        this.consultationAmount = consultationAmount;
    }

    public double getPharmacyAddons() {
        return pharmacyAddons;
    }

    public void setPharmacyAddons(double pharmacyAddons) {
        this.pharmacyAddons = pharmacyAddons;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(String paidAt) {
        this.paidAt = paidAt;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
}
