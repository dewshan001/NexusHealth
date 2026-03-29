package com.NexusHelth.service;

import com.NexusHelth.model.Medicine;
import com.NexusHelth.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PharmacistInventoryService {

    public PharmacistInventoryService() {
    }

    public List<Medicine> getAllMedicines() {
        List<Medicine> items = new ArrayList<>();
        String query = "SELECT id, name, COALESCE(batch_number, '') AS batch_number, category, unit_price, stock_level, expiry_date, status FROM medicines ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Medicine medicine = new Medicine();
                medicine.setId(rs.getInt("id"));
                medicine.setName(rs.getString("name"));
                medicine.setBatchNumber(rs.getString("batch_number"));
                medicine.setCategory(rs.getString("category"));
                medicine.setUnitPrice(rs.getDouble("unit_price"));
                medicine.setStockLevel(rs.getInt("stock_level"));
                medicine.setExpiryDate(rs.getString("expiry_date"));

                String computedStatus = determineStatus(medicine.getStockLevel(), medicine.getExpiryDate());
                medicine.setStatus(computedStatus);
                updateStatusIfChanged(conn, medicine.getId(), rs.getString("status"), computedStatus);

                items.add(medicine);
            }
        } catch (SQLException e) {
            System.err.println("Error loading medicine inventory: " + e.getMessage());
            e.printStackTrace();
        }

        return items;
    }

    public boolean createMedicine(String name, String batchNumber, String category, double unitPrice, int stockLevel,
            String expiryDate) {
        String normalizedExpiry = normalizeExpiry(expiryDate);
        String status = determineStatus(stockLevel, normalizedExpiry);

        String query = "INSERT INTO medicines (name, batch_number, category, unit_price, stock_level, expiry_date, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, safeTrim(name));
            pstmt.setString(2, safeTrim(batchNumber));
            pstmt.setString(3, defaultIfBlank(category, "General"));
            pstmt.setDouble(4, Math.max(unitPrice, 0.0));
            pstmt.setInt(5, Math.max(stockLevel, 0));
            pstmt.setString(6, normalizedExpiry);
            pstmt.setString(7, status);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating medicine inventory item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateMedicine(int id, String name, String batchNumber, String category, double unitPrice,
            int stockLevel,
            String expiryDate) {
        String normalizedExpiry = normalizeExpiry(expiryDate);
        String status = determineStatus(stockLevel, normalizedExpiry);

        String query = "UPDATE medicines SET name = ?, batch_number = ?, category = ?, unit_price = ?, stock_level = ?, expiry_date = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, safeTrim(name));
            pstmt.setString(2, safeTrim(batchNumber));
            pstmt.setString(3, defaultIfBlank(category, "General"));
            pstmt.setDouble(4, Math.max(unitPrice, 0.0));
            pstmt.setInt(5, Math.max(stockLevel, 0));
            pstmt.setString(6, normalizedExpiry);
            pstmt.setString(7, status);
            pstmt.setInt(8, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating medicine inventory item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteMedicine(int id) {
        String query = "DELETE FROM medicines WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting medicine inventory item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void updateStatusIfChanged(Connection conn, int id, String currentStatus, String computedStatus) {
        if (computedStatus == null || computedStatus.equals(currentStatus)) {
            return;
        }

        String query = "UPDATE medicines SET status = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, computedStatus);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error syncing medicine status for ID " + id + ": " + e.getMessage());
        }
    }

    private String determineStatus(int stockLevel, String expiryDate) {
        if (isExpired(expiryDate)) {
            return "expired";
        }

        if (stockLevel <= 0) {
            return "out_of_stock";
        }

        if (stockLevel <= 20) {
            return "low_stock";
        }

        return "in_stock";
    }

    private boolean isExpired(String expiryDate) {
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            return false;
        }

        String value = expiryDate.trim();
        try {
            if (value.matches("^\\d{4}-\\d{2}$")) {
                YearMonth expiryMonth = YearMonth.parse(value);
                YearMonth now = YearMonth.now();
                return now.isAfter(expiryMonth);
            }

            if (value.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                LocalDate expiry = LocalDate.parse(value);
                return expiry.isBefore(LocalDate.now());
            }
        } catch (DateTimeParseException ignored) {
            return false;
        }

        return false;
    }

    private String normalizeExpiry(String expiryDate) {
        if (expiryDate == null) {
            return null;
        }

        String value = expiryDate.trim();
        if (value.isEmpty()) {
            return null;
        }

        if (value.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return value.substring(0, 7);
        }

        return value;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String defaultIfBlank(String value, String fallback) {
        String trimmed = safeTrim(value);
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    private void ensureBatchNumberColumnExists() {
        String checkQuery = "PRAGMA table_info(medicines)";
        String alterQuery = "ALTER TABLE medicines ADD COLUMN batch_number TEXT";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(checkQuery);
                ResultSet rs = pstmt.executeQuery()) {

            boolean hasBatchColumn = false;
            while (rs.next()) {
                if ("batch_number".equalsIgnoreCase(rs.getString("name"))) {
                    hasBatchColumn = true;
                    break;
                }
            }

            if (!hasBatchColumn) {
                try (PreparedStatement alterStmt = conn.prepareStatement(alterQuery)) {
                    alterStmt.execute();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error ensuring medicines.batch_number column exists: " + e.getMessage());
        }
    }
}
