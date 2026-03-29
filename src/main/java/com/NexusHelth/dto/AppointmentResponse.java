package com.NexusHelth.dto;

/**
 * Standardized response object for appointment operations
 */
public class AppointmentResponse {
    
    private boolean success;
    private String message;
    private Object data;
    private String error;
    private int statusCode;

    // Constructors
    public AppointmentResponse() {
    }

    public AppointmentResponse(boolean success, String message, Object data, int statusCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.statusCode = statusCode;
    }

    public AppointmentResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.statusCode = success ? 200 : 400;
    }

    public AppointmentResponse(boolean success, String message, String error, int statusCode) {
        this.success = success;
        this.message = message;
        this.error = error;
        this.statusCode = statusCode;
    }

    // Factory methods for common responses
    public static AppointmentResponse success(String message, Object data) {
        return new AppointmentResponse(true, message, data, 200);
    }

    public static AppointmentResponse badRequest(String message, String error) {
        return new AppointmentResponse(false, message, error, 400);
    }

    public static AppointmentResponse notFound(String message) {
        return new AppointmentResponse(false, message, "Appointment not found", 404);
    }

    public static AppointmentResponse conflict(String message, String reason) {
        return new AppointmentResponse(false, message, reason, 409);
    }

    public static AppointmentResponse unauthorized() {
        return new AppointmentResponse(false, "Unauthorized", "Receptionist role required", 403);
    }

    public static AppointmentResponse internalError(String message) {
        return new AppointmentResponse(false, message, "Internal server error", 500);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
