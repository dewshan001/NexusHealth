package com.nexushelth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.nexushelth.enums.PrescriptionStatus;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDTO {
    private Long id;
    private Long doctorId;
    private Long patientId;
    private String medicationName;
    private String dosage;
    private String frequency;
    private String instructions;
    private LocalDate issuedDate;
    private LocalDate expiryDate;
    private PrescriptionStatus status;
}

