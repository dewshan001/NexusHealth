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
        
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
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

