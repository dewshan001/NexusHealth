package com.nexushelth.services;

import com.nexushelth.dto.MedicineDTO;
import com.nexushelth.entities.Medicine;
import com.nexushelth.repositories.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MedicineService {
    
    @Autowired
    private MedicineRepository medicineRepository;
    
    public List<MedicineDTO> getAllMedicines() {
        return medicineRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<MedicineDTO> getMedicineById(Long id) {
        return medicineRepository.findById(id).map(this::convertToDTO);
    }
    
    public List<MedicineDTO> getAvailableMedicines() {
        return medicineRepository.findByIsAvailableTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<MedicineDTO> getMedicineByName(String name) {
        return medicineRepository.findByName(name).map(this::convertToDTO);
    }
    
    public List<MedicineDTO> getMedicinesByManufacturer(String manufacturer) {
        return medicineRepository.findByManufacturer(manufacturer).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public MedicineDTO createMedicine(MedicineDTO medicineDTO) {
        Medicine medicine = new Medicine();
        medicine.setName(medicineDTO.getName());
        medicine.setDescription(medicineDTO.getDescription());
        medicine.setPrice(medicineDTO.getPrice());
        medicine.setQuantity(medicineDTO.getQuantity());
        medicine.setManufacturer(medicineDTO.getManufacturer());
        medicine.setBatchNumber(medicineDTO.getBatchNumber());
        medicine.setIsAvailable(medicineDTO.getIsAvailable() != null ? medicineDTO.getIsAvailable() : true);
        
        Medicine savedMedicine = medicineRepository.save(medicine);
        return convertToDTO(savedMedicine);
    }
    
    public MedicineDTO updateMedicine(Long id, MedicineDTO medicineDTO) {
        Optional<Medicine> medicine = medicineRepository.findById(id);
        if (medicine.isEmpty()) {
            throw new RuntimeException("Medicine not found");
        }
        
        Medicine existingMedicine = medicine.get();
        if (medicineDTO.getName() != null) {
            existingMedicine.setName(medicineDTO.getName());
        }
        if (medicineDTO.getDescription() != null) {
            existingMedicine.setDescription(medicineDTO.getDescription());
        }
        if (medicineDTO.getPrice() != null) {
            existingMedicine.setPrice(medicineDTO.getPrice());
        }
        if (medicineDTO.getQuantity() != null) {
            existingMedicine.setQuantity(medicineDTO.getQuantity());
        }
        if (medicineDTO.getManufacturer() != null) {
            existingMedicine.setManufacturer(medicineDTO.getManufacturer());
        }
        if (medicineDTO.getBatchNumber() != null) {
            existingMedicine.setBatchNumber(medicineDTO.getBatchNumber());
        }
        if (medicineDTO.getIsAvailable() != null) {
            existingMedicine.setIsAvailable(medicineDTO.getIsAvailable());
        }
        
        Medicine updatedMedicine = medicineRepository.save(existingMedicine);
        return convertToDTO(updatedMedicine);
    }
    
    public void deleteMedicine(Long id) {
        medicineRepository.deleteById(id);
    }
    
    private MedicineDTO convertToDTO(Medicine medicine) {
        MedicineDTO dto = new MedicineDTO();
        dto.setId(medicine.getId());
        dto.setName(medicine.getName());
        dto.setDescription(medicine.getDescription());
        dto.setPrice(medicine.getPrice());
        dto.setQuantity(medicine.getQuantity());
        dto.setManufacturer(medicine.getManufacturer());
        dto.setBatchNumber(medicine.getBatchNumber());
        dto.setIsAvailable(medicine.getIsAvailable());
        return dto;
    }
}

