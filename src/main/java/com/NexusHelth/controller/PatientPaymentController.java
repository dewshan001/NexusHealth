package com.NexusHelth.controller;

import com.NexusHelth.dto.InvoiceResponse;
import com.NexusHelth.model.User;
import com.NexusHelth.model.Patient;
import com.NexusHelth.service.PatientPaymentService;
import com.NexusHelth.service.PatientService;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/patient")
public class PatientPaymentController {

    private final PatientPaymentService paymentService = new PatientPaymentService();
    private final PatientService patientService = new PatientService();

    /**
     * Get all invoices for authenticated patient
     * Optional status filter: 'unpaid', 'paid', or 'all'
     */
    @GetMapping("/invoices")
    public InvoicesResponse getInvoices(
            @RequestParam(required = false) String status,
            HttpSession session) {
        
        System.out.println("[💳] Fetching invoices for patient");
        
        // Verify authentication
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new InvoicesResponse(false, null, "Authentication required");
        }
        
        // Verify patient role
        if (!user.getRole().equals("patient")) {
            return new InvoicesResponse(false, null, "Only patients can access their invoices");
        }
        
        // Get patient profile
        Patient patient = patientService.getPatientByUserId(user.getId());
        if (patient == null) {
            return new InvoicesResponse(false, null, "Patient profile not found");
        }
        
        // Fetch invoices
        List<InvoiceResponse> allInvoices = paymentService.getPatientInvoices(patient.getId());
        
        // Filter by status if provided
        List<InvoiceResponse> filteredInvoices = allInvoices;
        if (status != null && !status.isEmpty() && !status.equals("all")) {
            filteredInvoices = allInvoices.stream()
                    .filter(inv -> inv.getStatus().equalsIgnoreCase(status))
                    .toList();
        }
        
        System.out.println("✓ Retrieved " + filteredInvoices.size() + " invoices for patient " + patient.getId());
        return new InvoicesResponse(true, filteredInvoices, null);
    }

    /**
     * Get details of a specific invoice
     * Validates that the invoice belongs to the authenticated patient
     */
    @GetMapping("/invoices/{invoiceId}")
    public InvoiceDetailResponse getInvoiceDetail(
            @PathVariable int invoiceId,
            HttpSession session) {
        
        System.out.println("[💳] Fetching invoice details for ID: " + invoiceId);
        
        // Verify authentication
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new InvoiceDetailResponse(false, null, null, "Authentication required");
        }
        
        // Verify patient role or admin role
        if (!user.getRole().equals("patient") && !user.getRole().equals("admin")) {
            return new InvoiceDetailResponse(false, null, null, "Unauthorized access");
        }
        
        Patient patient = patientService.getPatientByUserId(user.getId());
        if (patient == null && !user.getRole().equals("admin")) {
            return new InvoiceDetailResponse(false, null, null, "Patient profile not found");
        }
        
        int patientId = user.getRole().equals("admin") ? -1 : patient.getId(); // -1 for admin (no filter)
        
        // For non-admin users, validate invoice ownership
        if (!user.getRole().equals("admin")) {
            if (!paymentService.validateInvoiceOwnership(invoiceId, patientId)) {
                return new InvoiceDetailResponse(false, null, null, "Invoice not found or access denied");
            }
        }
        
        // Fetch invoice details
        InvoiceResponse invoice = user.getRole().equals("admin") ? 
                getInvoiceForAdmin(invoiceId) : 
                paymentService.getInvoiceDetails(invoiceId, patientId);
        
        if (invoice == null) {
            return new InvoiceDetailResponse(false, null, null, "Invoice not found");
        }
        
        // Fetch transaction details
        List<Map<String, Object>> transactions = paymentService.getTransactionDetails(invoiceId);
        
        System.out.println("✓ Retrieved invoice details for ID: " + invoiceId);
        return new InvoiceDetailResponse(true, invoice, transactions, null);
    }

    /**
     * Record a payment for an invoice
     * Updates invoice status to 'paid' and creates a transaction record
     */
    @PostMapping("/invoices/{invoiceId}/pay")
    public PaymentResponseDTO recordPayment(
            @PathVariable int invoiceId,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) Double amountPaid,
            HttpSession session) {
        
        System.out.println("[💳] Recording payment for invoice ID: " + invoiceId);
        
        // Verify authentication
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new PaymentResponseDTO(false, "Authentication required", null, null, null, null, null);
        }
        
        // Verify patient role
        if (!user.getRole().equals("patient")) {
            return new PaymentResponseDTO(false, "Only patients can pay invoices", null, null, null, null, null);
        }
        
        // Get patient profile
        Patient patient = patientService.getPatientByUserId(user.getId());
        if (patient == null) {
            return new PaymentResponseDTO(false, "Patient profile not found", null, null, null, null, null);
        }
        
        // Validate invoice ownership
        if (!paymentService.validateInvoiceOwnership(invoiceId, patient.getId())) {
            return new PaymentResponseDTO(false, "Invoice not found or access denied", null, null, null, null, null);
        }
        
        // Check if already paid
        if (paymentService.isInvoicePaid(invoiceId)) {
            return new PaymentResponseDTO(false, "Invoice is already paid", null, invoiceId, 0.0, paymentMethod, null);
        }
        
        // Validate payment method
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            return new PaymentResponseDTO(false, "Payment method is required", null, null, null, null, null);
        }
        
        // Get invoice total amount
        double totalAmount = paymentService.getInvoiceTotalAmount(invoiceId);
        if (totalAmount <= 0) {
            return new PaymentResponseDTO(false, "Invalid invoice amount", null, invoiceId, 0.0, paymentMethod, null);
        }
        
        // Use total amount if amountPaid not specified or different
        if (amountPaid == null) {
            amountPaid = totalAmount;
        }
        
        // Validate amount
        if (amountPaid <= 0 || amountPaid > totalAmount) {
            return new PaymentResponseDTO(false, "Invalid payment amount. Must be between 0 and " + totalAmount, 
                    null, invoiceId, 0.0, paymentMethod, null);
        }
        
        // Record payment
        String transactionCode = paymentService.recordPayment(invoiceId, paymentMethod, amountPaid);
        
        if (transactionCode == null) {
            return new PaymentResponseDTO(false, "Payment recording failed", null, invoiceId, 0.0, paymentMethod, null);
        }
        
        System.out.println("✓ Payment recorded successfully. Transaction: " + transactionCode);
        return new PaymentResponseDTO(
                true, 
                "Payment recorded successfully", 
                transactionCode, 
                invoiceId, 
                amountPaid, 
                paymentMethod,
                LocalDateTime.now()
        );
    }

    /**
     * Get receipt data for an invoice
     * Returns invoice and transaction details for PDF generation on frontend
     */
    @GetMapping("/invoices/{invoiceId}/receipt")
    public ReceiptResponse getReceipt(
            @PathVariable int invoiceId,
            HttpSession session) {
        
        System.out.println("[💳] Generating receipt for invoice ID: " + invoiceId);
        
        // Verify authentication
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new ReceiptResponse(false, null, null, "Authentication required");
        }
        
        // Verify patient role or admin role
        if (!user.getRole().equals("patient") && !user.getRole().equals("admin")) {
            return new ReceiptResponse(false, null, null, "Unauthorized access");
        }
        
        Patient patient = patientService.getPatientByUserId(user.getId());
        if (patient == null && !user.getRole().equals("admin")) {
            return new ReceiptResponse(false, null, null, "Patient profile not found");
        }
        
        int patientId = user.getRole().equals("admin") ? -1 : patient.getId();
        
        // For non-admin users, validate invoice ownership
        if (!user.getRole().equals("admin")) {
            if (!paymentService.validateInvoiceOwnership(invoiceId, patientId)) {
                return new ReceiptResponse(false, null, null, "Invoice not found or access denied");
            }
        }
        
        // Fetch invoice details
        InvoiceResponse invoice = user.getRole().equals("admin") ? 
                getInvoiceForAdmin(invoiceId) : 
                paymentService.getInvoiceDetails(invoiceId, patientId);
        
        if (invoice == null) {
            return new ReceiptResponse(false, null, null, "Invoice not found");
        }
        
        // Fetch transaction details
        List<Map<String, Object>> transactions = paymentService.getTransactionDetails(invoiceId);
        
        System.out.println("✓ Receipt data generated for invoice ID: " + invoiceId);
        return new ReceiptResponse(true, invoice, transactions, null);
    }

    /**
     * Admin endpoint to view all invoices with optional patient filter
     */
    @GetMapping("/admin/invoices")
    public InvoicesResponse getAdminInvoices(
            @RequestParam(required = false) Integer patientId,
            @RequestParam(required = false) String status,
            HttpSession session) {
        
        System.out.println("[💳] Admin fetching invoices");
        
        // Verify authentication
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new InvoicesResponse(false, null, "Authentication required");
        }
        
        // Verify admin role
        if (!user.getRole().equals("admin")) {
            return new InvoicesResponse(false, null, "Only admins can access this endpoint");
        }
        
        // If patientId specified, fetch for that patient
        // Otherwise, fetch all invoices
        List<InvoiceResponse> allInvoices;
        if (patientId != null) {
            allInvoices = paymentService.getPatientInvoices(patientId);
        } else {
            // Fetch all invoices (admin view)
            allInvoices = getAllInvoices();
        }
        
        // Filter by status if provided
        List<InvoiceResponse> filteredInvoices = allInvoices;
        if (status != null && !status.isEmpty() && !status.equals("all")) {
            filteredInvoices = allInvoices.stream()
                    .filter(inv -> inv.getStatus().equalsIgnoreCase(status))
                    .toList();
        }
        
        System.out.println("✓ Admin retrieved " + filteredInvoices.size() + " invoices");
        return new InvoicesResponse(true, filteredInvoices, null);
    }

    /**
     * Helper method to fetch all invoices (admin view)
     */
    private List<InvoiceResponse> getAllInvoices() {
        // This would need to be implemented in PatientPaymentService
        // For now, returning empty list - can enhance later
        return new ArrayList<>();
    }

    /**
     * Helper method to get invoice for admin (without patient ownership check)
     */
    private InvoiceResponse getInvoiceForAdmin(int invoiceId) {
        // This would need to be implemented in PatientPaymentService
        // For now, using the regular method with a dummy patientId
        // In production, create a separate method without ownership check
        return null;
    }

    // ─────────────────────────────────────────────
    // Inner classes for JSON Responses
    // ─────────────────────────────────────────────

    public static class InvoicesResponse {
        public boolean success;
        public List<InvoiceResponse> data;
        public String error;

        public InvoicesResponse(boolean success, List<InvoiceResponse> data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }
    }

    public static class InvoiceDetailResponse {
        public boolean success;
        public InvoiceResponse invoice;
        public List<Map<String, Object>> transactions;
        public String error;

        public InvoiceDetailResponse(boolean success, InvoiceResponse invoice, 
                                    List<Map<String, Object>> transactions, String error) {
            this.success = success;
            this.invoice = invoice;
            this.transactions = transactions;
            this.error = error;
        }
    }

    public static class PaymentResponseDTO {
        public boolean success;
        public String message;
        public String transactionCode;
        public Integer invoiceId;
        public Double amountPaid;
        public String paymentMethod;
        public LocalDateTime transactionDate;
        public String error;

        public PaymentResponseDTO(boolean success, String message, String transactionCode,
                                 Integer invoiceId, Double amountPaid, String paymentMethod,
                                 LocalDateTime transactionDate) {
            this.success = success;
            this.message = message;
            this.transactionCode = transactionCode;
            this.invoiceId = invoiceId;
            this.amountPaid = amountPaid;
            this.paymentMethod = paymentMethod;
            this.transactionDate = transactionDate;
        }
    }

    public static class ReceiptResponse {
        public boolean success;
        public InvoiceResponse invoice;
        public List<Map<String, Object>> transactions;
        public String error;

        public ReceiptResponse(boolean success, InvoiceResponse invoice,
                              List<Map<String, Object>> transactions, String error) {
            this.success = success;
            this.invoice = invoice;
            this.transactions = transactions;
            this.error = error;
        }
    }
}
