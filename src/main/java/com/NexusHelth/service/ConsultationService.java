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
            "JOIN appointments a ON c.appointment_id = a.id " +
            "JOIN patients p ON c.patient_id = p.id " +
            "JOIN doctors d ON c.doctor_id = d.id " +
            "JOIN users u ON d.user_id = u.id " +
            "WHERE p.patient_code = ? AND LOWER(a.status) = 'completed' " +
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
        String appointmentValidateQuery = "SELECT doctor_id, patient_id, status FROM appointments WHERE id = ?";
        String findConsultationQuery = "SELECT id FROM consultations WHERE appointment_id = ?";
        String insertQuery = "INSERT INTO consultations (appointment_id, doctor_id, patient_id, diagnosis, notes) VALUES (?, ?, ?, ?, ?)";
        String updateQuery = "UPDATE consultations SET diagnosis = ?, notes = ?, consulted_at = CURRENT_TIMESTAMP WHERE appointment_id = ? AND doctor_id = ? AND patient_id = ?";

        int patientId = -1;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Get patient ID from patientCode
                try (PreparedStatement pstmt = conn.prepareStatement(patientQuery)) {
                    pstmt.setString(1, patientCode);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            patientId = rs.getInt("id");
                        } else {
                            conn.rollback();
                            return false;
                        }
                    }
                }

                // Validate appointment eligibility: must be confirmed and match doctor/patient
                int apptDoctorId = -1;
                int apptPatientId = -1;
                String apptStatus = null;
                try (PreparedStatement pstmt = conn.prepareStatement(appointmentValidateQuery)) {
                    pstmt.setInt(1, appointmentId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        apptDoctorId = rs.getInt("doctor_id");
                        apptPatientId = rs.getInt("patient_id");
                        apptStatus = rs.getString("status");
                    }
                }

                if (apptDoctorId != doctorId || apptPatientId != patientId || apptStatus == null
                        || !"confirmed".equalsIgnoreCase(apptStatus)) {
                    conn.rollback();
                    return false;
                }

                // Insert or update consultation notes (appointment is NOT completed here)
                boolean consultationExists = false;
                try (PreparedStatement pstmt = conn.prepareStatement(findConsultationQuery)) {
                    pstmt.setInt(1, appointmentId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        consultationExists = rs.next();
                    }
                }

                if (consultationExists) {
                    try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                        pstmt.setString(1, diagnosis);
                        pstmt.setString(2, notes);
                        pstmt.setInt(3, appointmentId);
                        pstmt.setInt(4, doctorId);
                        pstmt.setInt(5, patientId);
                        int rows = pstmt.executeUpdate();
                        if (rows <= 0) {
                            conn.rollback();
                            return false;
                        }
                    }
                } else {
                    try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                        pstmt.setInt(1, appointmentId);
                        pstmt.setInt(2, doctorId);
                        pstmt.setInt(3, patientId);
                        pstmt.setString(4, diagnosis);
                        pstmt.setString(5, notes);
                        pstmt.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                    // Best effort rollback
                }
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // Best effort restore
                }
            }

        } catch (SQLException e) {
            System.err.println("Error saving consultation: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean completeConsultation(int appointmentId, int doctorId, String patientCode) {
        String patientQuery = "SELECT id FROM patients WHERE patient_code = ?";
        String appointmentValidateQuery = "SELECT doctor_id, patient_id, status FROM appointments WHERE id = ?";
        String consultationExistsQuery = "SELECT id FROM consultations WHERE appointment_id = ? AND doctor_id = ? AND patient_id = ?";
        String updateApptQuery = "UPDATE appointments SET status = 'completed' WHERE id = ? AND status = 'confirmed'";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int patientId = -1;
                try (PreparedStatement pstmt = conn.prepareStatement(patientQuery)) {
                    pstmt.setString(1, patientCode);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            patientId = rs.getInt("id");
                        } else {
                            conn.rollback();
                            return false;
                        }
                    }
                }

                int apptDoctorId;
                int apptPatientId;
                String apptStatus;
                try (PreparedStatement pstmt = conn.prepareStatement(appointmentValidateQuery)) {
                    pstmt.setInt(1, appointmentId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        apptDoctorId = rs.getInt("doctor_id");
                        apptPatientId = rs.getInt("patient_id");
                        apptStatus = rs.getString("status");
                    }
                }

                if (apptDoctorId != doctorId || apptPatientId != patientId || apptStatus == null
                        || !"confirmed".equalsIgnoreCase(apptStatus)) {
                    conn.rollback();
                    return false;
                }

                boolean hasConsultation;
                try (PreparedStatement pstmt = conn.prepareStatement(consultationExistsQuery)) {
                    pstmt.setInt(1, appointmentId);
                    pstmt.setInt(2, doctorId);
                    pstmt.setInt(3, patientId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        hasConsultation = rs.next();
                    }
                }

                if (!hasConsultation) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement pstmt = conn.prepareStatement(updateApptQuery)) {
                    pstmt.setInt(1, appointmentId);
                    int rows = pstmt.executeUpdate();
                    if (rows <= 0) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                    // Best effort rollback
                }
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // Best effort restore
                }
            }
        } catch (SQLException e) {
            System.err.println("Error completing consultation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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

    public List<Map<String, Object>> getDoctorConsultationHistory(int doctorId) {
        List<Map<String, Object>> history = new ArrayList<>();
        String query = "SELECT c.id, u.full_name AS patient_name, p.patient_code, c.diagnosis, c.notes, c.consulted_at " +
            "FROM consultations c " +
            "JOIN appointments a ON c.appointment_id = a.id " +
            "JOIN patients p ON c.patient_id = p.id " +
            "JOIN users u ON p.user_id = u.id " +
            "WHERE c.doctor_id = ? AND LOWER(a.status) = 'completed' " +
            "ORDER BY c.consulted_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, doctorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("id", rs.getInt("id"));
                    record.put("patientName", rs.getString("patient_name"));
                    record.put("patientCode", rs.getString("patient_code"));
                    record.put("diagnosis", rs.getString("diagnosis"));
                    record.put("notes", rs.getString("notes"));
                    record.put("date", rs.getString("consulted_at"));
                    history.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor consultation history: " + e.getMessage());
            e.printStackTrace();
        }
        return history;
    }
}
