package com.NexusHelth.service;

import com.NexusHelth.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DoctorPrescriptionService {

    /**
     * Creates a prescription and its items in the database.
     *
     * @param doctorId    the doctors table id (NOT user id)
     * @param patientCode the patient_code from patients table
     * @param medications list of maps, each with keys: name, dose, freq
     * @return true on success
     */
    public boolean createPrescription(int doctorId, String patientCode, List<Map<String, String>> medications) {
        String findPatientSql = "SELECT id FROM patients WHERE patient_code = ?";
        String findConsultationSql = "SELECT c.id FROM consultations c " +
                "JOIN appointments a ON c.appointment_id = a.id " +
                "WHERE c.patient_id = ? AND c.doctor_id = ? " +
                "ORDER BY c.consulted_at DESC LIMIT 1";
        String findAppointmentSql = "SELECT id FROM appointments " +
                "WHERE patient_id = ? AND doctor_id = ? " +
                "ORDER BY created_at DESC LIMIT 1";
        String createConsultationSql = "INSERT INTO consultations (appointment_id, doctor_id, patient_id, diagnosis, notes) "
                +
                "VALUES (?, ?, ?, 'Prescription Issued', 'Auto-created for prescription')";
        String insertPrescriptionSql = "INSERT INTO prescriptions (consultation_id, doctor_id, patient_id, status) " +
                "VALUES (?, ?, ?, 'pending')";
        String findMedicineSql = "SELECT id FROM medicines WHERE LOWER(name) = LOWER(?)";
        String insertItemSql = "INSERT INTO prescription_items (prescription_id, medicine_id, dosage, frequency, instructions, quantity) "
                +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. Find patient_id
                int patientId = -1;
                try (PreparedStatement pstmt = conn.prepareStatement(findPatientSql)) {
                    pstmt.setString(1, patientCode);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            patientId = rs.getInt("id");
                        }
                    }
                }
                if (patientId == -1) {
                    conn.rollback();
                    return false; // Patient not found
                }

                // 2. Find or create consultation
                int consultationId = -1;
                try (PreparedStatement pstmt = conn.prepareStatement(findConsultationSql)) {
                    pstmt.setInt(1, patientId);
                    pstmt.setInt(2, doctorId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            consultationId = rs.getInt("id");
                        }
                    }
                }

                if (consultationId == -1) {
                    // Need to find an appointment first, then auto-create a consultation
                    int appointmentId = -1;
                    try (PreparedStatement pstmt = conn.prepareStatement(findAppointmentSql)) {
                        pstmt.setInt(1, patientId);
                        pstmt.setInt(2, doctorId);
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                appointmentId = rs.getInt("id");
                            }
                        }
                    }

                    if (appointmentId == -1) {
                        conn.rollback();
                        return false; // No appointment found for this patient/doctor
                    }

                    try (PreparedStatement pstmt = conn.prepareStatement(createConsultationSql)) {
                        pstmt.setInt(1, appointmentId);
                        pstmt.setInt(2, doctorId);
                        pstmt.setInt(3, patientId);
                        pstmt.executeUpdate();
                    }
                    try (java.sql.Statement s = conn.createStatement();
                            ResultSet keys = s.executeQuery("SELECT last_insert_rowid()")) {
                        if (keys.next()) {
                            consultationId = keys.getInt(1);
                        }
                    }
                }

                if (consultationId == -1) {
                    conn.rollback();
                    return false;
                }

                // 3. Insert prescription
                int prescriptionId = -1;
                try (PreparedStatement pstmt = conn.prepareStatement(insertPrescriptionSql)) {
                    pstmt.setInt(1, consultationId);
                    pstmt.setInt(2, doctorId);
                    pstmt.setInt(3, patientId);
                    pstmt.executeUpdate();
                }
                try (java.sql.Statement s = conn.createStatement();
                        ResultSet keys = s.executeQuery("SELECT last_insert_rowid()")) {
                    if (keys.next()) {
                        prescriptionId = keys.getInt(1);
                    }
                }

                if (prescriptionId == -1) {
                    conn.rollback();
                    return false;
                }

                // 4. Insert each medication item
                for (Map<String, String> med : medications) {
                    String medName = med.get("name");
                    String dose = med.getOrDefault("dose", "");
                    String freq = med.getOrDefault("freq", "");

                    // Look up medicine_id by name
                    int medicineId = -1;
                    try (PreparedStatement pstmt = conn.prepareStatement(findMedicineSql)) {
                        pstmt.setString(1, medName);
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                medicineId = rs.getInt("id");
                            }
                        }
                    }

                    if (medicineId == -1) {
                        // Medicine not in inventory — create a placeholder entry
                        String insertMedSql = "INSERT INTO medicines (name, stock_level, unit_price) VALUES (?, 0, 0.0)";
                        try (PreparedStatement pstmt = conn.prepareStatement(insertMedSql)) {
                            pstmt.setString(1, medName);
                            pstmt.executeUpdate();
                        }
                        try (java.sql.Statement s = conn.createStatement();
                                ResultSet keys = s.executeQuery("SELECT last_insert_rowid()")) {
                            if (keys.next()) {
                                medicineId = keys.getInt(1);
                            }
                        }
                    }

                    // Parse quantity from dose field, default to 1
                    int quantity = 1;
                    try {
                        quantity = Integer.parseInt(dose.trim());
                    } catch (NumberFormatException ignored) {
                        // If dose isn't a pure number, store it as dosage text
                    }

                    try (PreparedStatement pstmt = conn.prepareStatement(insertItemSql)) {
                        pstmt.setInt(1, prescriptionId);
                        pstmt.setInt(2, medicineId);
                        pstmt.setString(3, dose); // dosage
                        pstmt.setString(4, freq); // frequency
                        pstmt.setString(5, freq); // instructions (same as frequency for now)
                        pstmt.setInt(6, quantity); // quantity
                        pstmt.executeUpdate();
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
