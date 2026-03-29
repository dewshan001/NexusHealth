package com.NexusHelth.dto;

import java.time.LocalDateTime;

/**
 * DTO for Financial Transactions
 */
public class TransactionDTO {
    
    private String transactionId;
    private String type;
    private String department;
    private Double amount;
    private String status;
    private LocalDateTime transactedAt;

    // Constructors
    public TransactionDTO() {
    }

    public TransactionDTO(String transactionId, String type, String department, 
                          Double amount, String status, LocalDateTime transactedAt) {
        this.transactionId = transactionId;
        this.type = type;
        this.department = department;
        this.amount = amount;
        this.status = status;
        this.transactedAt = transactedAt;
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTransactedAt() {
        return transactedAt;
    }

    public void setTransactedAt(LocalDateTime transactedAt) {
        this.transactedAt = transactedAt;
    }

    @Override
    public String toString() {
        return "TransactionDTO{" +
                "transactionId='" + transactionId + '\'' +
                ", type='" + type + '\'' +
                ", department='" + department + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                ", transactedAt=" + transactedAt +
                '}';
    }
}
