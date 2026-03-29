package com.NexusHelth.service;

import com.NexusHelth.model.PharmacistAlert;
import com.NexusHelth.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PharmacistAlertService {

    private static final int LOW_STOCK_THRESHOLD = 20;
    private static final int EXPIRY_WARNING_DAYS = 30;

    public PharmacistAlertService() {
    }

    public List<PharmacistAlert> getActiveAlerts() {
        refreshAlertsFromInventory();

        List<PharmacistAlert> alerts = new ArrayList<>();
        String query = "SELECT id, medicine_id, medicine_name, alert_type, severity, message, created_at, updated_at " +
                "FROM pharmacist_alerts WHERE is_active = 1 " +
                "ORDER BY CASE severity WHEN 'urgent' THEN 0 ELSE 1 END, updated_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                PharmacistAlert alert = new PharmacistAlert();
                alert.setId(rs.getInt("id"));
                alert.setMedicineId(rs.getInt("medicine_id"));
                alert.setMedicineName(rs.getString("medicine_name"));
                alert.setAlertType(rs.getString("alert_type"));
                alert.setSeverity(rs.getString("severity"));
                alert.setMessage(rs.getString("message"));
                alert.setCreatedAt(rs.getString("created_at"));
                alert.setUpdatedAt(rs.getString("updated_at"));
                alerts.add(alert);
            }
        } catch (SQLException e) {
            System.err.println("Error loading pharmacist alerts: " + e.getMessage());
            e.printStackTrace();
        }

        return alerts;
    }

    public void refreshAlertsFromInventory() {
        String inventoryQuery = "SELECT id, name, stock_level, expiry_date FROM medicines";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(inventoryQuery);
                ResultSet rs = pstmt.executeQuery()) {

            Map<String, AlertPayload> desiredAlerts = new HashMap<>();

            while (rs.next()) {
                int medicineId = rs.getInt("id");
                String medicineName = rs.getString("name");
                int stockLevel = rs.getInt("stock_level");
                String expiryDate = rs.getString("expiry_date");

                if (stockLevel <= LOW_STOCK_THRESHOLD) {
                    String severity = stockLevel <= 5 ? "urgent" : "warning";
                    String message = stockLevel <= 0
                            ? medicineName + " is out of stock. Restock immediately."
                            : medicineName + " is running low (" + stockLevel + " units remaining).";
                    desiredAlerts.put(buildKey(medicineId, "low_stock"),
                            new AlertPayload(medicineId, medicineName, "low_stock", severity, message));
                }

                ExpiryAlertInfo expiryInfo = getExpiryAlertInfo(expiryDate);
                if (expiryInfo != null && expiryInfo.shouldAlert) {
                    String severity = expiryInfo.daysToExpiry <= 7 ? "urgent" : "warning";
                    String message = medicineName + " expires in " + expiryInfo.humanText + ".";
                    desiredAlerts.put(buildKey(medicineId, "expiry_soon"),
                            new AlertPayload(medicineId, medicineName, "expiry_soon", severity, message));
                }
            }

            upsertDesiredAlerts(conn, desiredAlerts);
            deactivateOldAlerts(conn, desiredAlerts);

        } catch (SQLException e) {
            System.err.println("Error refreshing pharmacist alerts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void upsertDesiredAlerts(Connection conn, Map<String, AlertPayload> desiredAlerts) throws SQLException {
        String upsert = "INSERT INTO pharmacist_alerts (medicine_id, medicine_name, alert_type, severity, message, is_active, resolved_at, updated_at) "
                +
                "VALUES (?, ?, ?, ?, ?, 1, NULL, CURRENT_TIMESTAMP) " +
                "ON CONFLICT(medicine_id, alert_type) DO UPDATE SET " +
                "medicine_name = excluded.medicine_name, " +
                "severity = excluded.severity, " +
                "message = excluded.message, " +
                "is_active = 1, " +
                "resolved_at = NULL, " +
                "updated_at = CURRENT_TIMESTAMP";

        try (PreparedStatement pstmt = conn.prepareStatement(upsert)) {
            for (AlertPayload payload : desiredAlerts.values()) {
                pstmt.setInt(1, payload.medicineId);
                pstmt.setString(2, payload.medicineName);
                pstmt.setString(3, payload.alertType);
                pstmt.setString(4, payload.severity);
                pstmt.setString(5, payload.message);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private void deactivateOldAlerts(Connection conn, Map<String, AlertPayload> desiredAlerts) throws SQLException {
        String query = "SELECT id, medicine_id, alert_type FROM pharmacist_alerts WHERE is_active = 1";

        try (PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            List<Integer> toDeactivate = new ArrayList<>();
            while (rs.next()) {
                String key = buildKey(rs.getInt("medicine_id"), rs.getString("alert_type"));
                if (!desiredAlerts.containsKey(key)) {
                    toDeactivate.add(rs.getInt("id"));
                }
            }

            if (toDeactivate.isEmpty()) {
                return;
            }

            String update = "UPDATE pharmacist_alerts SET is_active = 0, resolved_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(update)) {
                for (Integer alertId : toDeactivate) {
                    updateStmt.setInt(1, alertId);
                    updateStmt.addBatch();
                }
                updateStmt.executeBatch();
            }
        }
    }

    private ExpiryAlertInfo getExpiryAlertInfo(String expiryDate) {
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            return null;
        }

        String value = expiryDate.trim();
        try {
            if (value.matches("^\\d{4}-\\d{2}$")) {
                YearMonth nowMonth = YearMonth.now();
                YearMonth expiryMonth = YearMonth.parse(value);
                long monthDiff = ChronoUnit.MONTHS.between(nowMonth, expiryMonth);

                // Month-based expiry fields should alert for the current or next month.
                if (monthDiff < 0 || monthDiff > 1) {
                    return null;
                }

                LocalDate expiryDateAtMonthEnd = expiryMonth.atEndOfMonth();
                long daysToExpiry = ChronoUnit.DAYS.between(LocalDate.now(), expiryDateAtMonthEnd);
                if (daysToExpiry < 0) {
                    return null;
                }

                String humanText = monthDiff == 0
                        ? "this month"
                        : "about 1 month";
                return new ExpiryAlertInfo(true, daysToExpiry, humanText);
            }

            if (value.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                LocalDate expiry = LocalDate.parse(value);
                long daysToExpiry = ChronoUnit.DAYS.between(LocalDate.now(), expiry);
                if (daysToExpiry < 0 || daysToExpiry > EXPIRY_WARNING_DAYS) {
                    return null;
                }

                return new ExpiryAlertInfo(true, daysToExpiry, daysToExpiry + " day(s)");
            }

            return null;
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String buildKey(int medicineId, String alertType) {
        return medicineId + "::" + alertType;
    }

    private void ensureAlertsTableExists() {
        String createTable = "CREATE TABLE IF NOT EXISTS pharmacist_alerts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "medicine_id INTEGER NOT NULL REFERENCES medicines(id) ON DELETE CASCADE," +
                "medicine_name TEXT NOT NULL," +
                "alert_type TEXT NOT NULL CHECK(alert_type IN ('expiry_soon','low_stock'))," +
                "severity TEXT NOT NULL CHECK(severity IN ('warning','urgent'))," +
                "message TEXT NOT NULL," +
                "is_active INTEGER NOT NULL DEFAULT 1," +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "resolved_at DATETIME," +
                "UNIQUE(medicine_id, alert_type)" +
                ")";

        String createIndex = "CREATE INDEX IF NOT EXISTS idx_pharmacist_alerts_active ON pharmacist_alerts(is_active, severity, updated_at)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement createStmt = conn.prepareStatement(createTable);
                PreparedStatement indexStmt = conn.prepareStatement(createIndex)) {

            createStmt.execute();
            indexStmt.execute();
        } catch (SQLException e) {
            System.err.println("Error ensuring pharmacist_alerts table exists: " + e.getMessage());
        }
    }

    private static class AlertPayload {
        int medicineId;
        String medicineName;
        String alertType;
        String severity;
        String message;

        AlertPayload(int medicineId, String medicineName, String alertType, String severity, String message) {
            this.medicineId = medicineId;
            this.medicineName = medicineName;
            this.alertType = alertType;
            this.severity = severity;
            this.message = message;
        }
    }

    private static class ExpiryAlertInfo {
        boolean shouldAlert;
        long daysToExpiry;
        String humanText;

        ExpiryAlertInfo(boolean shouldAlert, long daysToExpiry, String humanText) {
            this.shouldAlert = shouldAlert;
            this.daysToExpiry = daysToExpiry;
            this.humanText = humanText;
        }
    }
}
