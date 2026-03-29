package com.NexusHelth.dto;

import java.time.LocalDateTime;

/**
 * DTO for payment confirmation response
 */
public class PaymentResponse {
    
    private Boolean success;
    private String message;
    private String transactionCode;
    private Integer invoiceId;
    private Double amountPaid;
    private String paymentMethod;
    private LocalDateTime transactionDate;
    private String error;

    // Constructors
    public PaymentResponse() {
    }

    public PaymentResponse(Boolean success, String message, String transactionCode, 
                          Integer invoiceId, Double amountPaid, String paymentMethod,
                          LocalDateTime transactionDate) {
        this.success = success;
        this.message = message;
        this.transactionCode = transactionCode;
        this.invoiceId = invoiceId;
        this.amountPaid = amountPaid;
        this.paymentMethod = paymentMethod;
        this.transactionDate = transactionDate;
    }

    public PaymentResponse(Boolean success, String message, String error) {
        this.success = success;
        this.message = message;
        this.error = error;
    }

    // Getters and Setters
    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public Integer getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
