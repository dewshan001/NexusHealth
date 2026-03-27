package com.nexushelth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDTO {
    private Long id;
    private UserDTO user;
    private String specialization;
    private String licenseNumber;
    private String bio;
    private Double consultationFee;
    private String availableHours;
    private Boolean isAvailable;
}

