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

public class AppointmentService {

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

    public boolean bookAppointment(int patientId, int doctorId, String date, String time) {
        // Double check availability to prevent concurrent booking issues
        if (!isTimeSlotAvailable(doctorId, date, time)) {
            return false;
        }

        String query = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status) " +
                "VALUES (?, ?, ?, ?, 'scheduled')";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, patientId);
            pstmt.setInt(2, doctorId);
            pstmt.setString(3, date);
            pstmt.setString(4, time);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error booking appointment: " + e.getMessage());
            e.printStackTrace();
            return false;
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
}
