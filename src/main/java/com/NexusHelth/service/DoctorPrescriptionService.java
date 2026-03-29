package com.NexusHelth.service;

import com.NexusHelth.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DoctorPrescriptionService {

    public static class PrescriptionResult {
        public final boolean success;
        public final String message;

        public PrescriptionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    /**
     * Creates a prescription and its items in the database.
     *
     * @param doctorId    the doctors table id (NOT user id)
         * @param appointmentId the appointment id for the active consultation
     * @param patientCode the patient_code from patients table
     * @param medications list of maps, each with keys: name, dose, freq
         * @return result with success flag and message
     */
        public PrescriptionResult createPrescription(int doctorId, int appointmentId, String patientCode,
            List<Map<String, String>> medications) {
        String findPatientSql = "SELECT id FROM patients WHERE patient_code = ?";
        String findAppointmentSql = "SELECT doctor_id, patient_id, status FROM appointments WHERE id = ?";
        String findConsultationSql = "SELECT id FROM consultations WHERE appointment_id = ? AND doctor_id = ? AND patient_id = ?";
        String insertPrescriptionSql = "INSERT INTO prescriptions (consultation_id, doctor_id, patient_id, status) " +
                "VALUES (?, ?, ?, 'pending')";
        String findMedicineSql = "SELECT id FROM medicines WHERE LOWER(TRIM(name)) = LOWER(TRIM(?))";
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
                    return new PrescriptionResult(false, "Patient not found");
                }

                // 2. Validate appointment (must be checked-in/confirmed and belong to this doctor/patient)
                int apptDoctorId = -1;
                int apptPatientId = -1;
                String apptStatus = null;
                try (PreparedStatement pstmt = conn.prepareStatement(findAppointmentSql)) {
                    pstmt.setInt(1, appointmentId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            apptDoctorId = rs.getInt("doctor_id");
                            apptPatientId = rs.getInt("patient_id");
                            apptStatus = rs.getString("status");
                        }
                    }
                }

                if (apptDoctorId != doctorId || apptPatientId != patientId) {
                    conn.rollback();
                    return new PrescriptionResult(false, "Appointment does not match this doctor/patient");
                }

                if (apptStatus == null || !"confirmed".equalsIgnoreCase(apptStatus)) {
                    conn.rollback();
                    return new PrescriptionResult(false,
                            "Prescription not allowed for appointment status: " + (apptStatus == null ? "unknown" : apptStatus));
                }

                // 3. Require an existing consultation (notes must be submitted first)
                int consultationId = -1;
                try (PreparedStatement pstmt = conn.prepareStatement(findConsultationSql)) {
                    pstmt.setInt(1, appointmentId);
                    pstmt.setInt(2, doctorId);
                    pstmt.setInt(3, patientId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            consultationId = rs.getInt("id");
                        }
                    }
                }

                if (consultationId == -1) {
                    conn.rollback();
                    return new PrescriptionResult(false, "Please submit consultation notes before issuing a prescription");
                }

                // 4. Insert prescription
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
                    return new PrescriptionResult(false, "Failed to create prescription");
                }

                // 5. Insert each medication item
                for (Map<String, String> med : medications) {
                    String medName = med.get("name");
                    medName = medName == null ? null : medName.trim();
                    String dose = med.getOrDefault("dose", "");
                    String freq = med.getOrDefault("freq", "");

                    int requestedMedicineId = -1;
                    String medicineIdStr = med.get("medicineId");
                    if (medicineIdStr == null || medicineIdStr.trim().isEmpty()) {
                        medicineIdStr = med.get("id");
                    }
                    if (medicineIdStr != null && !medicineIdStr.trim().isEmpty()) {
                        try {
                            requestedMedicineId = Integer.parseInt(medicineIdStr.trim());
                        } catch (NumberFormatException ignored) {
                            requestedMedicineId = -1;
                        }
                    }

                    if ((medName == null || medName.isEmpty()) && requestedMedicineId <= 0) {
                        conn.rollback();
                        return new PrescriptionResult(false, "Medication name is required");
                    }

                    // Look up medicine_id by name
                    int medicineId = -1;
                    if (requestedMedicineId > 0) {
                        // Verify medicine exists
                        String verifySql = "SELECT id FROM medicines WHERE id = ?";
                        try (PreparedStatement pstmt = conn.prepareStatement(verifySql)) {
                            pstmt.setInt(1, requestedMedicineId);
                            try (ResultSet rs = pstmt.executeQuery()) {
                                if (rs.next()) {
                                    medicineId = rs.getInt("id");
                                }
                            }
                        }
                    }

                    if (medicineId == -1) {
                        try (PreparedStatement pstmt = conn.prepareStatement(findMedicineSql)) {
                            pstmt.setString(1, medName);
                            try (ResultSet rs = pstmt.executeQuery()) {
                                if (rs.next()) {
                                    medicineId = rs.getInt("id");
                                }
                            }
                        }
                    }

                    if (medicineId == -1) {
                        // Medicine not in inventory — create a placeholder entry
                        String insertMedSql = "INSERT INTO medicines (name, stock_level, unit_price) VALUES (?, 0, 0.0)";
                        try (PreparedStatement pstmt = conn.prepareStatement(insertMedSql)) {
                            pstmt.setString(1, medName == null ? "" : medName.trim());
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
                return new PrescriptionResult(true, "Prescription saved successfully");

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return new PrescriptionResult(false, "Failed to save prescription");
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new PrescriptionResult(false, "Failed to save prescription");
        }
    }
}
