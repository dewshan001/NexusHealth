package com.NexusHelth.controller;

import com.NexusHelth.model.User;
import com.NexusHelth.service.PharmacistPrescriptionService;
import com.NexusHelth.service.PharmacistInvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pharmacist/prescriptions")
public class PharmacistPrescriptionController {

    private final PharmacistPrescriptionService service;
    private final PharmacistInvoiceService invoiceService;

    public PharmacistPrescriptionController() {
        this.service = new PharmacistPrescriptionService();
        this.invoiceService = new PharmacistInvoiceService();
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingPrescriptions(HttpSession session) {
        if (!isAuthorized(session)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
        }

        List<Map<String, Object>> prescriptions = service.getPendingPrescriptions();
        return ResponseEntity.ok(Map.of("success", true, "data", prescriptions));
    }

    @PostMapping("/{id}/dispense")
    public ResponseEntity<?> dispensePrescription(@PathVariable int id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"pharmacist".equals(user.getRole())) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
        }

        boolean success = service.dispensePrescription(id, user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        if (!success) {
            response.put("error", "Failed to dispense prescription. It may not be pending.");
            return ResponseEntity.badRequest().body(response);
        }

        // Get the generated invoice details
        Map<String, Object> invoiceData = invoiceService.generateInvoiceForPrescription(id);
        if ((Boolean) invoiceData.get("success")) {
            response.put("invoiceId", invoiceData.get("invoiceId"));
            response.put("invoiceData", invoiceData);
        }

        return ResponseEntity.ok(response);
    }

    private boolean isAuthorized(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && "pharmacist".equals(user.getRole());
    }
}
