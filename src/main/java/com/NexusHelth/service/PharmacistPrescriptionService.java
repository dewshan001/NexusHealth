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

public class PharmacistPrescriptionService {

    public static class DispenseResult {
        public final boolean success;
        public final String message;

        public DispenseResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public List<Map<String, Object>> getDispensedPrescriptions(String startDate, String endDate, String patientName) {
        List<Map<String, Object>> dispensedPrescriptions = new ArrayList<>();

        // Build dynamic SQL query with optional filtering
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT p.id as prescription_id, p.issued_at, p.dispensed_at, p.status, " +
                        "pat.patient_code, u_pat.full_name as patient_name, " +
                        "u_doc.full_name as doctor_name, u_pharm.full_name as pharmacist_name " +
                        "FROM prescriptions p " +
                        "JOIN patients pat ON p.patient_id = pat.id " +
                        "JOIN users u_pat ON pat.user_id = u_pat.id " +
                        "JOIN doctors d ON p.doctor_id = d.id " +
                        "JOIN users u_doc ON d.user_id = u_doc.id " +
                        "LEFT JOIN users u_pharm ON p.dispensed_by = u_pharm.id " +
                        "WHERE p.status = 'dispensed'");

        // Add optional date filter
        if (startDate != null && !startDate.isEmpty()) {
            sqlBuilder.append(" AND DATE(p.dispensed_at) >= ?");
        }
        if (endDate != null && !endDate.isEmpty()) {
            sqlBuilder.append(" AND DATE(p.dispensed_at) <= ?");
        }

        // Add optional patient name filter
        if (patientName != null && !patientName.isEmpty()) {
            sqlBuilder.append(" AND u_pat.full_name LIKE ?");
        }

        sqlBuilder.append(" ORDER BY p.dispensed_at DESC");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {

            int paramIndex = 1;

            // Set date parameters
            if (startDate != null && !startDate.isEmpty()) {
                pstmt.setString(paramIndex++, startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                pstmt.setString(paramIndex++, endDate);
            }

            // Set patient name parameter
            if (patientName != null && !patientName.isEmpty()) {
                pstmt.setString(paramIndex, "%" + patientName + "%");
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> prescription = new HashMap<>();
                    int prescriptionId = rs.getInt("prescription_id");
                    prescription.put("id", prescriptionId);
                    prescription.put("orderId", "RX-" + (10000 + prescriptionId)); // Format RX-10024
                    prescription.put("patientName", rs.getString("patient_name"));
                    prescription.put("doctorName", "Dr. " + rs.getString("doctor_name"));
                    prescription.put("issuedAt", rs.getString("issued_at"));
                    prescription.put("dispensedAt", rs.getString("dispensed_at"));
                    prescription.put("dispensedBy", rs.getString("pharmacist_name"));
                    prescription.put("status", rs.getString("status"));
                    prescription.put("items", getPrescriptionItems(prescriptionId, conn));

                    dispensedPrescriptions.add(prescription);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dispensedPrescriptions;
    }

    public List<Map<String, Object>> getPendingPrescriptions() {
        List<Map<String, Object>> pendingPrescriptions = new ArrayList<>();

        // Single query to get all pending prescriptions
        String sql = "SELECT p.id as prescription_id, p.issued_at, p.status, " +
                "pat.patient_code, u_pat.full_name as patient_name, " +
                "u_doc.full_name as doctor_name " +
                "FROM prescriptions p " +
                "JOIN patients pat ON p.patient_id = pat.id " +
                "JOIN users u_pat ON pat.user_id = u_pat.id " +
                "JOIN doctors d ON p.doctor_id = d.id " +
                "JOIN users u_doc ON d.user_id = u_doc.id " +
                "WHERE p.status = 'pending' " +
                "ORDER BY p.issued_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> prescription = new HashMap<>();
                int prescriptionId = rs.getInt("prescription_id");
                prescription.put("id", prescriptionId);
                prescription.put("orderId", "RX-" + (10000 + prescriptionId)); // Format RX-10024
                prescription.put("patientName", rs.getString("patient_name"));
                prescription.put("doctorName", "Dr. " + rs.getString("doctor_name"));
                prescription.put("issuedAt", rs.getString("issued_at"));
                prescription.put("status", rs.getString("status"));
                prescription.put("items", getPrescriptionItems(prescriptionId, conn));

                pendingPrescriptions.add(prescription);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pendingPrescriptions;
    }

    private List<Map<String, Object>> getPrescriptionItems(int prescriptionId, Connection conn) throws SQLException {
        List<Map<String, Object>> items = new ArrayList<>();
        String sql = "SELECT pi.dosage, pi.frequency, pi.instructions, pi.quantity, m.name as medicine_name, m.unit_price " +
                "FROM prescription_items pi " +
                "JOIN medicines m ON pi.medicine_id = m.id " +
                "WHERE pi.prescription_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, prescriptionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("medicineName", rs.getString("medicine_name"));
                    item.put("dosage", rs.getString("dosage"));
                    item.put("frequency", rs.getString("frequency"));
                    item.put("instructions", rs.getString("instructions"));
                    int quantity = rs.getInt("quantity");
                    double unitPrice = rs.getDouble("unit_price");
                    item.put("quantity", quantity);
                    item.put("unitPrice", unitPrice);
                    item.put("lineTotal", quantity * unitPrice);
                    items.add(item);
                }
            }
        }
        return items;
    }

    public DispenseResult dispensePrescription(int prescriptionId, int pharmacistId) {
        String checkStatusSql = "SELECT status FROM prescriptions WHERE id = ?";
        String getItemsSql = "SELECT medicine_id, quantity FROM prescription_items WHERE prescription_id = ?";
        String updateStockSql = "UPDATE medicines SET stock_level = stock_level - ? WHERE id = ? AND stock_level >= ?";
        String updatePrescriptionSql = "UPDATE prescriptions SET status = 'dispensed', dispensed_at = CURRENT_TIMESTAMP, dispensed_by = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try {
                // 1. Check if it's pending
                try (PreparedStatement checkStmt = conn.prepareStatement(checkStatusSql)) {
                    checkStmt.setInt(1, prescriptionId);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            if (!"pending".equals(rs.getString("status"))) {
                                conn.rollback();
                                return new DispenseResult(false, "Prescription is not pending");
                            }
                        } else {
                            conn.rollback();
                            return new DispenseResult(false, "Prescription not found");
                        }
                    }
                }

                // 2. Identify items and deduct stock
                List<int[]> medQuantities = new ArrayList<>();
                try (PreparedStatement getItemsStmt = conn.prepareStatement(getItemsSql)) {
                    getItemsStmt.setInt(1, prescriptionId);
                    try (ResultSet rs = getItemsStmt.executeQuery()) {
                        while (rs.next()) {
                            medQuantities.add(new int[] { rs.getInt("medicine_id"), rs.getInt("quantity") });
                        }
                    }
                }

                if (medQuantities.isEmpty()) {
                    conn.rollback();
                    return new DispenseResult(false, "Prescription has no items");
                }

                try (PreparedStatement updateStockStmt = conn.prepareStatement(updateStockSql)) {
                    for (int[] mq : medQuantities) {
                        int medicineId = mq[0];
                        int quantity = mq[1];

                        if (quantity <= 0) {
                            conn.rollback();
                            return new DispenseResult(false, "Invalid prescription item quantity");
                        }

                        updateStockStmt.setInt(1, quantity); // subtract
                        updateStockStmt.setInt(2, medicineId);
                        updateStockStmt.setInt(3, quantity); // guard
                        int rows = updateStockStmt.executeUpdate();
                        if (rows <= 0) {
                            conn.rollback();
                            return new DispenseResult(false, "Insufficient stock to dispense one or more items");
                        }
                    }
                }

                // 3. Mark as dispensed
                try (PreparedStatement updatePrescriptionStmt = conn.prepareStatement(updatePrescriptionSql)) {
                    updatePrescriptionStmt.setInt(1, pharmacistId);
                    updatePrescriptionStmt.setInt(2, prescriptionId);
                    int rows = updatePrescriptionStmt.executeUpdate();
                    if (rows <= 0) {
                        conn.rollback();
                        return new DispenseResult(false, "Failed to update prescription status");
                    }
                }

                conn.commit(); // Commit transaction

                return new DispenseResult(true, "Prescription dispensed successfully");

            } catch (SQLException e) {
                conn.rollback(); // Rollback on error
                e.printStackTrace();
                return new DispenseResult(false, "Failed to dispense prescription");
            } finally {
                conn.setAutoCommit(true); // Reset auto-commit
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new DispenseResult(false, "Failed to dispense prescription");
        }
    }
}
