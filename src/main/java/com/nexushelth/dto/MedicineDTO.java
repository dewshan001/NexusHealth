package com.nexushelth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer quantity;
    private String manufacturer;
    private String batchNumber;
    private Boolean isAvailable;
}

