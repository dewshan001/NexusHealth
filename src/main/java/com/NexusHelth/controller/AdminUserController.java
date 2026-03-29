package com.NexusHelth.controller;

import com.NexusHelth.model.User;
import com.NexusHelth.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    private UserService userService = new UserService();

    @PostMapping("/users/create")
    public CreateUserResponse createUser(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String role,
            @RequestParam String password,
            HttpSession session) {
            
        System.out.println("\n==================== API CREATE-USER REQUEST ====================");
        
        // Verifying Admin Access
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null || !user.getRole().equals("admin")) {
            System.out.println("❌ Unauthorized access attempt - not an admin");
            return new CreateUserResponse(false, "Unauthorized access");
        }
        
        System.out.println("✅ Admin verified: " + user.getEmail());
        
        // Input validation
        if (fullName == null || fullName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            role == null || role.trim().isEmpty()) {
            return new CreateUserResponse(false, "All fields are required");
        }
        
        // Validating role
        String lowerRole = role.toLowerCase();
        if (!lowerRole.equals("pharmacist") && 
            !lowerRole.equals("receptionist") && !lowerRole.equals("admin")) {
            return new CreateUserResponse(false, "Invalid role specified for general staff creation");
        }

        boolean success = userService.createUser(fullName, email, password, lowerRole);
        
        if (success) {
            return new CreateUserResponse(true, "User created successfully!");
        } else {
            return new CreateUserResponse(false, "Failed to create user. Email may already exist.");
        }
    }

    public static class CreateUserResponse {
        public boolean success;
        public String message;

        public CreateUserResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    @PostMapping("/doctors/create")
    public CreateUserResponse createDoctor(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String licenseNumber,
            @RequestParam(required = false) String assignedRoom,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false, defaultValue = "30") int consultationDurationMin,
            @RequestParam(required = false) String workingHoursStart,
            @RequestParam(required = false) String workingHoursEnd,
            @RequestParam(required = false, defaultValue = "0") int yearsExperience,
            HttpSession session) {
            
        System.out.println("\n==================== API CREATE-DOCTOR REQUEST ====================");
        System.out.println("Specialization: " + specialization);
        
        // Verifying Admin Access
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null || !user.getRole().equals("admin")) {
            System.out.println("❌ Unauthorized access attempt - not an admin");
            return new CreateUserResponse(false, "Unauthorized access");
        }
        
        // Input validation
        if (fullName == null || fullName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return new CreateUserResponse(false, "Name, email and password are required");
        }

        boolean success = userService.createDoctor(fullName, email, password, licenseNumber, assignedRoom, 
                                                   consultationDurationMin, workingHoursStart, workingHoursEnd, yearsExperience, specialization);
        
        if (success) {
            return new CreateUserResponse(true, "Doctor created successfully!");
        } else {
            return new CreateUserResponse(false, "Failed to create doctor. Email may already exist.");
        }
    }

    @GetMapping("/staff")
    public StaffListResponse getAllStaff(HttpSession session) {
        System.out.println("\n==================== API GET-ALL-STAFF REQUEST ====================");
        
        // Verifying Admin Access
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null || !user.getRole().equals("admin")) {
            System.out.println("❌ Unauthorized access attempt - not an admin");
            return new StaffListResponse(false, null, "Unauthorized access");
        }
        
        List<User> staff = userService.getAllStaff();
        return new StaffListResponse(true, staff, null);
    }

    public static class StaffListResponse {
        public boolean success;
        public List<User> data;
        public String error;

        public StaffListResponse(boolean success, List<User> data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }

        public boolean isSuccess() { return success; }
        public List<User> getData() { return data; }
        public String getError() { return error; }
    }

    @PostMapping("/staff/status")
    public StatusUpdateResponse updateStaffStatus(
            @RequestParam int userId,
            @RequestParam String status,
            HttpSession session) {
        System.out.println("\n==================== API UPDATE-STAFF-STATUS REQUEST ====================");
        
        // Verifying Admin Access
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null || !user.getRole().equals("admin")) {
            System.out.println("❌ Unauthorized access attempt - not an admin");
            return new StatusUpdateResponse(false, "Unauthorized access");
        }
        
        // Don't allow admin to deactivate themselves
        if (userId == user.getId()) {
             return new StatusUpdateResponse(false, "Cannot deactivate your own account");
        }

        boolean success = userService.updateUserStatus(userId, status);
        if (success) {
            return new StatusUpdateResponse(true, "Staff status updated successfully");
        } else {
            return new StatusUpdateResponse(false, "Failed to update staff status");
        }
    }

    @PostMapping("/staff/update")
    public StatusUpdateResponse updateStaffDetails(
            @RequestParam int userId,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String role,
            HttpSession session) {
        System.out.println("\n==================== API UPDATE-STAFF REQUEST ====================");
        
        // Verifying Admin Access
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null || !user.getRole().equals("admin")) {
            System.out.println("❌ Unauthorized access attempt - not an admin");
            return new StatusUpdateResponse(false, "Unauthorized access");
        }

        // Don't allow admin to change their own role to something else
        if (userId == user.getId() && !role.equalsIgnoreCase("admin")) {
            return new StatusUpdateResponse(false, "Cannot change your own admin role");
        }
        
        boolean success = userService.updateStaffDetails(userId, fullName, email, role.toLowerCase());
        if (success) {
            return new StatusUpdateResponse(true, "Staff account updated successfully");
        } else {
            return new StatusUpdateResponse(false, "Failed to update staff account");
        }
    }

    @PostMapping("/staff/delete")
    public StatusUpdateResponse deleteStaff(
            @RequestParam int userId,
            HttpSession session) {
        System.out.println("\n==================== API DELETE-STAFF REQUEST ====================");
        
        // Verifying Admin Access
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null || !user.getRole().equals("admin")) {
            System.out.println("❌ Unauthorized access attempt - not an admin");
            return new StatusUpdateResponse(false, "Unauthorized access");
        }
        
        // Don't allow admin to delete themselves
        if (userId == user.getId()) {
             return new StatusUpdateResponse(false, "Cannot delete your own account");
        }

        boolean success = userService.deleteUser(userId);
        if (success) {
            return new StatusUpdateResponse(true, "Staff account deleted successfully");
        } else {
            return new StatusUpdateResponse(false, "Failed to delete staff account");
        }
    }

    public static class StatusUpdateResponse {
        public boolean success;
        public String message;

        public StatusUpdateResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}

