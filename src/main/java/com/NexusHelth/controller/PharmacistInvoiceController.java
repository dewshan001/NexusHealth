package com.NexusHelth.controller;

import com.NexusHelth.model.User;
import com.NexusHelth.service.PharmacistInvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pharmacist/invoices")
public class PharmacistInvoiceController {

    private final PharmacistInvoiceService service;

    public PharmacistInvoiceController() {
        this.service = new PharmacistInvoiceService();
    }

    /**
     * Generate invoice for a prescription
     */
    @PostMapping("/generate/{prescriptionId}")
    public ResponseEntity<?> generateInvoice(@PathVariable int prescriptionId, HttpSession session) {
        if (!isAuthorized(session)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
        }

        Map<String, Object> result = service.generateInvoiceForPrescription(prescriptionId);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Get invoice details with items
     */
    @GetMapping("/{invoiceId}")
    public ResponseEntity<?> getInvoice(@PathVariable int invoiceId, HttpSession session) {
        if (!isAuthorized(session)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
        }

        Map<String, Object> invoice = service.getInvoiceById(invoiceId);
        
        if ((Boolean) invoice.getOrDefault("found", false)) {
            return ResponseEntity.ok(Map.of("success", true, "data", invoice));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all invoices (limited)
     */
    @GetMapping
    public ResponseEntity<?> getAllInvoices(@RequestParam(defaultValue = "50") int limit, HttpSession session) {
        if (!isAuthorized(session)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
        }

        List<Map<String, Object>> invoices = service.getAllInvoices(limit);
        return ResponseEntity.ok(Map.of("success", true, "data", invoices));
    }

    /**
     * Get invoice as PDF (base64 encoded)
     * This endpoint returns the invoice data that can be used client-side to generate PDF
     */
    @GetMapping("/{invoiceId}/pdf")
    public ResponseEntity<?> getInvoicePDF(@PathVariable int invoiceId, HttpSession session) {
        if (!isAuthorized(session)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
        }

        Map<String, Object> invoice = service.getInvoiceById(invoiceId);
        
        if ((Boolean) invoice.getOrDefault("found", false)) {
            // Return invoice data for client-side PDF generation (using jsPDF)
            return ResponseEntity.ok(Map.of("success", true, "data", invoice));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private boolean isAuthorized(HttpSession session) {
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        return user != null && "pharmacist".equals(user.getRole());
    }
}

