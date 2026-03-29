package com.NexusHelth.service;

import com.NexusHelth.model.Invoice;
import com.NexusHelth.model.InvoiceItem;
import com.NexusHelth.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PharmacistInvoiceService {

    /**
     * Generate an invoice for a given prescription
     * Fetches all prescription items with medicine details, calculates total, and saves invoice
     */
    public Map<String, Object> generateInvoiceForPrescription(int prescriptionId) {
        Map<String, Object> result = new HashMap<>();
        
        String getItemsSql = "SELECT pi.id, pi.medicine_id, pi.quantity, m.name as medicine_name, m.unit_price, " +
                "p.patient_id, p.consultation_id " +
                "FROM prescription_items pi " +
                "JOIN medicines m ON pi.medicine_id = m.id " +
                "JOIN prescriptions p ON pi.prescription_id = p.id " +
                "WHERE pi.prescription_id = ?";
        
        String getConsultationSql = "SELECT appointment_id FROM consultations WHERE id = ?";

        String getPatientNameSql = "SELECT u.full_name AS patient_name " +
            "FROM patients pat " +
            "JOIN users u ON pat.user_id = u.id " +
            "WHERE pat.id = ?";
        
        String insertInvoiceSql = "INSERT INTO invoices (prescription_id, patient_id, appointment_id, total_amount, " +
                "discount, payment_status, created_at) VALUES (?, ?, ?, ?, 0.0, 'unpaid', CURRENT_TIMESTAMP)";
        
        String insertInvoiceItemSql = "INSERT INTO invoice_items (invoice_id, medicine_id, medicine_name, quantity, " +
                "unit_price, line_total, created_at) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try {
                // 1. Get prescription items with prices
                List<Map<String, Object>> items = new ArrayList<>();
                double totalAmount = 0;
                int patientId = 0;
                int appointmentId = 0;
                
                try (PreparedStatement pstmt = conn.prepareStatement(getItemsSql)) {
                    pstmt.setInt(1, prescriptionId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            int medicineId = rs.getInt("medicine_id");
                            int quantity = rs.getInt("quantity");
                            String medicineName = rs.getString("medicine_name");
                            double unitPrice = rs.getDouble("unit_price");
                            double lineTotal = quantity * unitPrice;
                            
                            Map<String, Object> item = new HashMap<>();
                            item.put("medicineId", medicineId);
                            item.put("medicineName", medicineName);
                            item.put("quantity", quantity);
                            item.put("unitPrice", unitPrice);
                            item.put("lineTotal", lineTotal);
                            
                            items.add(item);
                            totalAmount += lineTotal;
                            
                            if (patientId == 0) {
                                patientId = rs.getInt("patient_id");
                                int consultationId = rs.getInt("consultation_id");
                                
                                // Get appointment_id from consultation
                                try (PreparedStatement consultStmt = conn.prepareStatement(getConsultationSql)) {
                                    consultStmt.setInt(1, consultationId);
                                    try (ResultSet consultRs = consultStmt.executeQuery()) {
                                        if (consultRs.next()) {
                                            appointmentId = consultRs.getInt("appointment_id");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                if (items.isEmpty()) {
                    result.put("success", false);
                    result.put("message", "No prescription items found");
                    return result;
                }

                // 2. Create invoice in database
                int invoiceId = 0;
                try (PreparedStatement pstmt = conn.prepareStatement(insertInvoiceSql)) {
                    pstmt.setInt(1, prescriptionId);
                    pstmt.setInt(2, patientId);
                    pstmt.setInt(3, appointmentId);
                    pstmt.setDouble(4, totalAmount);
                    
                    pstmt.executeUpdate();
                    
                    // SQLite doesn't support getGeneratedKeys(), so query the last inserted rowid
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

                // 3. Create invoice items
                try (PreparedStatement pstmt = conn.prepareStatement(insertInvoiceItemSql)) {
                    for (Map<String, Object> item : items) {
                        pstmt.setInt(1, invoiceId);
                        pstmt.setInt(2, (Integer) item.get("medicineId"));
                        pstmt.setString(3, (String) item.get("medicineName"));
                        pstmt.setInt(4, (Integer) item.get("quantity"));
                        pstmt.setDouble(5, (Double) item.get("unitPrice"));
                        pstmt.setDouble(6, (Double) item.get("lineTotal"));
                        
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }

                conn.commit();
                
                // Return invoice data
                result.put("success", true);
                result.put("invoiceId", invoiceId);
                result.put("prescriptionId", prescriptionId);
                result.put("patientId", patientId);
                result.put("appointmentId", appointmentId);
                result.put("totalAmount", totalAmount);
                result.put("items", items);

                // Helpful display fields
                try (PreparedStatement nameStmt = conn.prepareStatement(getPatientNameSql)) {
                    nameStmt.setInt(1, patientId);
                    try (ResultSet nameRs = nameStmt.executeQuery()) {
                        if (nameRs.next()) {
                            result.put("patientName", nameRs.getString("patient_name"));
                        }
                    }
                }
                
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
        
        String invoiceSql = "SELECT i.id, i.prescription_id, i.patient_id, i.appointment_id, " +
                "i.total_amount, i.discount, i.amount_paid, i.payment_status, i.created_at, " +
                "u.full_name as patient_name " +
                "FROM invoices i " +
                "JOIN patients p ON i.patient_id = p.id " +
                "JOIN users u ON p.user_id = u.id " +
                "WHERE i.id = ?";
        
        String itemsSql = "SELECT id, medicine_id, medicine_name, quantity, unit_price, line_total " +
                "FROM invoice_items WHERE invoice_id = ? ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get invoice
            try (PreparedStatement pstmt = conn.prepareStatement(invoiceSql)) {
                pstmt.setInt(1, invoiceId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        result.put("id", rs.getInt("id"));
                        result.put("prescriptionId", rs.getInt("prescription_id"));
                        result.put("patientId", rs.getInt("patient_id"));
                        result.put("patientName", rs.getString("patient_name"));
                        result.put("appointmentId", rs.getInt("appointment_id"));
                        result.put("totalAmount", rs.getDouble("total_amount"));
                        result.put("discount", rs.getDouble("discount"));
                        result.put("amountPaid", rs.getDouble("amount_paid"));
                        result.put("paymentStatus", rs.getString("payment_status"));
                        result.put("createdAt", rs.getString("created_at"));
                        
                        // Get items
                        List<Map<String, Object>> items = new ArrayList<>();
                        try (PreparedStatement itemPstmt = conn.prepareStatement(itemsSql)) {
                            itemPstmt.setInt(1, invoiceId);
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
        
        String sql = "SELECT i.id, i.prescription_id, i.patient_id, i.total_amount, i.payment_status, i.created_at, " +
                "u.full_name as patient_name " +
                "FROM invoices i " +
                "JOIN patients p ON i.patient_id = p.id " +
                "JOIN users u ON p.user_id = u.id " +
                "ORDER BY i.created_at DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> invoice = new HashMap<>();
                    invoice.put("id", rs.getInt("id"));
                    invoice.put("invoiceNumber", "INV-" + rs.getInt("id"));
                    invoice.put("prescriptionId", rs.getInt("prescription_id"));
                    invoice.put("patientId", rs.getInt("patient_id"));
                    invoice.put("patientName", rs.getString("patient_name"));
                    invoice.put("totalAmount", rs.getDouble("total_amount"));
                    invoice.put("paymentStatus", rs.getString("payment_status"));
                    invoice.put("createdAt", rs.getString("created_at"));
                    invoices.add(invoice);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return invoices;
    }
}
