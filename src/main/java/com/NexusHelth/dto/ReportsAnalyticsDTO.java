package com.NexusHelth.dto;

import java.util.List;

/**
 * DTO for Reports & Analytics Dashboard
 */
public class ReportsAnalyticsDTO {
    
    private Double dailyRevenue;
    private Double pharmacySales;
    private Integer totalPatientsVisited;
    private List<TransactionDTO> recentTransactions;

    // Constructors
    public ReportsAnalyticsDTO() {
    }

    public ReportsAnalyticsDTO(Double dailyRevenue, Double pharmacySales, 
                               Integer totalPatientsVisited, List<TransactionDTO> recentTransactions) {
        this.dailyRevenue = dailyRevenue;
        this.pharmacySales = pharmacySales;
        this.totalPatientsVisited = totalPatientsVisited;
        this.recentTransactions = recentTransactions;
    }

    // Getters and Setters
    public Double getDailyRevenue() {
        return dailyRevenue;
    }

    public void setDailyRevenue(Double dailyRevenue) {
        this.dailyRevenue = dailyRevenue;
    }

    public Double getPharmacySales() {
        return pharmacySales;
    }

    public void setPharmacySales(Double pharmacySales) {
        this.pharmacySales = pharmacySales;
    }

    public Integer getTotalPatientsVisited() {
        return totalPatientsVisited;
    }

    public void setTotalPatientsVisited(Integer totalPatientsVisited) {
        this.totalPatientsVisited = totalPatientsVisited;
    }

    public List<TransactionDTO> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<TransactionDTO> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    @Override
    public String toString() {
        return "ReportsAnalyticsDTO{" +
                "dailyRevenue=" + dailyRevenue +
                ", pharmacySales=" + pharmacySales +
                ", totalPatientsVisited=" + totalPatientsVisited +
                ", recentTransactions=" + recentTransactions +
                '}';
    }
}
