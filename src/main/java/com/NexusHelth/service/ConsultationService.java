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

public class ConsultationService {

    public List<Map<String, Object>> getPatientHistory(String patientCode) {
        List<Map<String, Object>> history = new ArrayList<>();
        String query = "SELECT c.diagnosis, c.notes, c.consulted_at, u.full_name AS doctor_name " +
                "FROM consultations c " +
                "JOIN patients p ON c.patient_id = p.id " +
                "JOIN doctors d ON c.doctor_id = d.id " +
                "JOIN users u ON d.user_id = u.id " +
                "WHERE p.patient_code = ? " +
                "ORDER BY c.consulted_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, patientCode);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("diagnosis", rs.getString("diagnosis"));
                    record.put("notes", rs.getString("notes"));
                    record.put("date", rs.getString("consulted_at"));
                    record.put("doctorName", rs.getString("doctor_name"));
                    history.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient history: " + e.getMessage());
            e.printStackTrace();
        }
        return history;
    }

    public boolean saveConsultation(int appointmentId, int doctorId, String patientCode, String diagnosis,
            String notes) {
        String patientQuery = "SELECT id FROM patients WHERE patient_code = ?";
        String insertQuery = "INSERT INTO consultations (appointment_id, doctor_id, patient_id, diagnosis, notes) VALUES (?, ?, ?, ?, ?)";
        String updateApptQuery = "UPDATE appointments SET status = 'completed' WHERE id = ?";

        int patientId = -1;

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get patient ID
            try (PreparedStatement pstmt = conn.prepareStatement(patientQuery)) {
                pstmt.setString(1, patientCode);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        patientId = rs.getInt("id");
                    } else {
                        return false;
                    }
                }
            }

            // Insert consultation
            try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                pstmt.setInt(1, appointmentId);
                pstmt.setInt(2, doctorId);
                pstmt.setInt(3, patientId);
                pstmt.setString(4, diagnosis);
                pstmt.setString(5, notes);
                pstmt.executeUpdate();
            }

            // Update appointment status to completed
            try (PreparedStatement pstmt = conn.prepareStatement(updateApptQuery)) {
                pstmt.setInt(1, appointmentId);
                pstmt.executeUpdate();
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Error saving consultation: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public Map<String, Object> getPatientVitals(String patientCode) {
        Map<String, Object> vitals = new HashMap<>();
        String query = "SELECT blood_type, height, weight, heart_rate FROM patients WHERE patient_code = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, patientCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    vitals.put("bloodType", rs.getString("blood_type") != null ? rs.getString("blood_type") : "");
                    vitals.put("height", rs.getString("height") != null ? rs.getString("height") : "");
                    vitals.put("weight", rs.getString("weight") != null ? rs.getString("weight") : "");
                    vitals.put("heartRate", rs.getString("heart_rate") != null ? rs.getString("heart_rate") : "");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient vitals: " + e.getMessage());
            e.printStackTrace();
        }
        return vitals;
    }

    public boolean updatePatientVitals(String patientCode, String bloodType, String height, String weight,
            String heartRate) {
        String query = "UPDATE patients SET blood_type = ?, height = ?, weight = ?, heart_rate = ? WHERE patient_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, bloodType);
            pstmt.setString(2, height);
            pstmt.setString(3, weight);
            pstmt.setString(4, heartRate);
            pstmt.setString(5, patientCode);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating patient vitals: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
