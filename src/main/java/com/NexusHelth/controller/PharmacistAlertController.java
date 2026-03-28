package com.NexusHelth.controller;

import com.NexusHelth.model.PharmacistAlert;
import com.NexusHelth.model.User;
import com.NexusHelth.service.PharmacistAlertService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacist/alerts")
public class PharmacistAlertController {

    private final PharmacistAlertService alertService = new PharmacistAlertService();

    @GetMapping
    public AlertListResponse listAlerts(HttpSession session) {
        if (!isPharmacist(session)) {
            return new AlertListResponse(false, null, "Unauthorized access");
        }

        List<PharmacistAlert> data = alertService.getActiveAlerts();
        return new AlertListResponse(true, data, null);
    }

    private boolean isPharmacist(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "pharmacist".equalsIgnoreCase(user.getRole());
    }

    public static class AlertListResponse {
        public boolean success;
        public List<PharmacistAlert> data;
        public String error;

        public AlertListResponse(boolean success, List<PharmacistAlert> data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }
    }
}

