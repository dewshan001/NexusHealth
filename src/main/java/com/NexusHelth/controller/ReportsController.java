package com.NexusHelth.controller;

import com.NexusHelth.dto.ReportsAnalyticsDTO;
import com.NexusHelth.model.User;
import com.NexusHelth.service.ReportsService;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Reports & Analytics endpoints
 * Requires admin authentication
 */
@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    private final ReportsService reportsService = new ReportsService();

    /**
     * Get reports and analytics data for a specific date
     * Requires admin authentication
     * 
     * Query Parameters:
     *  - date: (optional) yyyy-MM-dd format. Defaults to today's date
     * 
     * Returns:
     *  - dailyRevenue: Sum of settled transactions for the date
     *  - pharmacySales: Sum of pharmacy addons from paid invoices
     *  - totalPatientsVisited: Count of completed/confirmed appointments
     *  - recentTransactions: List of last 5 transactions
     */
    @GetMapping("/analytics")
    public Map<String, Object> getReportsAnalytics(
            @RequestParam(required = false) String date,
            HttpSession session) {

        System.out.println("[📊] Fetching Reports & Analytics");

        // Verify authentication
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null) {
            return errorResponse(false, "Authentication required", 401);
        }

        // Verify admin role
        if (!user.getRole().equals("admin")) {
            return errorResponse(false, "Only admins can access reports", 403);
        }

        try {
            // Parse date or use today's date
            LocalDate reportDate = LocalDate.now();
            if (date != null && !date.isEmpty()) {
                try {
                    reportDate = LocalDate.parse(date); // expects yyyy-MM-dd format
                } catch (Exception e) {
                    return errorResponse(false, "Invalid date format. Use yyyy-MM-dd", 400);
                }
            }

            // Fetch reports data
            ReportsAnalyticsDTO reports = reportsService.getReportsAnalyticsForDate(reportDate);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("date", reportDate.toString());
            response.put("dailyRevenue", reports.getDailyRevenue());
            response.put("pharmacySales", reports.getPharmacySales());
            response.put("totalPatientsVisited", reports.getTotalPatientsVisited());
            response.put("recentTransactions", reports.getRecentTransactions());
            response.put("timestamp", LocalDateTime.now().toString());

            System.out.println("✅ Reports data retrieved successfully for date: " + reportDate);
            return response;

        } catch (Exception e) {
            System.out.println("❌ Error fetching reports: " + e.getMessage());
            e.printStackTrace();
            return errorResponse(false, "Error fetching reports: " + e.getMessage(), 500);
        }
    }

    /**
     * Get daily revenue for a specific date
     */
    @GetMapping("/daily-revenue")
    public Map<String, Object> getDailyRevenue(
            @RequestParam(required = false) String date,
            HttpSession session) {

        System.out.println("[💰] Fetching Daily Revenue");

        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null) {
            return errorResponse(false, "Authentication required", 401);
        }

        if (!user.getRole().equals("admin")) {
            return errorResponse(false, "Only admins can access reports", 403);
        }

        try {
            LocalDate reportDate = LocalDate.now();
            if (date != null && !date.isEmpty()) {
                reportDate = LocalDate.parse(date);
            }

            ReportsAnalyticsDTO reports = reportsService.getReportsAnalyticsForDate(reportDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("date", reportDate.toString());
            response.put("dailyRevenue", reports.getDailyRevenue());
            response.put("timestamp", LocalDateTime.now().toString());

            System.out.println("✅ Daily revenue retrieved: Rs." + reports.getDailyRevenue());
            return response;

        } catch (Exception e) {
            return errorResponse(false, "Error fetching daily revenue: " + e.getMessage(), 500);
        }
    }

    /**
     * Get pharmacy sales for a specific date
     */
    @GetMapping("/pharmacy-sales")
    public Map<String, Object> getPharmacySales(
            @RequestParam(required = false) String date,
            HttpSession session) {

        System.out.println("[💊] Fetching Pharmacy Sales");

        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null) {
            return errorResponse(false, "Authentication required", 401);
        }

        if (!user.getRole().equals("admin")) {
            return errorResponse(false, "Only admins can access reports", 403);
        }

        try {
            LocalDate reportDate = LocalDate.now();
            if (date != null && !date.isEmpty()) {
                reportDate = LocalDate.parse(date);
            }

            ReportsAnalyticsDTO reports = reportsService.getReportsAnalyticsForDate(reportDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("date", reportDate.toString());
            response.put("pharmacySales", reports.getPharmacySales());
            response.put("timestamp", LocalDateTime.now().toString());

            System.out.println("✅ Pharmacy sales retrieved: Rs." + reports.getPharmacySales());
            return response;

        } catch (Exception e) {
            return errorResponse(false, "Error fetching pharmacy sales: " + e.getMessage(), 500);
        }
    }

    /**
     * Get total patients visited for a specific date
     */
    @GetMapping("/patients-visited")
    public Map<String, Object> getPatientsVisited(
            @RequestParam(required = false) String date,
            HttpSession session) {

        System.out.println("[👥] Fetching Total Patients Visited");

        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        if (user == null) {
            return errorResponse(false, "Authentication required", 401);
        }

        if (!user.getRole().equals("admin")) {
            return errorResponse(false, "Only admins can access reports", 403);
        }

        try {
            LocalDate reportDate = LocalDate.now();
            if (date != null && !date.isEmpty()) {
                reportDate = LocalDate.parse(date);
            }

            ReportsAnalyticsDTO reports = reportsService.getReportsAnalyticsForDate(reportDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("date", reportDate.toString());
            response.put("totalPatientsVisited", reports.getTotalPatientsVisited());
            response.put("timestamp", LocalDateTime.now().toString());

            System.out.println("✅ Total patients visited retrieved: " + reports.getTotalPatientsVisited());
            return response;

        } catch (Exception e) {
            return errorResponse(false, "Error fetching patients visited: " + e.getMessage(), 500);
        }
    }

    /**
     * Utility method to create error responses
     */
    private Map<String, Object> errorResponse(boolean success, String message, int statusCode) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("statusCode", statusCode);
        return response;
    }
}

