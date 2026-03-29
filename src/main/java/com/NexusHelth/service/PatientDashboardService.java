package com.NexusHelth.service;

import com.NexusHelth.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PatientDashboardService {

    public DashboardData getDashboardData(int patientId) {
        DashboardData data = new DashboardData();

        try (Connection conn = DatabaseConnection.getConnection()) {

            // 1. Upcoming Clinics Count
            String visitsQuery = "SELECT COUNT(*) FROM appointments WHERE patient_id = ? AND status IN ('scheduled', 'confirmed') AND appointment_date >= DATE('now')";
            try (PreparedStatement pstmt = conn.prepareStatement(visitsQuery)) {
                pstmt.setInt(1, patientId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        data.setUpcomingVisits(rs.getInt(1));
                    }
                }
            }

            // 2. Active Prescriptions Count
            String rxQuery = "SELECT COUNT(*) FROM prescriptions WHERE patient_id = ? AND status = 'pending'";
            try (PreparedStatement pstmt = conn.prepareStatement(rxQuery)) {
                pstmt.setInt(1, patientId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        data.setActivePrescriptions(rs.getInt(1));
                    }
                }
            }

            // 3. Outstanding Bills Sum
            String billsQuery = "SELECT SUM(total_amount) FROM invoices WHERE patient_id = ? AND status = 'unpaid'";
            try (PreparedStatement pstmt = conn.prepareStatement(billsQuery)) {
                pstmt.setInt(1, patientId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        double sum = rs.getDouble(1);
                        data.setOutstandingBillsCounter(rs.wasNull() ? 0.0 : sum);
                    }
                }
            }

            // 4. Upcoming Clinic Visits List
            String visitListQuery = "SELECT a.appointment_date, a.appointment_time, u.full_name as doctor_name, d.specialization "
                    +
                    "FROM appointments a " +
                    "JOIN doctors d ON a.doctor_id = d.id " +
                    "JOIN users u ON d.user_id = u.id " +
                    "WHERE a.patient_id = ? AND a.status IN ('scheduled', 'confirmed') AND a.appointment_date >= DATE('now') "
                    +
                    "ORDER BY a.appointment_date, a.appointment_time LIMIT 5";

            List<VisitDTO> visits = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(visitListQuery)) {
                pstmt.setInt(1, patientId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        VisitDTO visit = new VisitDTO();
                        visit.setDate(rs.getString("appointment_date"));
                        visit.setTime(rs.getString("appointment_time"));
                        visit.setDoctorName(rs.getString("doctor_name"));
                        visit.setSpecialty(rs.getString("specialization"));
                        visits.add(visit);
                    }
                }
            }
            data.setUpcomingVisitsList(visits);

        } catch (SQLException e) {
            System.err.println("Error fetching patient dashboard data: " + e.getMessage());
            e.printStackTrace();
        }

        return data;
    }

    // --- Inner DTOs ---

    public static class DashboardData {
        private int upcomingVisits = 0;
        private int activePrescriptions = 0;
        private double outstandingBillsCounter = 0.0;
        private List<VisitDTO> upcomingVisitsList = new ArrayList<>();

        public int getUpcomingVisits() {
            return upcomingVisits;
        }

        public void setUpcomingVisits(int upcomingVisits) {
            this.upcomingVisits = upcomingVisits;
        }

        public int getActivePrescriptions() {
            return activePrescriptions;
        }

        public void setActivePrescriptions(int activePrescriptions) {
            this.activePrescriptions = activePrescriptions;
        }

        public double getOutstandingBillsCounter() {
            return outstandingBillsCounter;
        }

        public void setOutstandingBillsCounter(double outstandingBillsCounter) {
            this.outstandingBillsCounter = outstandingBillsCounter;
        }

        public List<VisitDTO> getUpcomingVisitsList() {
            return upcomingVisitsList;
        }

        public void setUpcomingVisitsList(List<VisitDTO> upcomingVisitsList) {
            this.upcomingVisitsList = upcomingVisitsList;
        }
    }

    public static class VisitDTO {
        private String date;
        private String time;
        private String doctorName;
        private String specialty;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getDoctorName() {
            return doctorName;
        }

        public void setDoctorName(String doctorName) {
            this.doctorName = doctorName;
        }

        public String getSpecialty() {
            return specialty;
        }

        public void setSpecialty(String specialty) {
            this.specialty = specialty;
        }
    }
}
