package com.nexushelth.controllers;

import com.nexushelth.dto.MedicineDTO;
import com.nexushelth.services.MedicineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/medicines")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MedicineController {
    
    @Autowired
    private MedicineService medicineService;
    
    @GetMapping
    public ResponseEntity<List<MedicineDTO>> getAllMedicines() {
        List<MedicineDTO> medicines = medicineService.getAllMedicines();
        return ResponseEntity.ok(medicines);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MedicineDTO> getMedicineById(@PathVariable Long id) {
        Optional<MedicineDTO> medicine = medicineService.getMedicineById(id);
        if (medicine.isPresent()) {
            return ResponseEntity.ok(medicine.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    
    @GetMapping("/available/list")
    public ResponseEntity<List<MedicineDTO>> getAvailableMedicines() {
        List<MedicineDTO> medicines = medicineService.getAvailableMedicines();
        return ResponseEntity.ok(medicines);
    }
    
    @GetMapping("/search/{name}")
    public ResponseEntity<MedicineDTO> getMedicineByName(@PathVariable String name) {
        Optional<MedicineDTO> medicine = medicineService.getMedicineByName(name);
        if (medicine.isPresent()) {
            return ResponseEntity.ok(medicine.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    
    @GetMapping("/manufacturer/{manufacturer}")
    public ResponseEntity<List<MedicineDTO>> getMedicinesByManufacturer(@PathVariable String manufacturer) {
        List<MedicineDTO> medicines = medicineService.getMedicinesByManufacturer(manufacturer);
        return ResponseEntity.ok(medicines);
    }
    
    @PostMapping
    public ResponseEntity<MedicineDTO> createMedicine(@RequestBody MedicineDTO medicineDTO) {
        try {
            MedicineDTO createdMedicine = medicineService.createMedicine(medicineDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMedicine);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<MedicineDTO> updateMedicine(@PathVariable Long id, @RequestBody MedicineDTO medicineDTO) {
        try {
            MedicineDTO updatedMedicine = medicineService.updateMedicine(id, medicineDTO);
            return ResponseEntity.ok(updatedMedicine);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicine(@PathVariable Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.noContent().build();
    }
}

