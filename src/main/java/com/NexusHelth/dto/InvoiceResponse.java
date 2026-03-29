package com.NexusHelth.dto;

import java.time.LocalDateTime;

/**
 * DTO for invoice data returned to patient dashboard
 */
public class InvoiceResponse {
    
    private Integer invoiceId;
    private String invoiceNumber;
    private String patientName;
    private String doctorName;
    private String consultationType;
    private Double consultationAmount;
    private Double pharmacyAddons;
    private Double discountAmount;
    private Double totalAmount;
    private Double amountPaid;
    private String status; // 'unpaid', 'paid'
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    // Constructors
    public InvoiceResponse() {
    }

    public InvoiceResponse(Integer invoiceId, String invoiceNumber, String patientName, 
                          String doctorName, String consultationType, Double consultationAmount,
                          Double pharmacyAddons, Double discountAmount, Double totalAmount,
                          String status, LocalDateTime createdAt) {
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.consultationType = consultationType;
        this.consultationAmount = consultationAmount;
        this.pharmacyAddons = pharmacyAddons;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.amountPaid = status.equals("paid") ? totalAmount : 0.0;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Integer getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getConsultationType() {
        return consultationType;
    }

    public void setConsultationType(String consultationType) {
        this.consultationType = consultationType;
    }

    public Double getConsultationAmount() {
        return consultationAmount;
    }

    public void setConsultationAmount(Double consultationAmount) {
        this.consultationAmount = consultationAmount;
    }

    public Double getPharmacyAddons() {
        return pharmacyAddons;
    }

    public void setPharmacyAddons(Double pharmacyAddons) {
        this.pharmacyAddons = pharmacyAddons;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
