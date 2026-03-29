package com.NexusHelth.service;

import com.NexusHelth.model.Invoice;
import com.NexusHelth.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

        String query = "SELECT id, prescription_id, patient_id, appointment_id, total_amount, " +
                "discount, amount_paid, payment_status, created_at FROM invoices";

        if (statusFilter != null && !statusFilter.isEmpty()) {
            query += " WHERE payment_status = ?";
        }
        query += " ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (statusFilter != null && !statusFilter.isEmpty()) {
                pstmt.setString(1, statusFilter);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Invoice invoice = new Invoice(
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
     * Record payment for an invoice
     */
    public boolean recordPayment(int invoiceId, String paymentMethod, double paidAmount) {
        System.out.println("\n💳 BILLING SERVICE: Recording payment for invoice ID: " + invoiceId);

        String query = "UPDATE invoices SET payment_status = 'paid', amount_paid = amount_paid + ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setDouble(1, paidAmount);
            pstmt.setInt(2, invoiceId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✅ Payment recorded successfully");
                System.out.println("   Amount: $" + String.format("%.2f", paidAmount));
                System.out.println("   Method: " + paymentMethod);
                return true;
            }
        } catch (SQLException e) {
            System.out.println("❌ Error recording payment: " + e.getMessage());
            e.printStackTrace();
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
