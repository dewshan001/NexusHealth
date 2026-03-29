package com.NexusHelth.controller;

import com.NexusHelth.model.User;
import com.NexusHelth.service.PatientService;
import com.NexusHelth.service.UserService;
import com.NexusHelth.util.TabAuthStore;
import com.NexusHelth.util.ValidationUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class AuthController {
    
    private PatientService patientService = new PatientService();
    private UserService userService = new UserService();
    
    @GetMapping("/")
    public String showIndexPage() {
        return "index";
    }
    
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
    
    @PostMapping("/login")
    public String handleLogin(@RequestParam String email, 
                             @RequestParam String password,
                             HttpSession session,
                             RedirectAttributes redirectAttributes,
                             jakarta.servlet.http.HttpServletRequest request) {
        System.out.println("\n==================== LOGIN REQUEST ====================");
        System.out.println("📧 Email: " + email);
        System.out.println("🔐 Initial Session ID: " + session.getId());
        System.out.println("🔐 Is New Session: " + session.isNew());
        System.out.println("🔐 Request Session: " + request.getSession(false) != null);
        
        // Try to login as any user (admin, doctor, patient, etc.)
        User user = userService.loginUser(email, password);
        
        if (user != null && user.getStatus().equals("active")) {
            // Store user in Spring's built-in HTTP session
            System.out.println("📝 Before setAttribute - Session ID: " + session.getId());
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            // Issue per-tab token for multi-account support across tabs
            String tabToken = TabAuthStore.issueToken(user);
            
            // Verify attributes were set
            Object userCheck = com.NexusHelth.util.AuthSessionUtil.getUser(session);
            Object userIdCheck = session.getAttribute("userId");
            
            System.out.println("✅ User logged in: " + email);
            System.out.println("✅ User role: " + user.getRole());
            System.out.println("✅ Session ID after setAttribute: " + session.getId());
            System.out.println("✅ Session Max Inactive Interval: " + session.getMaxInactiveInterval() + " seconds");
            System.out.println("✅ User object stored: " + (userCheck != null));
            System.out.println("✅ UserId object stored: " + (userIdCheck != null));
            System.out.println("✅ Session Attributes: " + java.util.Collections.list(session.getAttributeNames()));
            System.out.println("==========================================================\n");
            
            // Redirect to appropriate dashboard based on user role
            String dashboardPath = UserService.getDashboardPath(user.getRole());
            String encodedToken = URLEncoder.encode(tabToken, StandardCharsets.UTF_8);
            return "redirect:" + dashboardPath + "#tabToken=" + encodedToken;
        } else {
            System.out.println("❌ Login failed for email: " + email);
            System.out.println("==========================================================\n");
            redirectAttributes.addFlashAttribute("error", "Invalid email or password");
            return "redirect:/login";
        }
    }
    
    @GetMapping("/signup")
    public String showSignupPage() {
        return "signup";
    }
    
    @PostMapping("/signup")
    public String handleSignup(@RequestParam String fullName,
                              @RequestParam String email,
                              @RequestParam String password,
                              @RequestParam String phone,
                              @RequestParam String dateOfBirth,
                              @RequestParam String gender,
                              @RequestParam String bloodType,
                              @RequestParam String address,
                              RedirectAttributes redirectAttributes) {
        
        // Validate all input fields
        if (!ValidationUtil.isValidFullName(fullName)) {
            redirectAttributes.addFlashAttribute("error", "Full name is required (3-100 characters)");
            return "redirect:/signup";
        }
        
        if (!ValidationUtil.isValidEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Invalid email format");
            return "redirect:/signup";
        }
        
        if (!ValidationUtil.isValidPassword(password)) {
            redirectAttributes.addFlashAttribute("error", "Password must be between 6-128 characters");
            return "redirect:/signup";
        }
        
        if (!ValidationUtil.isValidPhone(phone)) {
            redirectAttributes.addFlashAttribute("error", "Phone number is required (minimum 7 digits)");
            return "redirect:/signup";
        }
        
        if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Date of birth is required");
            return "redirect:/signup";
        }
        
        if (!ValidationUtil.isValidGender(gender)) {
            redirectAttributes.addFlashAttribute("error", "Please select a valid gender");
            return "redirect:/signup";
        }
        
        if (!ValidationUtil.isValidBloodType(bloodType)) {
            redirectAttributes.addFlashAttribute("error", "Please select a valid blood type");
            return "redirect:/signup";
        }
        
        if (!ValidationUtil.isValidAddress(address)) {
            redirectAttributes.addFlashAttribute("error", "Address is required (minimum 5 characters)");
            return "redirect:/signup";
        }
        
        // Check if email already exists
        if (patientService.emailExists(email)) {
            redirectAttributes.addFlashAttribute("error", "Email already registered");
            return "redirect:/signup";
        }
        
        // Register patient
        boolean success = patientService.registerPatient(fullName, email, password, 
                                                        phone, dateOfBirth, gender, 
                                                        bloodType, address);
        
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "Registration failed. Please try again");
            return "redirect:/signup";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @PostMapping("/api/logout")
    @ResponseBody
    public Map<String, Object> apiLogout(HttpServletRequest request) {
        String token = TabAuthStore.extractToken(request);
        if (token != null) {
            TabAuthStore.revoke(token);
            return Map.of("success", true);
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Map.of("success", true);
    }
    
    @GetMapping("/about")
    public String showAboutPage() {
        return "about";
    }
    
    @GetMapping("/contact")
    public String showContactPage() {
        return "contact";
    }
    
    @GetMapping("/admin-dashboard")
    public String showAdminDashboard(HttpSession session, Model model) {
        return "admin-dashboard";
    }
    
    @GetMapping("/doctor-dashboard")
    public String showDoctorDashboard(HttpSession session, Model model) {
        return "doctor-dashboard";
    }
    
    @GetMapping("/pharmacist-dashboard")
    public String showPharmacistDashboard(HttpSession session, Model model) {
        return "pharmacist-dashboard";
    }
    
    @GetMapping("/receptionist-dashboard")
    public String showReceptionistDashboard(HttpSession session, Model model) {
        return "receptionist-dashboard";
    }
}


