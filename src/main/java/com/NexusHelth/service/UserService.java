package com.NexusHelth.service;

import com.NexusHelth.model.User;
import com.NexusHelth.util.DatabaseConnection;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Service for user authentication supporting all roles (admin, doctor, patient, etc.)
 */
public class UserService {
    
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Login user with any role (admin, doctor, receptionist, pharmacist, patient)
     */
    public User loginUser(String email, String password) {
        String query = "SELECT id, full_name, email, password_hash, role, status FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            System.out.println("\n📝 USER LOGIN SERVICE: Attempting to login user: " + email);
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("✅ User found in database: " + email);
                String storedHash = rs.getString("password_hash");
                String role = rs.getString("role");
                String status = rs.getString("status");
                
                System.out.println("✅ User role: " + role);
                System.out.println("✅ User status: " + status);
                System.out.println("🔐 Verifying password...");
                
                // Use BCrypt to verify the password
                if (passwordEncoder.matches(password, storedHash)) {
                    System.out.println("✅ Password verified successfully");
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(role);
                    user.setStatus(status);
                    System.out.println("✅ User object created successfully\n");
                    return user;
                } else {
                    System.out.println("❌ Password verification failed - wrong password\n");
                }
            } else {
                System.out.println("❌ User not found in database with email: " + email + "\n");
            }
        } catch (SQLException e) {
            System.out.println("❌ Database error during login: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get the appropriate dashboard path based on user role
     */
    public static String getDashboardPath(String role) {
        return switch (role.toLowerCase()) {
            case "admin" -> "/admin-dashboard";
            case "doctor" -> "/doctor-dashboard";
            case "pharmacist" -> "/pharmacist-dashboard";
            case "receptionist" -> "/receptionist-dashboard";
            case "patient" -> "/patient-dashboard";
            default -> "/login";
        };
    }

    /**
     * Create a new user (used by admin to create staff)
     */
    public boolean createUser(String fullName, String email, String password, String role) {
        String query = "INSERT INTO users (full_name, email, password_hash, role, status) VALUES (?, ?, ?, ?, 'active')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            System.out.println("\n👤 CREATING NEW USER:");
            System.out.println("   Name: " + fullName);
            System.out.println("   Email: " + email);
            System.out.println("   Role: " + role);
            
            String hashedPassword = passwordEncoder.encode(password);
            
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, hashedPassword);
            pstmt.setString(4, role.toLowerCase());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("   ✅ User successfully created in database\n");
                
                if ("doctor".equalsIgnoreCase(role)) {
                    String idQuery = "SELECT last_insert_rowid()";
                    try (java.sql.Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(idQuery)) {
                        if (rs.next()) {
                            int userId = rs.getInt(1);
                            String docQuery = "INSERT INTO doctors (user_id) VALUES (?)";
                            try (PreparedStatement dpstmt = conn.prepareStatement(docQuery)) {
                                dpstmt.setInt(1, userId);
                                dpstmt.executeUpdate();
                            }
                        }
                    }
                }
                
                return true;
            } else {
                System.out.println("   ❌ Failed to create user - no rows affected\n");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("❌ Database error creating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create a new doctor (used by admin)
     */
    public boolean createDoctor(String fullName, String email, String password, String licenseNumber, String assignedRoom, int durationMin, String workStart, String workEnd, int experience, String specialization) {
        String insertUserQuery = "INSERT INTO users (full_name, email, password_hash, role, status) VALUES (?, ?, ?, 'doctor', 'active')";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
            try (PreparedStatement pstmt = conn.prepareStatement(insertUserQuery)) {
                
                System.out.println("\n👨‍⚕️ CREATING NEW DOCTOR:");
                System.out.println("   Name: " + fullName);
                System.out.println("   Email: " + email);
                System.out.println("   Specialization: " + specialization);
                
                String hashedPassword = passwordEncoder.encode(password);
                
                pstmt.setString(1, fullName);
                pstmt.setString(2, email);
                pstmt.setString(3, hashedPassword);
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    System.out.println("   ✅ Doctor user successfully created in database");
                    
                    String idQuery = "SELECT last_insert_rowid()";
                    int userId = -1;
                    try (java.sql.Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(idQuery)) {
                        if (rs.next()) {
                            userId = rs.getInt(1);
                        }
                    }
                    
                    if (userId != -1) {
                        String insertDocQuery = "INSERT INTO doctors (user_id, license_number, assigned_room, specialization, consultation_duration_min, working_hours_start, working_hours_end, years_experience) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement dpstmt = conn.prepareStatement(insertDocQuery)) {
                            dpstmt.setInt(1, userId);
                            dpstmt.setString(2, licenseNumber);
                            dpstmt.setString(3, assignedRoom);
                            dpstmt.setString(4, specialization != null && !specialization.isEmpty() ? specialization : "General Practice");
                            dpstmt.setInt(5, durationMin);
                            dpstmt.setString(6, workStart != null && !workStart.isEmpty() ? workStart : "09:00");
                            dpstmt.setString(7, workEnd != null && !workEnd.isEmpty() ? workEnd : "17:00");
                            dpstmt.setInt(8, experience);
                            dpstmt.executeUpdate();
                        }
                        
                        conn.commit();
                        System.out.println("   ✅ Doctor details with specialization successfully added\n");
                        return true;
                    }
                }
                
                conn.rollback();
                return false;
                
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            System.out.println("❌ Database error creating doctor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all staff members (users who are not patients)
     */
    public java.util.List<User> getAllStaff() {
        String query = "SELECT id, full_name, email, role, status FROM users WHERE role != 'patient'";
        java.util.List<User> staffList = new java.util.ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setStatus(rs.getString("status"));
                staffList.add(user);
            }
        } catch (SQLException e) {
            System.out.println("❌ Database error retrieving staff: " + e.getMessage());
            e.printStackTrace();
        }
        
        return staffList;
    }

    /**
     * Update user account status
     */
    public boolean updateUserStatus(int userId, String status) {
        String query = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("❌ Database error updating user status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update user details
     */
    public boolean updateStaffDetails(int userId, String fullName, String email, String role) {
        String query = "UPDATE users SET full_name = ?, email = ?, role = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, role);
            pstmt.setInt(4, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("❌ Database error updating staff details: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete user account completely
     */
    public boolean deleteUser(int userId) {
        String pragma = "PRAGMA foreign_keys = ON;";
        String query = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            // Enable cascading deletes in SQLite for this connection
            stmt.execute(pragma);
            
            pstmt.setInt(1, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("❌ Database error deleting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
