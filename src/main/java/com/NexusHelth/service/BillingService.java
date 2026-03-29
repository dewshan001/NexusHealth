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
     * Generate a new bill/invoice for a patient
     */
    public Invoice generateBill(int patientId, String patientName, int doctorId, 
                               String consultationType, double consultationAmount, 
                               double pharmacyAddons, String discountType) {
        System.out.println("\n📝 BILLING SERVICE: Generating bill for patient ID: " + patientId);

        Invoice invoice = new Invoice(patientId, doctorId, patientName, consultationType, 
                                     consultationAmount, pharmacyAddons, discountType);

        // Calculate subtotal
        double subtotal = consultationAmount + pharmacyAddons;
        invoice.setSubtotal(subtotal);

        // Calculate discount
        double discountAmount = calculateDiscount(subtotal, discountType);
        invoice.setDiscountAmount(discountAmount);

        // Calculate total
        double total = subtotal - discountAmount;
        invoice.setTotalAmount(total);

        // Generate invoice number
        String invoiceNumber = generateInvoiceNumber();
        invoice.setInvoiceNumber(invoiceNumber);

        // Set creation timestamp
        invoice.setCreatedAt(LocalDateTime.now().format(dateFormatter));

        // Save to database
        String query = "INSERT INTO invoices (patient_id, doctor_id, patient_name, consultation_type, " +
                "consultation_amount, pharmacy_addons, subtotal, discount_type, discount_amount, " +
                "total_amount, status, invoice_number, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'unpaid', ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, patientId);
            pstmt.setInt(2, doctorId);
            pstmt.setString(3, patientName);
            pstmt.setString(4, consultationType);
            pstmt.setDouble(5, consultationAmount);
            pstmt.setDouble(6, pharmacyAddons);
            pstmt.setDouble(7, subtotal);
            pstmt.setString(8, discountType);
            pstmt.setDouble(9, discountAmount);
            pstmt.setDouble(10, total);
            pstmt.setString(11, invoiceNumber);
            pstmt.setString(12, invoice.getCreatedAt());

            pstmt.executeUpdate();
            System.out.println("✅ Bill generated successfully: " + invoiceNumber);
            System.out.println("   Total Amount: $" + String.format("%.2f", total));

            return invoice;
        } catch (SQLException e) {
            System.out.println("❌ Error generating bill: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all invoices with optional status filter
     */
    public List<Invoice> getAllInvoices(String statusFilter) {
        List<Invoice> invoices = new ArrayList<>();
        System.out.println("📊 BILLING SERVICE: Fetching invoices (status: " + (statusFilter != null ? statusFilter : "all") + ")");

        String query = "SELECT id, patient_id, doctor_id, patient_name, consultation_type, " +
                "consultation_amount, pharmacy_addons, subtotal, discount_type, discount_amount, " +
                "total_amount, status, payment_method, created_at, paid_at, invoice_number " +
                "FROM invoices";

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
                    invoice.setDoctorId(rs.getInt("doctor_id"));
                    invoice.setPatientName(rs.getString("patient_name"));
                    invoice.setConsultationType(rs.getString("consultation_type"));
                    invoice.setConsultationAmount(rs.getDouble("consultation_amount"));
                    invoice.setPharmacyAddons(rs.getDouble("pharmacy_addons"));
                    invoice.setSubtotal(rs.getDouble("subtotal"));
                    invoice.setDiscountType(rs.getString("discount_type"));
                    invoice.setDiscountAmount(rs.getDouble("discount_amount"));
                    invoice.setTotalAmount(rs.getDouble("total_amount"));
                    invoice.setStatus(rs.getString("status"));
                    invoice.setPaymentMethod(rs.getString("payment_method"));
                    invoice.setCreatedAt(rs.getString("created_at"));
                    invoice.setPaidAt(rs.getString("paid_at"));
                    invoice.setInvoiceNumber(rs.getString("invoice_number"));

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

        String query = "UPDATE invoices SET status = 'paid', payment_method = ?, paid_at = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, paymentMethod);
            pstmt.setString(2, LocalDateTime.now().format(dateFormatter));
            pstmt.setInt(3, invoiceId);

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
        String query = "SELECT id, patient_id, doctor_id, patient_name, consultation_type, " +
                "consultation_amount, pharmacy_addons, subtotal, discount_type, discount_amount, " +
                "total_amount, status, payment_method, created_at, paid_at, invoice_number " +
                "FROM invoices WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, invoiceId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Invoice invoice = new Invoice();
                    invoice.setId(rs.getInt("id"));
                    invoice.setPatientId(rs.getInt("patient_id"));
                    invoice.setDoctorId(rs.getInt("doctor_id"));
                    invoice.setPatientName(rs.getString("patient_name"));
                    invoice.setConsultationType(rs.getString("consultation_type"));
                    invoice.setConsultationAmount(rs.getDouble("consultation_amount"));
                    invoice.setPharmacyAddons(rs.getDouble("pharmacy_addons"));
                    invoice.setSubtotal(rs.getDouble("subtotal"));
                    invoice.setDiscountType(rs.getString("discount_type"));
                    invoice.setDiscountAmount(rs.getDouble("discount_amount"));
                    invoice.setTotalAmount(rs.getDouble("total_amount"));
                    invoice.setStatus(rs.getString("status"));
                    invoice.setPaymentMethod(rs.getString("payment_method"));
                    invoice.setCreatedAt(rs.getString("created_at"));
                    invoice.setPaidAt(rs.getString("paid_at"));
                    invoice.setInvoiceNumber(rs.getString("invoice_number"));

                    return invoice;
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
