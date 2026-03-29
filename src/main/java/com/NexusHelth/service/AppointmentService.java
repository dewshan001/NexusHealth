package com.NexusHelth.service;

import com.NexusHelth.model.Doctor;
import com.NexusHelth.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.sql.Statement;

public class AppointmentService {

    // Static appointment fee - can be set from config
    private static double appointmentFee = 500.0;

    // Getter and Setter for appointment fee
    public static double getAppointmentFee() {
        return appointmentFee;
    }

    public static void setAppointmentFee(double fee) {
        appointmentFee = fee;
    }

    public List<Doctor> getAvailableDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        String query = "SELECT d.id, d.user_id, u.full_name, d.specialization, u.profile_picture " +
                "FROM doctors d " +
                "JOIN users u ON d.user_id = u.id " +
                "WHERE u.status = 'active'";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                doctors.add(new Doctor(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("full_name"),
                        rs.getString("specialization"),
                        rs.getString("profile_picture")));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available doctors: " + e.getMessage());
            e.printStackTrace();
        }
        return doctors;
    }

    public List<String> getBookedTimeSlots(int doctorId, String date) {
        List<String> bookedSlots = new ArrayList<>();
        String query = "SELECT appointment_time FROM appointments " +
                "WHERE doctor_id = ? AND appointment_date = ? AND status IN ('scheduled', 'confirmed')";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, doctorId);
            pstmt.setString(2, date);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookedSlots.add(rs.getString("appointment_time"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching booked time slots: " + e.getMessage());
            e.printStackTrace();
        }
        return bookedSlots;
    }

    public boolean isTimeSlotAvailable(int doctorId, String date, String time) {
        String query = "SELECT COUNT(*) FROM appointments " +
                "WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? AND status IN ('scheduled', 'confirmed')";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, doctorId);
            pstmt.setString(2, date);
            pstmt.setString(3, time);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking time slot availability: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public int bookAppointment(int patientId, int doctorId, String date, String time) {
        // Double check availability to prevent concurrent booking issues
        if (!isTimeSlotAvailable(doctorId, date, time)) {
            return -1;
        }

        String insertQuery = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status) " +
                "VALUES (?, ?, ?, ?, 'scheduled')";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement insertPstmt = conn.prepareStatement(insertQuery)) {

            insertPstmt.setInt(1, patientId);
            insertPstmt.setInt(2, doctorId);
            insertPstmt.setString(3, date);
            insertPstmt.setString(4, time);

            int affectedRows = insertPstmt.executeUpdate();
            if (affectedRows > 0) {
                // SQLite doesn't support getGeneratedKeys(), so query the last inserted rowid
                String idQuery = "SELECT last_insert_rowid() as id";
                try (PreparedStatement idPstmt = conn.prepareStatement(idQuery);
                     ResultSet rs = idPstmt.executeQuery()) {
                    if (rs.next()) {
                        int appointmentId = rs.getInt("id");
                        System.out.println("✅ Appointment booked successfully with ID: " + appointmentId);
                        return appointmentId;
                    }
                }
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("Error booking appointment: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public Doctor getDoctorByUserId(int userId) {
        String query = "SELECT d.id, d.user_id, u.full_name, d.specialization, u.profile_picture " +
                "FROM doctors d JOIN users u ON d.user_id = u.id " +
                "WHERE d.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Doctor(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("specialization"),
                            rs.getString("profile_picture"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor by user id: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Map<String, Object>> getDoctorAppointments(int doctorId, String date) {
        List<Map<String, Object>> appointments = new ArrayList<>();
        String query = "SELECT a.id, a.appointment_time, a.status, u.full_name AS patient_name, p.patient_code " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN users u ON p.user_id = u.id " +
                "WHERE a.doctor_id = ? AND a.appointment_date = ? " +
                "ORDER BY a.appointment_time";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, doctorId);
            pstmt.setString(2, date);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> appt = new HashMap<>();
                    appt.put("id", rs.getInt("id"));
                    appt.put("time", rs.getString("appointment_time"));
                    appt.put("status", rs.getString("status"));
                    appt.put("patientName", rs.getString("patient_name"));
                    appt.put("patientCode", rs.getString("patient_code"));
                    appointments.add(appt);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor appointments: " + e.getMessage());
            e.printStackTrace();
        }
        return appointments;
    }

    /**
     * Get all appointments for a given date (for receptionist view)
     */
    public List<Map<String, Object>> getAppointmentsByDate(String date) {
        List<Map<String, Object>> appointments = new ArrayList<>();
        String query = "SELECT a.id, a.appointment_time, a.status, a.doctor_id, u.full_name AS patient_name, " +
                "p.patient_code, d.full_name AS doctor_name, doc.specialization " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN users u ON p.user_id = u.id " +
                "JOIN doctors doc ON a.doctor_id = doc.id " +
                "JOIN users d ON doc.user_id = d.id " +
                "WHERE a.appointment_date = ? " +
                "ORDER BY a.appointment_time";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, date);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> appt = new HashMap<>();
                    appt.put("id", rs.getInt("id"));
                    appt.put("doctorId", rs.getInt("doctor_id"));
                    appt.put("time", rs.getString("appointment_time"));
                    appt.put("status", rs.getString("status"));
                    appt.put("patientName", rs.getString("patient_name"));
                    appt.put("patientCode", rs.getString("patient_code"));
                    appt.put("doctorName", rs.getString("doctor_name"));
                    appt.put("specialization", rs.getString("specialization"));
                    appointments.add(appt);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching appointments by date: " + e.getMessage());
            e.printStackTrace();
        }
        return appointments;
    }

    /**
     * Reschedule an appointment to a new date and time
     */
    public boolean rescheduleAppointment(int appointmentId, String newDate, String newTime) {
        System.out.println("\n📅 APPOINTMENT SERVICE: Rescheduling appointment ID: " + appointmentId);

        // First, get the doctor ID from the appointment
        String getAppointmentQuery = "SELECT doctor_id FROM appointments WHERE id = ?";
        int doctorId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement getPstmt = conn.prepareStatement(getAppointmentQuery)) {
            getPstmt.setInt(1, appointmentId);

            try (ResultSet rs = getPstmt.executeQuery()) {
                if (rs.next()) {
                    doctorId = rs.getInt("doctor_id");
                }
            }

            if (doctorId == -1) {
                System.out.println("❌ Appointment not found");
                return false;
            }

            // Check if new time slot is available
            if (!isTimeSlotAvailable(doctorId, newDate, newTime)) {
                System.out.println("❌ Time slot is not available");
                return false;
            }

            // Update the appointment
            String updateQuery = "UPDATE appointments SET appointment_date = ?, appointment_time = ?, status = 'confirmed' WHERE id = ?";
            try (PreparedStatement updatePstmt = conn.prepareStatement(updateQuery)) {
                updatePstmt.setString(1, newDate);
                updatePstmt.setString(2, newTime);
                updatePstmt.setInt(3, appointmentId);

                int affectedRows = updatePstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("✅ Appointment rescheduled to " + newDate + " at " + newTime);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error rescheduling appointment: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cancel an appointment
     */
    public boolean cancelAppointment(int appointmentId) {
        System.out.println("\n❌ APPOINTMENT SERVICE: Cancelling appointment ID: " + appointmentId);

        String query = "UPDATE appointments SET status = 'cancelled' WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, appointmentId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✅ Appointment cancelled successfully");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error cancelling appointment: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check in a patient for their appointment
     */
    public boolean checkInPatient(int appointmentId) {
        System.out.println("\n✅ APPOINTMENT SERVICE: Checking in patient for appointment ID: " + appointmentId);

        String query = "UPDATE appointments SET status = 'confirmed' WHERE id = ? AND status IN ('scheduled', 'pending')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, appointmentId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✅ Patient checked in successfully");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error checking in patient: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ===================== VALIDATION METHODS =====================

    /**
     * Validate appointment exists and return its current status
     */
    public String getAppointmentStatus(int appointmentId) {
        String query = "SELECT status FROM appointments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, appointmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching appointment status: " + e.getMessage());
        }
        return null;
    }

    /**
     * Check if appointment can be rescheduled (not completed or cancelled)
     */
    public boolean canReschedule(int appointmentId) {
        String status = getAppointmentStatus(appointmentId);
        if (status == null) {
            return false;
        }
        return !status.equals("completed") && !status.equals("cancelled");
    }

    /**
     * Check if appointment can be cancelled (not already completed or cancelled)
     */
    public boolean canCancel(int appointmentId) {
        String status = getAppointmentStatus(appointmentId);
        if (status == null) {
            return false;
        }
        return !status.equals("completed") && !status.equals("cancelled");
    }

    /**
     * Check if doctor exists and is active
     */
    public boolean isDoctorValid(int doctorId) {
        String query = "SELECT COUNT(*) FROM doctors d JOIN users u ON d.user_id = u.id WHERE d.id = ? AND u.status = 'active'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error validating doctor: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check if patient exists and is active
     */
    public boolean isPatientValid(int patientId) {
        String query = "SELECT COUNT(*) FROM patients p JOIN users u ON p.user_id = u.id WHERE p.id = ? AND u.status = 'active'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error validating patient: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get appointment details by ID
     */
    public Map<String, Object> getAppointmentById(int appointmentId) {
        String query = "SELECT a.id, a.patient_id, a.doctor_id, a.appointment_date, a.appointment_time, " +
                "a.status, a.notes, u.full_name AS patient_name, p.patient_code, " +
                "d.full_name AS doctor_name, d.specialization " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN users u ON p.user_id = u.id " +
                "JOIN doctors doc ON a.doctor_id = doc.id " +
                "JOIN users d ON doc.user_id = d.id " +
                "WHERE a.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, appointmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> appt = new HashMap<>();
                    appt.put("id", rs.getInt("id"));
                    appt.put("patientId", rs.getInt("patient_id"));
                    appt.put("doctorId", rs.getInt("doctor_id"));
                    appt.put("appointmentDate", rs.getString("appointment_date"));
                    appt.put("appointmentTime", rs.getString("appointment_time"));
                    appt.put("status", rs.getString("status"));
                    appt.put("notes", rs.getString("notes"));
                    appt.put("patientName", rs.getString("patient_name"));
                    appt.put("patientCode", rs.getString("patient_code"));
                    appt.put("doctorName", rs.getString("doctor_name"));
                    appt.put("specialization", rs.getString("specialization"));
                    return appt;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching appointment details: " + e.getMessage());
        }
        return null;
    }

    /**
     * Create an invoice for an appointment after payment
     */
    public boolean createAppointmentInvoice(int appointmentId, int patientId, int doctorId, String patientName) {
        System.out.println("\n💳 APPOINTMENT SERVICE: Creating invoice for appointment ID: " + appointmentId);

        // Generate unique invoice number
        String invoiceNumber = "INV-APT-" + System.currentTimeMillis();

        String invoiceInsertSql = "INSERT INTO invoices (invoice_number, patient_id, doctor_id, patient_name, consultation_type, " +
                "consultation_amount, subtotal, total_amount, status, payment_method, paid_at) " +
                "VALUES (?, ?, ?, ?, 'Appointment', ?, ?, ?, 'paid', 'card', CURRENT_TIMESTAMP)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int invoiceId;
            try (PreparedStatement pstmt = conn.prepareStatement(invoiceInsertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, invoiceNumber);
                pstmt.setInt(2, patientId);
                pstmt.setInt(3, doctorId);
                pstmt.setString(4, patientName);
                pstmt.setDouble(5, appointmentFee);  // consultation_amount
                pstmt.setDouble(6, appointmentFee);  // subtotal
                pstmt.setDouble(7, appointmentFee);  // total_amount

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows <= 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (!keys.next()) {
                        conn.rollback();
                        return false;
                    }
                    invoiceId = keys.getInt(1);
                }
            }

            String specialization = null;
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT specialization FROM doctors WHERE id = ?")) {
                pstmt.setInt(1, doctorId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        specialization = rs.getString("specialization");
                    }
                }
            }

            String department = (specialization == null || specialization.trim().isEmpty()) ? "Appointments" : specialization;
            String transactionCode = "TXN-" + invoiceNumber;

            String txnInsertSql = "INSERT INTO transactions (invoice_id, transaction_code, type, department, amount, status, transacted_at) " +
                    "VALUES (?, ?, 'Appointment', ?, ?, 'settled', CURRENT_TIMESTAMP)";
            try (PreparedStatement pstmt = conn.prepareStatement(txnInsertSql)) {
                pstmt.setInt(1, invoiceId);
                pstmt.setString(2, transactionCode);
                pstmt.setString(3, department);
                pstmt.setDouble(4, appointmentFee);
                pstmt.executeUpdate();
            }

            conn.commit();
            System.out.println("✅ Invoice created successfully with number: " + invoiceNumber);
            System.out.println("✅ Transaction created successfully: " + transactionCode);
            return true;
        } catch (SQLException e) {
            System.err.println("Error creating appointment invoice: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back appointment invoice transaction: " + rollbackEx.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
        return false;
    }
}
