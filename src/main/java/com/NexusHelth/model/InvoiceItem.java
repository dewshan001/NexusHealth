package com.NexusHelth.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class InvoiceItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int invoiceId;
    private int medicineId;
    private String medicineName;
    private int quantity;
    private double unitPrice;
    private double lineTotal;
    private LocalDateTime createdAt;

    // Constructors
    public InvoiceItem() {
    }

    public InvoiceItem(int invoiceId, int medicineId, String medicineName,
                       int quantity, double unitPrice, double lineTotal) {
        this.invoiceId = invoiceId;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    public InvoiceItem(int id, int invoiceId, int medicineId, String medicineName,
                       int quantity, double unitPrice, double lineTotal, LocalDateTime createdAt) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(double lineTotal) {
        this.lineTotal = lineTotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
