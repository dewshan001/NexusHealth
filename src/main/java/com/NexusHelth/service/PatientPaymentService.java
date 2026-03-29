package com.NexusHelth.service;

import com.NexusHelth.dto.InvoiceResponse;
import com.NexusHelth.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service class for patient payment operations
 * Handles invoice retrieval, payment recording, and receipt generation
 */
public class PatientPaymentService {

    /**
     * Get all invoices for a specific patient
     * @param patientId The patient ID
     * @return List of InvoiceResponse objects
     */
    public List<InvoiceResponse> getPatientInvoices(int patientId) {
        List<InvoiceResponse> invoices = new ArrayList<>();
        String query = "SELECT i.id, i.invoice_number, i.patient_name, " +
                "COALESCE(u.full_name, 'N/A') as doctor_name, " +
                "i.consultation_type, i.consultation_amount, i.pharmacy_addons, " +
                "i.discount_amount, i.total_amount, i.status, i.created_at " +
                "FROM invoices i " +
                "LEFT JOIN doctors d ON i.doctor_id = d.id " +
                "LEFT JOIN users u ON d.user_id = u.id " +
                "WHERE i.patient_id = ? " +
                "ORDER BY i.created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, patientId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    InvoiceResponse invoice = new InvoiceResponse(
                            rs.getInt("id"),
                            rs.getString("invoice_number"),
                            rs.getString("patient_name"),
                            rs.getString("doctor_name"),
                            rs.getString("consultation_type"),
                            rs.getDouble("consultation_amount"),
                            rs.getDouble("pharmacy_addons"),
                            rs.getDouble("discount_amount"),
                            rs.getDouble("total_amount"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    
                    // Set additional fields if paid
                    if ("paid".equals(rs.getString("status"))) {
                        invoice.setAmountPaid(rs.getDouble("total_amount"));
                    }
                    
                    invoices.add(invoice);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching invoices for patient " + patientId + ": " + e.getMessage());
        }

        return invoices;
    }

    /**
     * Get a specific invoice with validation of patient ownership
     * @param invoiceId The invoice ID
     * @param patientId The patient ID (for ownership verification)
     * @return InvoiceResponse object or null if not found/not owned
     */
    public InvoiceResponse getInvoiceDetails(int invoiceId, int patientId) {
        String query = "SELECT i.id, i.invoice_number, i.patient_name, " +
                "COALESCE(u.full_name, 'N/A') as doctor_name, " +
                "i.consultation_type, i.consultation_amount, i.pharmacy_addons, " +
                "i.discount_amount, i.total_amount, i.status, i.payment_method, " +
                "i.created_at, i.paid_at, i.patient_id " +
                "FROM invoices i " +
                "LEFT JOIN doctors d ON i.doctor_id = d.id " +
                "LEFT JOIN users u ON d.user_id = u.id " +
                "WHERE i.id = ? AND i.patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, invoiceId);
            pstmt.setInt(2, patientId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    InvoiceResponse invoice = new InvoiceResponse(
                            rs.getInt("id"),
                            rs.getString("invoice_number"),
                            rs.getString("patient_name"),
                            rs.getString("doctor_name"),
                            rs.getString("consultation_type"),
                            rs.getDouble("consultation_amount"),
                            rs.getDouble("pharmacy_addons"),
                            rs.getDouble("discount_amount"),
                            rs.getDouble("total_amount"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    
                    invoice.setPaymentMethod(rs.getString("payment_method"));
                    
                    if ("paid".equals(rs.getString("status"))) {
                        invoice.setAmountPaid(rs.getDouble("total_amount"));
                        Timestamp paidAtTimestamp = rs.getTimestamp("paid_at");
                        if (paidAtTimestamp != null) {
                            invoice.setPaidAt(paidAtTimestamp.toLocalDateTime());
                        }
                    }
                    
                    return invoice;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching invoice " + invoiceId + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Validate that an invoice belongs to a patient
     * @param invoiceId The invoice ID
     * @param patientId The patient ID
     * @return true if invoice belongs to patient, false otherwise
     */
    public boolean validateInvoiceOwnership(int invoiceId, int patientId) {
        String query = "SELECT COUNT(*) as count FROM invoices WHERE id = ? AND patient_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, invoiceId);
            pstmt.setInt(2, patientId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error validating invoice ownership: " + e.getMessage());
        }

        return false;
    }

    /**
     * Check if an invoice is already paid
     * @param invoiceId The invoice ID
     * @return true if invoice is paid, false otherwise
     */
    public boolean isInvoicePaid(int invoiceId) {
        String query = "SELECT status FROM invoices WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, invoiceId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return "paid".equals(rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking invoice payment status: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get the total amount for an invoice
     * @param invoiceId The invoice ID
     * @return The total amount, or -1 if not found
     */
    public double getInvoiceTotalAmount(int invoiceId) {
        String query = "SELECT total_amount FROM invoices WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, invoiceId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_amount");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching invoice amount: " + e.getMessage());
        }

        return -1;
    }

    /**
     * Record a payment for an invoice
     * Creates a transaction record and updates invoice status to 'paid'
     * @param invoiceId The invoice ID
     * @param paymentMethod The payment method used
     * @param amountPaid The amount paid
     * @return Transaction code if successful, null otherwise
     */
    public String recordPayment(int invoiceId, String paymentMethod, double amountPaid) {
        String transactionCode = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Update invoice status to 'paid' and set payment method + paid_at timestamp
            String updateInvoiceQuery = "UPDATE invoices SET status = 'paid', " +
                    "payment_method = ?, paid_at = CURRENT_TIMESTAMP WHERE id = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(updateInvoiceQuery)) {
                pstmt.setString(1, paymentMethod);
                pstmt.setInt(2, invoiceId);
                pstmt.executeUpdate();
            }
            
            // Create transaction record
            String insertTransactionQuery = "INSERT INTO transactions " +
                    "(invoice_id, transaction_code, type, amount, status) " +
                    "VALUES (?, ?, 'payment', ?, 'settled')";
            
            try (PreparedStatement pstmt = conn.prepareStatement(insertTransactionQuery)) {
                pstmt.setInt(1, invoiceId);
                pstmt.setString(2, transactionCode);
                pstmt.setDouble(3, amountPaid);
                pstmt.executeUpdate();
            }
            
            conn.commit();
            System.out.println("✓ Payment recorded successfully. Transaction Code: " + transactionCode);
            return transactionCode;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error recording payment: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
        
        return null;
    }

    /**
     * Get transaction details for an invoice
     * Used for receipt generation
     * @param invoiceId The invoice ID
     * @return List of transaction details
     */
    public List<java.util.Map<String, Object>> getTransactionDetails(int invoiceId) {
        List<java.util.Map<String, Object>> transactions = new ArrayList<>();
        String query = "SELECT transaction_code, type, amount, status, transacted_at " +
                "FROM transactions WHERE invoice_id = ? ORDER BY transacted_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, invoiceId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, Object> transaction = new java.util.HashMap<>();
                    transaction.put("transactionCode", rs.getString("transaction_code"));
                    transaction.put("type", rs.getString("type"));
                    transaction.put("amount", rs.getDouble("amount"));
                    transaction.put("status", rs.getString("status"));
                    transaction.put("transactedAt", rs.getTimestamp("transacted_at").toLocalDateTime());
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transaction details: " + e.getMessage());
        }

        return transactions;
    }
}
