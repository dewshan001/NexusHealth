package com.NexusHelth.controller;

import com.NexusHelth.model.Medicine;
import com.NexusHelth.model.User;
import com.NexusHelth.service.PharmacistAlertService;
import com.NexusHelth.service.PharmacistInventoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacist/inventory")
public class PharmacistInventoryController {

    private final PharmacistInventoryService inventoryService = new PharmacistInventoryService();
    private final PharmacistAlertService alertService = new PharmacistAlertService();

    @GetMapping
    public InventoryListResponse listInventory(HttpSession session) {
        if (!isPharmacist(session)) {
            return new InventoryListResponse(false, null, "Unauthorized access");
        }

        List<Medicine> data = inventoryService.getAllMedicines();
        return new InventoryListResponse(true, data, null);
    }

    @PostMapping("/create")
    public StandardResponse createInventory(
            @RequestParam String name,
            @RequestParam(required = false) String batchNumber,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "0") double unitPrice,
            @RequestParam(defaultValue = "0") int stockLevel,
            @RequestParam(required = false) String expiryDate,
            HttpSession session) {

        if (!isPharmacist(session)) {
            return new StandardResponse(false, "Unauthorized access");
        }

        if (name == null || name.trim().isEmpty()) {
            return new StandardResponse(false, "Medicine name is required");
        }

        if (stockLevel < 0) {
            return new StandardResponse(false, "Stock level cannot be negative");
        }

        boolean success = inventoryService.createMedicine(name, batchNumber, category, unitPrice, stockLevel, expiryDate);
        if (success) {
            alertService.refreshAlertsFromInventory();
        }
        return success
                ? new StandardResponse(true, "Inventory item created successfully")
                : new StandardResponse(false, "Failed to create inventory item");
    }

    @PostMapping("/update")
    public StandardResponse updateInventory(
            @RequestParam int id,
            @RequestParam String name,
            @RequestParam(required = false) String batchNumber,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "0") double unitPrice,
            @RequestParam(defaultValue = "0") int stockLevel,
            @RequestParam(required = false) String expiryDate,
            HttpSession session) {

        if (!isPharmacist(session)) {
            return new StandardResponse(false, "Unauthorized access");
        }

        if (id <= 0) {
            return new StandardResponse(false, "Invalid inventory item ID");
        }

        if (name == null || name.trim().isEmpty()) {
            return new StandardResponse(false, "Medicine name is required");
        }

        if (stockLevel < 0) {
            return new StandardResponse(false, "Stock level cannot be negative");
        }

        boolean success = inventoryService.updateMedicine(id, name, batchNumber, category, unitPrice, stockLevel, expiryDate);
        if (success) {
            alertService.refreshAlertsFromInventory();
        }
        return success
                ? new StandardResponse(true, "Inventory item updated successfully")
                : new StandardResponse(false, "Failed to update inventory item");
    }

    @PostMapping("/delete")
    public StandardResponse deleteInventory(
            @RequestParam int id,
            HttpSession session) {

        if (!isPharmacist(session)) {
            return new StandardResponse(false, "Unauthorized access");
        }

        if (id <= 0) {
            return new StandardResponse(false, "Invalid inventory item ID");
        }

        boolean success = inventoryService.deleteMedicine(id);
        if (success) {
            alertService.refreshAlertsFromInventory();
        }
        return success
                ? new StandardResponse(true, "Inventory item deleted successfully")
                : new StandardResponse(false, "Failed to delete inventory item");
    }

    private boolean isPharmacist(HttpSession session) {
        User user = com.NexusHelth.util.AuthSessionUtil.getUser(session);
        return user != null && "pharmacist".equalsIgnoreCase(user.getRole());
    }

    public static class InventoryListResponse {
        public boolean success;
        public List<Medicine> data;
        public String error;

        public InventoryListResponse(boolean success, List<Medicine> data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
        }
    }

    public static class StandardResponse {
        public boolean success;
        public String message;

        public StandardResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}


