package com.NexusHelth.model;

public class Medicine {
    private int id;
    private String name;
    private String batchNumber;
    private String category;
    private double unitPrice;
    private int stockLevel;
    private String expiryDate;
    private String status;

    public Medicine() {
    }

    public Medicine(int id, String name, String batchNumber, String category, double unitPrice, int stockLevel,
            String expiryDate, String status) {
        this.id = id;
        this.name = name;
        this.batchNumber = batchNumber;
        this.category = category;
        this.unitPrice = unitPrice;
        this.stockLevel = stockLevel;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getStockLevel() {
        return stockLevel;
    }

    public void setStockLevel(int stockLevel) {
        this.stockLevel = stockLevel;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

