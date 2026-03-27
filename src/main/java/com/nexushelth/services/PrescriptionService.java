package com.nexushelth.services;

import com.nexushelth.dto.PrescriptionDTO;
import com.nexushelth.entities.Doctor;
import com.nexushelth.entities.Patient;
import com.nexushelth.entities.Prescription;
import com.nexushelth.enums.PrescriptionStatus;
import com.nexushelth.repositories.DoctorRepository;
import com.nexushelth.repositories.PatientRepository;
import com.nexushelth.repositories.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {
    
    @Autowired
    private PrescriptionRepository prescriptionRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private PatientRepository patientRepository;
    
    public List<PrescriptionDTO> getAllPrescriptions() {
        return prescriptionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<PrescriptionDTO> getPrescriptionById(Long id) {
        return prescriptionRepository.findById(id).map(this::convertToDTO);
    }
    
    public List<PrescriptionDTO> getPrescriptionsByPatient(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<PrescriptionDTO> getPrescriptionsByDoctor(Long doctorId) {
        return prescriptionRepository.findByDoctorId(doctorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<PrescriptionDTO> getActivePrescriptions(Long patientId) {
        return prescriptionRepository.findByPatientIdAndStatus(patientId, PrescriptionStatus.ACTIVE).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public PrescriptionDTO createPrescription(PrescriptionDTO prescriptionDTO) {
        Optional<Doctor> doctor = doctorRepository.findById(prescriptionDTO.getDoctorId());
        Optional<Patient> patient = patientRepository.findById(prescriptionDTO.getPatientId());
        
        if (doctor.isEmpty() || patient.isEmpty()) {
            throw new RuntimeException("Doctor or Patient not found");
        }
        
        Prescription prescription = new Prescription();
        prescription.setDoctor(doctor.get());
        prescription.setPatient(patient.get());
        prescription.setMedicationName(prescriptionDTO.getMedicationName());
        prescription.setDosage(prescriptionDTO.getDosage());
        prescription.setFrequency(prescriptionDTO.getFrequency());
        prescription.setInstructions(prescriptionDTO.getInstructions());
        prescription.setIssuedDate(prescriptionDTO.getIssuedDate());
        prescription.setExpiryDate(prescriptionDTO.getExpiryDate());
        prescription.setStatus(prescriptionDTO.getStatus() != null ? prescriptionDTO.getStatus() : PrescriptionStatus.ACTIVE);
        
        Prescription savedPrescription = prescriptionRepository.save(prescription);
        return convertToDTO(savedPrescription);
    }
    
    public PrescriptionDTO updatePrescription(Long id, PrescriptionDTO prescriptionDTO) {
        Optional<Prescription> prescription = prescriptionRepository.findById(id);
        if (prescription.isEmpty()) {
            throw new RuntimeException("Prescription not found");
        }
        
        Prescription existingPrescription = prescription.get();
        if (prescriptionDTO.getMedicationName() != null) {
            existingPrescription.setMedicationName(prescriptionDTO.getMedicationName());
        }
        if (prescriptionDTO.getDosage() != null) {
            existingPrescription.setDosage(prescriptionDTO.getDosage());
        }
        if (prescriptionDTO.getFrequency() != null) {
            existingPrescription.setFrequency(prescriptionDTO.getFrequency());
        }
        if (prescriptionDTO.getInstructions() != null) {
            existingPrescription.setInstructions(prescriptionDTO.getInstructions());
        }
        if (prescriptionDTO.getStatus() != null) {
            existingPrescription.setStatus(prescriptionDTO.getStatus());
        }
        
        Prescription updatedPrescription = prescriptionRepository.save(existingPrescription);
        return convertToDTO(updatedPrescription);
    }
    
    public void deletePrescription(Long id) {
        prescriptionRepository.deleteById(id);
    }
    
    private PrescriptionDTO convertToDTO(Prescription prescription) {
        PrescriptionDTO dto = new PrescriptionDTO();
        dto.setId(prescription.getId());
        dto.setDoctorId(prescription.getDoctor().getId());
        dto.setPatientId(prescription.getPatient().getId());
        dto.setMedicationName(prescription.getMedicationName());
        dto.setDosage(prescription.getDosage());
        dto.setFrequency(prescription.getFrequency());
        dto.setInstructions(prescription.getInstructions());
        dto.setIssuedDate(prescription.getIssuedDate());
        dto.setExpiryDate(prescription.getExpiryDate());
        dto.setStatus(prescription.getStatus());
        return dto;
    }
}

