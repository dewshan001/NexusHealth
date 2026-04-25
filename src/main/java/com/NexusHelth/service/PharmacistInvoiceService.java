package com.NexusHelth.service;

import com.NexusHelth.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PharmacistInvoiceService {

    /**
     * Generate an invoice for a given prescription
     * Uses the current invoices schema and returns existing invoice if already created.
     */
    public Map<String, Object> generateInvoiceForPrescription(int prescriptionId) {
        Map<String, Object> result = new HashMap<>();

        if (prescriptionId <= 0) {
            result.put("success", false);
            result.put("message", "Invalid prescription id");
            return result;
        }

        String prescriptionSql = "SELECT p.patient_id, u.full_name AS patient_name " +
                "FROM prescriptions p " +
                "JOIN patients pat ON p.patient_id = pat.id " +
                "JOIN users u ON pat.user_id = u.id " +
                "WHERE p.id = ?";

        String subtotalSql = "SELECT COALESCE(SUM(pi.quantity * m.unit_price), 0) AS subtotal " +
                "FROM prescription_items pi " +
                "JOIN medicines m ON pi.medicine_id = m.id " +
                "WHERE pi.prescription_id = ?";

        String existingInvoiceSql = "SELECT id FROM invoices WHERE prescription_id = ? LIMIT 1";

        String insertInvoiceSql = "INSERT INTO invoices (invoice_number, prescription_id, patient_id, doctor_id, patient_name, " +
                "consultation_type, consultation_amount, pharmacy_addons, subtotal, discount_type, discount_amount, total_amount, status) " +
                "VALUES (?, ?, ?, NULL, ?, ?, 0.0, ?, ?, 'none', 0.0, ?, 'unpaid')";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (!hasInvoicesPrescriptionIdColumn(conn)) {
                result.put("success", false);
                result.put("message", "Invoices schema is missing prescription_id column");
                return result;
            }

            conn.setAutoCommit(false);

            try {
                // Idempotent behavior: reuse existing invoice for this prescription.
                try (PreparedStatement existingStmt = conn.prepareStatement(existingInvoiceSql)) {
                    existingStmt.setInt(1, prescriptionId);
                    try (ResultSet rs = existingStmt.executeQuery()) {
                        if (rs.next()) {
                            int invoiceId = rs.getInt("id");
                            conn.rollback();
                            Map<String, Object> existing = getInvoiceById(invoiceId);
                            result.putAll(existing);
                            result.put("success", true);
                            result.put("invoiceId", invoiceId);
                            return result;
                        }
                    }
                }

                Integer patientId = null;
                String patientName = null;
                try (PreparedStatement pstmt = conn.prepareStatement(prescriptionSql)) {
                    pstmt.setInt(1, prescriptionId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            int pid = rs.getInt("patient_id");
                            patientId = rs.wasNull() ? null : pid;
                            patientName = rs.getString("patient_name");
                        }
                    }
                }

                if (patientId == null) {
                    conn.rollback();
                    result.put("success", false);
                    result.put("message", "Prescription not found");
                    return result;
                }

                double subtotal = 0.0;
                try (PreparedStatement pstmt = conn.prepareStatement(subtotalSql)) {
                    pstmt.setInt(1, prescriptionId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            subtotal = rs.getDouble("subtotal");
                        }
                    }
                }

                if (subtotal <= 0.0) {
                    conn.rollback();
                    result.put("success", false);
                    result.put("message", "Prescription has no billable items");
                    return result;
                }

                String invoiceNumber = generateInvoiceNumber(conn);
                double totalAmount = subtotal;

                int invoiceId = 0;
                try (PreparedStatement pstmt = conn.prepareStatement(insertInvoiceSql)) {
                    pstmt.setString(1, invoiceNumber);
                    pstmt.setInt(2, prescriptionId);
                    pstmt.setInt(3, patientId);
                    pstmt.setString(4, patientName != null ? patientName : "—");
                    pstmt.setString(5, "Pharmacy Prescription");
                    pstmt.setDouble(6, subtotal);
                    pstmt.setDouble(7, subtotal);
                    pstmt.setDouble(8, totalAmount);
                    pstmt.executeUpdate();

                    try (PreparedStatement idPstmt = conn.prepareStatement("SELECT last_insert_rowid() as id");
                         ResultSet idSet = idPstmt.executeQuery()) {
                        if (idSet.next()) {
                            invoiceId = idSet.getInt("id");
                        }
                    }
                }

                if (invoiceId == 0) {
                    result.put("success", false);
                    result.put("message", "Failed to create invoice");
                    conn.rollback();
                    return result;
                }

                conn.commit();

                Map<String, Object> saved = getInvoiceById(invoiceId);
                result.putAll(saved);
                result.put("success", true);
                result.put("invoiceId", invoiceId);
                result.put("prescriptionId", prescriptionId);
                result.put("patientId", patientId);
                result.put("totalAmount", totalAmount);
                result.put("invoiceNumber", invoiceNumber);
                return result;

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                result.put("success", false);
                result.put("message", "Database error: " + e.getMessage());
                return result;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Connection error: " + e.getMessage());
            return result;
        }
    }

    /**
     * Get invoice details with all items
     */
    public Map<String, Object> getInvoiceById(int invoiceId) {
        Map<String, Object> result = new HashMap<>();

        String invoiceSql = "SELECT i.id, i.invoice_number, i.prescription_id, i.patient_id, i.patient_name, " +
                "i.discount_amount, i.total_amount, i.status, i.created_at " +
                "FROM invoices i " +
                "WHERE i.id = ?";

        String itemsSql = "SELECT pi.id, pi.medicine_id, m.name AS medicine_name, pi.quantity, m.unit_price, " +
                "(pi.quantity * m.unit_price) AS line_total " +
                "FROM prescription_items pi " +
                "JOIN medicines m ON pi.medicine_id = m.id " +
                "WHERE pi.prescription_id = ? " +
                "ORDER BY pi.id";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get invoice
            try (PreparedStatement pstmt = conn.prepareStatement(invoiceSql)) {
                pstmt.setInt(1, invoiceId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int prescriptionId = rs.getInt("prescription_id");
                        int patientId = rs.getInt("patient_id");
                        double discount = rs.getDouble("discount_amount");
                        double totalAmount = rs.getDouble("total_amount");
                        String status = rs.getString("status");

                        result.put("id", rs.getInt("id"));
                        result.put("invoiceId", rs.getInt("id"));
                        result.put("invoiceNumber", rs.getString("invoice_number"));
                        result.put("prescriptionId", prescriptionId);
                        result.put("patientId", patientId);
                        result.put("patientName", rs.getString("patient_name"));
                        result.put("totalAmount", totalAmount);
                        result.put("total_amount", totalAmount);
                        result.put("discount", discount);
                        result.put("discountAmount", discount);
                        result.put("paymentStatus", status);
                        result.put("status", status);
                        result.put("createdAt", rs.getString("created_at"));

                        // Get items
                        List<Map<String, Object>> items = new ArrayList<>();
                        if (prescriptionId > 0) {
                            try (PreparedStatement itemPstmt = conn.prepareStatement(itemsSql)) {
                                itemPstmt.setInt(1, prescriptionId);
                                try (ResultSet itemRs = itemPstmt.executeQuery()) {
                                    while (itemRs.next()) {
                                        Map<String, Object> item = new HashMap<>();
                                        item.put("id", itemRs.getInt("id"));
                                        item.put("medicineId", itemRs.getInt("medicine_id"));
                                        item.put("medicineName", itemRs.getString("medicine_name"));
                                        item.put("quantity", itemRs.getInt("quantity"));
                                        item.put("unitPrice", itemRs.getDouble("unit_price"));
                                        item.put("lineTotal", itemRs.getDouble("line_total"));
                                        items.add(item);
                                    }
                                }
                            }
                        }
                        result.put("items", items);
                        result.put("found", true);
                    } else {
                        result.put("found", false);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            result.put("found", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Find the most recent invoice created for a prescription.
     * Returns the same shape as getInvoiceById().
     */
    public Map<String, Object> getInvoiceByPrescriptionId(int prescriptionId) {
        Map<String, Object> result = new HashMap<>();
        String sql = "SELECT id FROM invoices WHERE prescription_id = ? ORDER BY created_at DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Some deployments use the newer billing invoices schema (schema.sql) which does not
            // include prescription_id. In that case, there is no persisted invoice to fetch.
            boolean hasPrescriptionIdColumn = false;
            try (PreparedStatement pragmaStmt = conn.prepareStatement("PRAGMA table_info(invoices)");
                 ResultSet rs = pragmaStmt.executeQuery()) {
                while (rs.next()) {
                    String col = rs.getString("name");
                    if ("prescription_id".equalsIgnoreCase(col)) {
                        hasPrescriptionIdColumn = true;
                        break;
                    }
                }
            }

            if (!hasPrescriptionIdColumn) {
                result.put("found", false);
                return result;
            }

            pstmt.setInt(1, prescriptionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int invoiceId = rs.getInt("id");
                    return getInvoiceById(invoiceId);
                }
            }
            result.put("found", false);
        } catch (SQLException e) {
            result.put("found", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * Get all invoices created for a pharmacist (or all if pharmacistId is 0)
     */
    public List<Map<String, Object>> getAllInvoices(int limit) {
        List<Map<String, Object>> invoices = new ArrayList<>();

        String sql = "SELECT i.id, i.invoice_number, i.prescription_id, i.patient_id, i.total_amount, i.status, i.created_at, " +
                "COALESCE(i.patient_name, u.full_name, '—') as patient_name " +
                "FROM invoices i " +
                "LEFT JOIN patients p ON i.patient_id = p.id " +
                "LEFT JOIN users u ON p.user_id = u.id " +
                "ORDER BY i.created_at DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> invoice = new HashMap<>();
                    invoice.put("id", rs.getInt("id"));
                    invoice.put("invoiceNumber", rs.getString("invoice_number"));
                    invoice.put("prescriptionId", rs.getInt("prescription_id"));
                    invoice.put("patientId", rs.getInt("patient_id"));
                    invoice.put("patientName", rs.getString("patient_name"));
                    invoice.put("totalAmount", rs.getDouble("total_amount"));
                    invoice.put("paymentStatus", rs.getString("status"));
                    invoice.put("createdAt", rs.getString("created_at"));
                    invoices.add(invoice);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return invoices;
    }

    private boolean hasInvoicesPrescriptionIdColumn(Connection conn) {
        try (PreparedStatement pragmaStmt = conn.prepareStatement("PRAGMA table_info(invoices)");
             ResultSet rs = pragmaStmt.executeQuery()) {
            while (rs.next()) {
                String col = rs.getString("name");
                if ("prescription_id".equalsIgnoreCase(col)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    private String generateInvoiceNumber(Connection conn) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM invoices";
        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("count") + 1;
                return String.format("INV-%05d", count);
            }
        }
        return "INV-" + System.currentTimeMillis();
    }
}
