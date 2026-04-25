package com.NexusHelth.service;

import com.NexusHelth.model.User;
import com.NexusHelth.model.Patient;
import com.NexusHelth.util.DatabaseConnection;
import com.NexusHelth.util.IdGeneratorUtil;
import com.NexusHelth.util.ValidationUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PatientService {

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Check if email already exists
    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Register a new user and patient
    public boolean registerPatient(String fullName, String email, String password,
            String phone, String dateOfBirth, String gender,
            String bloodType, String address) {
        String userQuery = "INSERT INTO users (full_name, email, password_hash, role, status) VALUES (?, ?, ?, 'patient', 'active')";
        String patientQuery = "INSERT INTO patients (user_id, patient_code, phone, date_of_birth, gender, blood_type, address, account_status) VALUES (?, ?, ?, ?, ?, ?, ?, 'active')";
        String lastIdQuery = "SELECT last_insert_rowid() as id";

        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("\n📝 REGISTER SERVICE: Attempting to register: " + email);

            // Hash password using BCrypt
            String hashedPassword = passwordEncoder.encode(password);
            System.out.println("✅ Password hashed successfully");

            // Generate unique patient code
            String patientCode = IdGeneratorUtil.generatePatientCode();
            System.out.println("✅ Patient code generated: " + patientCode);

            // Insert user
            try (PreparedStatement pstmt = conn.prepareStatement(userQuery)) {
                pstmt.setString(1, fullName);
                pstmt.setString(2, email);
                pstmt.setString(3, hashedPassword);
                pstmt.executeUpdate();
                System.out.println("✅ User inserted into database");

                // SQLite JDBC doesn't support RETURN_GENERATED_KEYS, so we use
                // last_insert_rowid()
                try (PreparedStatement idStmt = conn.prepareStatement(lastIdQuery);
                        ResultSet rs = idStmt.executeQuery()) {
                    if (rs.next()) {
                        int userId = rs.getInt("id");
                        System.out.println("✅ User ID: " + userId);

                        // Insert patient record
                        try (PreparedStatement pstmt2 = conn.prepareStatement(patientQuery)) {
                            String normalizedGender = ValidationUtil.normalizeGender(gender);
                            pstmt2.setInt(1, userId);
                            pstmt2.setString(2, patientCode);
                            pstmt2.setString(3, phone);
                            pstmt2.setString(4, dateOfBirth);
                            pstmt2.setString(5, normalizedGender);
                            pstmt2.setString(6, bloodType);
                            pstmt2.setString(7, address);
                            pstmt2.executeUpdate();
                            System.out.println("✅ Patient record inserted successfully");
                            System.out.println("✅ Registration complete for: " + email + "\n");
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Database error during registration: " + e.getMessage());
            if (e.getMessage().contains("UNIQUE")) {
                System.out.println("❌ Email already exists in the system");
            }
            e.printStackTrace();
        }
        System.out.println("❌ Registration failed for: " + email + "\n");
        return false;
    }

    // Login user - compare plain password with BCrypt hash
    public User loginPatient(String email, String password) {
        String query = "SELECT id, full_name, email, password_hash, role, status FROM users WHERE email = ? AND role = 'patient'";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            System.out.println("\n📝 LOGIN SERVICE: Attempting to login user: " + email);

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("✅ User found in database: " + email);
                String storedHash = rs.getString("password_hash");
                String status = rs.getString("status");

                System.out.println("✅ User status: " + status);
                System.out.println("🔐 Verifying password...");

                // Use BCrypt to verify the password
                if (passwordEncoder.matches(password, storedHash)) {
                    System.out.println("✅ Password verified successfully");
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    user.setStatus(rs.getString("status"));
                    System.out.println("✅ User object created successfully\n");
                    return user;
                } else {
                    System.out.println("❌ Password verification failed - wrong password\n");
                }
            } else {
                System.out
                        .println("❌ User not found in database with email: " + email + " (or user is not a patient)\n");
            }
        } catch (SQLException e) {
            System.out.println("❌ Database error during login: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Get patient details by user ID
    public Patient getPatientByUserId(int userId) {
        String query = "SELECT p.id, p.user_id, p.patient_code, p.phone, p.date_of_birth, p.gender, " +
                "p.blood_type, p.address, p.account_status, u.full_name, u.email, u.profile_picture " +
                "FROM patients p " +
                "JOIN users u ON p.user_id = u.id " +
                "WHERE p.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Patient patient = new Patient();
                patient.setId(rs.getInt("id"));
                patient.setUserId(rs.getInt("user_id"));
                patient.setPatientCode(rs.getString("patient_code"));
                patient.setPhone(rs.getString("phone"));
                patient.setDateOfBirth(rs.getString("date_of_birth"));
                patient.setGender(rs.getString("gender"));
                patient.setBloodType(rs.getString("blood_type"));
                patient.setAddress(rs.getString("address"));
                patient.setAccountStatus(rs.getString("account_status"));
                patient.setFullName(rs.getString("full_name"));
                patient.setEmail(rs.getString("email"));
                patient.setProfilePicture(rs.getString("profile_picture"));
                return patient;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Update patient profile
    public boolean updatePatientProfile(int userId, String phone, String dateOfBirth,
            String gender, String bloodType, String address) {
        String query = "UPDATE patients SET phone = ?, date_of_birth = ?, gender = ?, blood_type = ?, address = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            String normalizedGender = ValidationUtil.normalizeGender(gender);
            pstmt.setString(1, phone);
            pstmt.setString(2, dateOfBirth);
            pstmt.setString(3, normalizedGender);
            pstmt.setString(4, bloodType);
            pstmt.setString(5, address);
            pstmt.setInt(6, userId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check whether an email is used by a different user.
     */
    public boolean emailExistsForOtherUser(String email, int currentUserId) {
        String query = "SELECT 1 FROM users WHERE email = ? AND id <> ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            pstmt.setInt(2, currentUserId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Receptionist-safe update of patient details.
     * Updates both users (full name + email) and patients (profile fields) atomically.
     */
    public boolean updatePatientDetailsByReceptionist(
            int userId,
            String fullName,
            String email,
            String phone,
            String dateOfBirth,
            String gender,
            String bloodType,
            String address) {

        String updateUserQuery = "UPDATE users SET full_name = ?, email = ? WHERE id = ? AND role = 'patient'";
        String updatePatientQuery = "UPDATE patients SET phone = ?, date_of_birth = ?, gender = ?, blood_type = ?, address = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            int userRows;
            try (PreparedStatement userStmt = conn.prepareStatement(updateUserQuery)) {
                userStmt.setString(1, fullName);
                userStmt.setString(2, email);
                userStmt.setInt(3, userId);
                userRows = userStmt.executeUpdate();
            }

            int patientRows;
            try (PreparedStatement patientStmt = conn.prepareStatement(updatePatientQuery)) {
                String normalizedGender = ValidationUtil.normalizeGender(gender);
                patientStmt.setString(1, phone);
                patientStmt.setString(2, dateOfBirth);
                patientStmt.setString(3, normalizedGender);
                patientStmt.setString(4, bloodType);
                patientStmt.setString(5, address);
                patientStmt.setInt(6, userId);
                patientRows = patientStmt.executeUpdate();
            }

            if (userRows <= 0 || patientRows <= 0) {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all patients for admin dashboard
    public java.util.List<Patient> getAllPatients() {
        String query = "SELECT p.id, p.user_id, p.patient_code, p.phone, p.date_of_birth, p.gender, " +
                "p.blood_type, p.address, p.account_status, p.last_visit, u.full_name, u.email " +
                "FROM patients p " +
                "LEFT JOIN users u ON p.user_id = u.id " +
                "ORDER BY p.id DESC";
        java.util.List<Patient> patients = new java.util.ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n🔍 Executing getAllPatients query with LEFT JOIN...");
            int count = 0;

            while (rs.next()) {
                Patient patient = new Patient();
                patient.setId(rs.getInt("id"));
                patient.setUserId(rs.getInt("user_id"));
                patient.setPatientCode(rs.getString("patient_code"));
                patient.setPhone(rs.getString("phone"));
                patient.setDateOfBirth(rs.getString("date_of_birth"));
                patient.setGender(rs.getString("gender"));
                patient.setBloodType(rs.getString("blood_type"));
                patient.setAddress(rs.getString("address"));
                patient.setAccountStatus(rs.getString("account_status"));
                patient.setFullName(rs.getString("full_name"));
                patient.setEmail(rs.getString("email"));
                // Set last visit - handle null values
                String lastVisit = rs.getString("last_visit");
                if (lastVisit != null) {
                    patient.setLastVisit(lastVisit);
                }
                patients.add(patient);
                count++;
                System.out.println("   ✅ Loaded Patient #" + count + ": Code=" + patient.getPatientCode() +
                        ", Name=" + (patient.getFullName() != null ? patient.getFullName() : "N/A") +
                        ", Status=" + patient.getAccountStatus());
            }
            System.out.println("✅ Successfully fetched " + patients.size() + " total patients from database\n");
        } catch (SQLException e) {
            System.out.println("❌ Database error fetching all patients: " + e.getMessage());
            e.printStackTrace();
        }
        return patients;
    }

    // Update patient account status (and sync with user status)
    public boolean updatePatientStatus(int patientId, String newStatus) {
        String getPatientQuery = "SELECT user_id FROM patients WHERE id = ?";
        String updatePatientQuery = "UPDATE patients SET account_status = ? WHERE id = ?";
        String updateUserQuery = "UPDATE users SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("\n📝 UPDATING PATIENT STATUS:");
            System.out.println("   Patient ID: " + patientId);
            System.out.println("   New Status: " + newStatus);

            // First, get the user_id associated with this patient
            int userId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(getPatientQuery)) {
                pstmt.setInt(1, patientId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                    System.out.println("   ✅ Found user_id: " + userId);
                } else {
                    System.out.println("   ❌ Patient not found with ID: " + patientId);
                    return false;
                }
            }

            // Update patient account_status in patients table
            try (PreparedStatement pstmt = conn.prepareStatement(updatePatientQuery)) {
                pstmt.setString(1, newStatus);
                pstmt.setInt(2, patientId);
                int rowsAffected = pstmt.executeUpdate();
                System.out.println("   ✅ Updated patients table - Rows affected: " + rowsAffected);
            }

            // Update user status in users table to keep in sync
            try (PreparedStatement pstmt = conn.prepareStatement(updateUserQuery)) {
                pstmt.setString(1, newStatus);
                pstmt.setInt(2, userId);
                int rowsAffected = pstmt.executeUpdate();
                System.out.println("   ✅ Updated users table - Rows affected: " + rowsAffected);
            }

            System.out.println("   ✅ Status updated successfully in both tables\n");
            return true;

        } catch (SQLException e) {
            System.out.println("❌ Database error updating patient status: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Delete patient account (and associated user)
    public boolean deletePatient(int patientId) {
        String deletePatientQuery = "DELETE FROM patients WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("\n🗑️  DELETE SERVICE: Attempting to delete patient ID: " + patientId);

            // First get the user_id before deleting
            String getUserIdQuery = "SELECT user_id FROM patients WHERE id = ?";
            int userId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(getUserIdQuery)) {
                pstmt.setInt(1, patientId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                    System.out.println("✅ Found user_id: " + userId);
                }
            }

            if (userId == -1) {
                System.out.println("❌ Patient not found: " + patientId);
                return false;
            }

            // Delete patient record
            try (PreparedStatement pstmt = conn.prepareStatement(deletePatientQuery)) {
                pstmt.setInt(1, patientId);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("✅ Patient record deleted successfully");

                    // Delete corresponding user record
                    String deleteUserQuery = "DELETE FROM users WHERE id = ?";
                    try (PreparedStatement pstmt2 = conn.prepareStatement(deleteUserQuery)) {
                        pstmt2.setInt(1, userId);
                        int userRowsAffected = pstmt2.executeUpdate();
                        if (userRowsAffected > 0) {
                            System.out.println("✅ User record deleted successfully");
                            System.out.println("✅ Patient and user deletion complete\n");
                            return true;
                        } else {
                            System.out.println("⚠️  Patient deleted but user record not found");
                            return true;
                        }
                    }
                } else {
                    System.out.println("❌ Failed to delete patient record");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Database error during patient deletion: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Search patients by name, patient code, phone, or date of birth
     */
    public java.util.List<Patient> searchPatients(String keyword) {
        System.out.println("\n🔍 PATIENT SERVICE: Searching patients with keyword: " + keyword);
        java.util.List<Patient> patients = new java.util.ArrayList<>();

        String query = "SELECT p.id, p.user_id, p.patient_code, p.phone, p.date_of_birth, p.gender, " +
                "p.blood_type, p.address, p.account_status, p.last_visit, u.full_name, u.email " +
                "FROM patients p " +
                "JOIN users u ON p.user_id = u.id " +
                "WHERE u.full_name LIKE ? OR p.patient_code LIKE ? OR p.phone LIKE ? OR p.date_of_birth LIKE ? " +
                "ORDER BY u.full_name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Patient patient = new Patient();
                    patient.setId(rs.getInt("id"));
                    patient.setUserId(rs.getInt("user_id"));
                    patient.setPatientCode(rs.getString("patient_code"));
                    patient.setPhone(rs.getString("phone"));
                    patient.setDateOfBirth(rs.getString("date_of_birth"));
                    patient.setGender(rs.getString("gender"));
                    patient.setBloodType(rs.getString("blood_type"));
                    patient.setAddress(rs.getString("address"));
                    patient.setAccountStatus(rs.getString("account_status"));
                    patient.setFullName(rs.getString("full_name"));
                    patient.setEmail(rs.getString("email"));
                    String lastVisit = rs.getString("last_visit");
                    if (lastVisit != null) {
                        patient.setLastVisit(lastVisit);
                    }
                    patients.add(patient);
                }
            }
            System.out.println("✅ Found " + patients.size() + " matching patients");
        } catch (SQLException e) {
            System.out.println("❌ Database error during patient search: " + e.getMessage());
            e.printStackTrace();
        }
        return patients;
    }

    /**
     * Register patient from receptionist dashboard
     */
    public Patient registerPatientFromReceptionist(String fullName, String email, String password,
                                                    String phone, String dateOfBirth, String gender) {
        String userQuery = "INSERT INTO users (full_name, email, password_hash, role, status) VALUES (?, ?, ?, 'patient', 'active')";
        String patientQuery = "INSERT INTO patients (user_id, patient_code, phone, date_of_birth, gender, account_status) VALUES (?, ?, ?, ?, ?, 'active')";
        String lastIdQuery = "SELECT last_insert_rowid() as id";

        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("\n📝 RECEPTIONIST: Registering new patient: " + email);

            // Hash password using BCrypt
            String hashedPassword = passwordEncoder.encode(password);
            System.out.println("✅ Password hashed successfully");

            // Generate unique patient code
            String patientCode = com.NexusHelth.util.IdGeneratorUtil.generatePatientCode();
            System.out.println("✅ Patient code generated: " + patientCode);

            // Insert user
            try (PreparedStatement pstmt = conn.prepareStatement(userQuery)) {
                pstmt.setString(1, fullName);
                pstmt.setString(2, email);
                pstmt.setString(3, hashedPassword);
                pstmt.executeUpdate();
                System.out.println("✅ User inserted into database");

                // Get the last inserted user ID
                try (PreparedStatement idStmt = conn.prepareStatement(lastIdQuery);
                     ResultSet rs = idStmt.executeQuery()) {
                    if (rs.next()) {
                        int userId = rs.getInt("id");
                        System.out.println("✅ User ID: " + userId);

                        // Insert patient record
                        try (PreparedStatement pstmt2 = conn.prepareStatement(patientQuery)) {
                            String normalizedGender = ValidationUtil.normalizeGender(gender);
                            pstmt2.setInt(1, userId);
                            pstmt2.setString(2, patientCode);
                            pstmt2.setString(3, phone);
                            pstmt2.setString(4, dateOfBirth);
                            pstmt2.setString(5, normalizedGender);
                            pstmt2.executeUpdate();
                            System.out.println("✅ Patient record inserted successfully");

                            // Return the created patient
                            Patient patient = getPatientByUserId(userId);
                            System.out.println("✅ Patient registration complete for: " + email + "\n");
                            return patient;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Database error during registration: " + e.getMessage());
            if (e.getMessage().contains("UNIQUE")) {
                System.out.println("❌ Email already exists in the system");
            }
            e.printStackTrace();
        }
        System.out.println("❌ Patient registration failed for: " + email + "\n");
        return null;
    }
}
