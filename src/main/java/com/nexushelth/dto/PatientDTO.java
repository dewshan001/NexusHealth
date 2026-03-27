package com.nexushelth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDTO {
    private Long id;
    private UserDTO user;
    private LocalDate dateOfBirth;
    private String bloodType;
    private String medicalHistory;
    private String allergies;
    private String address;
    private String city;
    private String state;
    private String zipCode;
}

