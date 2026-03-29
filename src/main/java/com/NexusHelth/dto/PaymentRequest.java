package com.NexusHelth.dto;

/**
 * DTO for payment form submission
 */
public class PaymentRequest {
    
    private String cardholderName;
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private double appointmentFee;

    // Constructors
    public PaymentRequest() {
    }

    public PaymentRequest(String cardholderName, String cardNumber, String expiryDate, 
                         String cvv, double appointmentFee) {
        this.cardholderName = cardholderName;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.appointmentFee = appointmentFee;
    }

    // Getters and Setters
    public String getCardholderName() {
        return cardholderName;
    }

    public void setCardholderName(String cardholderName) {
        this.cardholderName = cardholderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public double getAppointmentFee() {
        return appointmentFee;
    }

    public void setAppointmentFee(double appointmentFee) {
        this.appointmentFee = appointmentFee;
    }

    // Validation methods
    public boolean isValidCardNumber() {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return false;
        }
        // Remove spaces and validate length (13-19 digits)
        String cleaned = cardNumber.replaceAll("\\s+", "");
        return cleaned.matches("^[0-9]{13,19}$");
    }

    public boolean isValidExpiryDate() {
        if (expiryDate == null || expiryDate.isEmpty()) {
            return false;
        }
        // Validate MM/YY format
        return expiryDate.matches("^(0[1-9]|1[0-2])/\\d{2}$");
    }

    public boolean isValidCvv() {
        if (cvv == null || cvv.isEmpty()) {
            return false;
        }
        // Validate 3-4 digits
        return cvv.matches("^[0-9]{3,4}$");
    }

    public boolean isValidCardholderName() {
        return cardholderName != null && !cardholderName.trim().isEmpty();
    }

    public boolean isValid() {
        return isValidCardholderName() && isValidCardNumber() && 
               isValidExpiryDate() && isValidCvv();
    }
}
