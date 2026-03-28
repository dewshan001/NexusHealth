package com.NexusHelth.controller;

import com.NexusHelth.model.User;
import com.NexusHelth.model.Patient;
import com.NexusHelth.service.PatientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {
    
    private PatientService patientService = new PatientService();
    
    @GetMapping("/patient-dashboard")
    public String showDashboard(HttpSession session, Model model) {
        System.out.println("\n==================== DASHBOARD REQUEST ====================");
        System.out.println("📊 Dashboard request - Session ID: " + session.getId());
        System.out.println("🔍 Session Attributes: " + java.util.Collections.list(session.getAttributeNames()));
        System.out.println("⏱️  Session Max Inactive Interval: " + session.getMaxInactiveInterval());
        System.out.println("⏱️  Session Creation Time: " + session.getCreationTime());
        System.out.println("⏱️  Session Last Accessed Time: " + session.getLastAccessedTime());
        System.out.println("⏱️  Session Is New: " + session.isNew());
        
        Object userObj = session.getAttribute("user");
        System.out.println("👤 User object from session: " + userObj);
        System.out.println("👤 User object type: " + (userObj != null ? userObj.getClass().getName() : "null"));
        
        User user = (User) userObj;
        
        if (user == null) {
            System.out.println("❌ NO USER IN SESSION - REDIRECTING TO LOGIN");
            System.out.println("❌ All session attributes: " + java.util.Collections.list(session.getAttributeNames()));
            System.out.println("==========================================================\n");
            return "redirect:/login";
        }
        
        System.out.println("✅ User found in session: " + user.getEmail());
        System.out.println("✅ User ID: " + user.getId());
        Patient patient = patientService.getPatientByUserId(user.getId());
        
        model.addAttribute("patient", patient);
        model.addAttribute("user", user);
        System.out.println("✅ Dashboard loading successfully for user: " + user.getEmail());
        System.out.println("==========================================================\n");
        
        return "patient-dashboard";
    }
    
    @PostMapping("/update-profile")
    public String updateProfile(HttpSession session,
                               @RequestParam String phone,
                               @RequestParam String dateOfBirth,
                               @RequestParam String gender,
                               @RequestParam String bloodType,
                               @RequestParam String address,
                               RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        boolean success = patientService.updatePatientProfile(user.getId(), phone, 
                                                             dateOfBirth, gender, 
                                                             bloodType, address);
        
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile");
        }
        
        return "redirect:/patient-dashboard";
    }
}
