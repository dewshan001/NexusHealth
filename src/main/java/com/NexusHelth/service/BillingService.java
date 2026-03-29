package com.NexusHelth.service;

import com.NexusHelth.model.Invoice;
import com.NexusHelth.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class BillingService {

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Discount percentages
    private static final double SENIOR_DISCOUNT = 0.15;
    private static final double STANDARD_INSURANCE_DISCOUNT = 0.10;
    private static final double PREMIUM_INSURANCE_DISCOUNT = 0.25;

    /**
     * Generate a new bill/invoice for a patient (legacy method - now uses prescription-based invoicing)
     * This is deprecated - use PharmacistInvoiceService for prescription-based billing
     */
    @Deprecated
    public Invoice generateBill(int patientId, String patientName, int doctorId, 
                               String consultationType, double consultationAmount, 
                               double pharmacyAddons, String discountType) {
        System.out.println("\n📝 BILLING SERVICE: Generating bill for patient ID: " + patientId);

        // Create invoice with new model
        double subtotal = consultationAmount + pharmacyAddons;
        double discountAmount = calculateDiscount(subtotal, discountType);
        double total = subtotal - discountAmount;

        Invoice invoice = new Invoice(0, patientId, 0, total, discountAmount, "unpaid");
        
        System.out.println("✅ Bill prepared (Note: Use PharmacistInvoiceService for database persistence)");
        System.out.println("   Total Amount: $" + String.format("%.2f", total));

        return invoice;
    }

    /**
     * Get all invoices with optional status filter
     */
    public List<Invoice> getAllInvoices(String statusFilter) {
        List<Invoice> invoices = new ArrayList<>();
        System.out.println("📊 BILLING SERVICE: Fetching invoices (status: " + (statusFilter != null ? statusFilter : "all") + ")");

        String query = "SELECT id, patient_id, total_amount, discount_amount, status, created_at FROM invoices";

        if (statusFilter != null && !statusFilter.isEmpty()) {
            query += " WHERE status = ?";
        }
        query += " ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (statusFilter != null && !statusFilter.isEmpty()) {
                pstmt.setString(1, statusFilter);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Invoice invoice = new Invoice();
                    invoice.setId(rs.getInt("id"));
                    invoice.setPatientId(rs.getInt("patient_id"));
                    invoice.setTotalAmount(rs.getDouble("total_amount"));
                    invoice.setDiscount(rs.getDouble("discount_amount"));
                    invoice.setPaymentStatus(rs.getString("status"));
                    String createdAt = rs.getString("created_at");
                    if (createdAt != null && !createdAt.isEmpty()) {
                        try {
                            invoice.setCreatedAt(LocalDateTime.parse(createdAt, dateFormatter));
                        } catch (Exception e) {
                            System.out.println("⚠️ Warning: Could not parse created_at date: " + createdAt);
                        }
                    }
                    invoices.add(invoice);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching invoices: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("✅ Retrieved " + invoices.size() + " invoices");
        return invoices;
    }

    /**
     * Get all bills for receptionist billing table.
     * Returns objects shaped for the frontend in receptionist-dashboard.html.
     */
    public List<Map<String, Object>> getReceptionistBills(String statusFilter) {
        List<Map<String, Object>> bills = new ArrayList<>();

        String query = "SELECT id, invoice_number, patient_name, consultation_type, " +
                "consultation_amount, pharmacy_addons, subtotal, discount_type, discount_amount, total_amount, " +
                "status, payment_method, created_at, paid_at " +
                "FROM invoices";

        boolean hasStatusFilter = statusFilter != null && !statusFilter.isBlank();
        if (hasStatusFilter) {
            query += " WHERE status = ?";
        }
        query += " ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (hasStatusFilter) {
                pstmt.setString(1, statusFilter.trim().toLowerCase());
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> bill = new HashMap<>();
                    bill.put("id", rs.getInt("id"));
                    String invoiceNumber = rs.getString("invoice_number");
                    bill.put("invoiceNumber", invoiceNumber != null ? invoiceNumber : "—");

                    String patientName = rs.getString("patient_name");
                    bill.put("patientName", patientName != null ? patientName : "—");

                    String consultationType = rs.getString("consultation_type");
                    bill.put("consultationType", consultationType != null ? consultationType : "Payment");
                    bill.put("consultationAmount", rs.getDouble("consultation_amount"));
                    bill.put("pharmacyAddons", rs.getDouble("pharmacy_addons"));
                    bill.put("subtotal", rs.getDouble("subtotal"));
                    String discountType = rs.getString("discount_type");
                    bill.put("discountType", discountType != null ? discountType : "none");
                    bill.put("discountAmount", rs.getDouble("discount_amount"));
                    bill.put("totalAmount", rs.getDouble("total_amount"));
                    String status = rs.getString("status");
                    bill.put("status", status != null ? status : "unpaid");

                    String paymentMethod = rs.getString("payment_method");
                    bill.put("paymentMethod", paymentMethod);

                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        bill.put("createdAt", createdAt.toLocalDateTime().toString());
                    }
                    Timestamp paidAt = rs.getTimestamp("paid_at");
                    if (paidAt != null) {
                        bill.put("paidAt", paidAt.toLocalDateTime().toString());
                    }

                    bills.add(bill);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching receptionist bills: " + e.getMessage());
            e.printStackTrace();
        }

        return bills;
    }

    /**
     * Record payment for an invoice
     */
    public boolean recordPayment(int invoiceId, String paymentMethod, double paidAmount) {
        System.out.println("\n💳 BILLING SERVICE: Recording payment for invoice ID: " + invoiceId);

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            System.out.println("❌ Payment method is required");
            return false;
        }
        if (paidAmount <= 0) {
            System.out.println("❌ Paid amount must be greater than 0");
            return false;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String invoiceNumber = null;
            String consultationType = null;
            String status = null;
            Double totalAmount = null;
            Integer doctorId = null;

            String invoiceQuery = "SELECT invoice_number, consultation_type, total_amount, status, doctor_id FROM invoices WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(invoiceQuery)) {
                pstmt.setInt(1, invoiceId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("❌ Invoice not found");
                        conn.rollback();
                        return false;
                    }
                    invoiceNumber = rs.getString("invoice_number");
                    consultationType = rs.getString("consultation_type");
                    totalAmount = rs.getDouble("total_amount");
                    status = rs.getString("status");
                    int dId = rs.getInt("doctor_id");
                    doctorId = rs.wasNull() ? null : dId;
                }
            }

            if (status != null && status.equalsIgnoreCase("paid")) {
                System.out.println("ℹ️ Invoice is already paid");
                conn.rollback();
                return false;
            }
            if (totalAmount == null || totalAmount <= 0) {
                System.out.println("❌ Invalid invoice total amount");
                conn.rollback();
                return false;
            }

            // This system tracks payments as paid/unpaid (no partial payments in schema).
            // Enforce full payment to prevent inconsistent reporting.
            if (Math.abs(paidAmount - totalAmount) > 0.01) {
                System.out.println("❌ Paid amount must equal invoice total amount (expected: " + String.format("%.2f", totalAmount) + ")");
                conn.rollback();
                return false;
            }

            String specialization = null;
            if (doctorId != null) {
                try (PreparedStatement pstmt = conn.prepareStatement("SELECT specialization FROM doctors WHERE id = ?")) {
                    pstmt.setInt(1, doctorId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            specialization = rs.getString("specialization");
                        }
                    }
                }
            }

            String department = (specialization == null || specialization.trim().isEmpty()) ? "Reception" : specialization;
            String type = (consultationType == null || consultationType.trim().isEmpty()) ? "Payment" : consultationType;
            String transactionCode = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            String updateInvoiceSql = "UPDATE invoices SET status = 'paid', payment_method = ?, paid_at = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateInvoiceSql)) {
                pstmt.setString(1, paymentMethod);
                pstmt.setInt(2, invoiceId);
                int updated = pstmt.executeUpdate();
                if (updated <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            String insertTxnSql = "INSERT INTO transactions (invoice_id, transaction_code, type, department, amount, status, transacted_at) " +
                    "VALUES (?, ?, ?, ?, ?, 'settled', CURRENT_TIMESTAMP)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertTxnSql)) {
                pstmt.setInt(1, invoiceId);
                pstmt.setString(2, transactionCode);
                pstmt.setString(3, type);
                pstmt.setString(4, department);
                pstmt.setDouble(5, paidAmount);
                pstmt.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ Payment recorded successfully");
            if (invoiceNumber != null) {
                System.out.println("   Invoice: " + invoiceNumber);
            }
            System.out.println("   Amount: Rs." + String.format("%.2f", paidAmount));
            System.out.println("   Method: " + paymentMethod);
            System.out.println("   Transaction: " + transactionCode);
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Error recording payment: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.out.println("⚠️ Error rolling back payment transaction: " + rollbackEx.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.out.println("⚠️ Error closing connection: " + closeEx.getMessage());
                }
            }
        }

        return false;
    }

    /**
     * Get invoice by ID
     */
    public Invoice getInvoiceById(int invoiceId) {
        String query = "SELECT id, prescription_id, patient_id, appointment_id, total_amount, " +
                "discount, amount_paid, payment_status, created_at FROM invoices WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, invoiceId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Invoice(
                            rs.getInt("id"),
                            rs.getInt("prescription_id"),
                            rs.getInt("patient_id"),
                            rs.getInt("appointment_id"),
                            rs.getDouble("total_amount"),
                            rs.getDouble("discount"),
                            rs.getDouble("amount_paid"),
                            rs.getString("payment_status"),
                            LocalDateTime.parse(rs.getString("created_at"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching invoice: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Calculate discount based on discount type
     */
    public double calculateDiscount(double amount, String discountType) {
        if (discountType == null || discountType.isEmpty() || discountType.equals("none")) {
            return 0.0;
        }

        switch (discountType.toLowerCase()) {
            case "senior":
            case "senior_citizen":
                return amount * SENIOR_DISCOUNT;
            case "standard_insurance":
                return amount * STANDARD_INSURANCE_DISCOUNT;
            case "premium_insurance":
                return amount * PREMIUM_INSURANCE_DISCOUNT;
            default:
                return 0.0;
        }
    }

    /**
     * Generate unique invoice number
     */
    private String generateInvoiceNumber() {
        String query = "SELECT COUNT(*) as count FROM invoices";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                int count = rs.getInt("count") + 1;
                return String.format("INV-%05d", count);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error generating invoice number: " + e.getMessage());
        }
        return "INV-" + System.currentTimeMillis();
    }
}
