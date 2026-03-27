package com.nexushelth.services;

import com.nexushelth.dto.PatientDTO;
import com.nexushelth.dto.UserDTO;
import com.nexushelth.entities.Patient;
import com.nexushelth.entities.User;
import com.nexushelth.repositories.PatientRepository;
import com.nexushelth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService {
    
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<PatientDTO> getPatientById(Long id) {
        return patientRepository.findById(id).map(this::convertToDTO);
    }
    
    public Optional<PatientDTO> getPatientByUserId(Long userId) {
        return patientRepository.findByUserId(userId).map(this::convertToDTO);
    }
    
    public PatientDTO createPatient(Long userId, PatientDTO patientDTO) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        Patient patient = new Patient();
        patient.setUser(user.get());
        patient.setDateOfBirth(patientDTO.getDateOfBirth());
        patient.setBloodType(patientDTO.getBloodType());
        patient.setMedicalHistory(patientDTO.getMedicalHistory());
        patient.setAllergies(patientDTO.getAllergies());
        patient.setAddress(patientDTO.getAddress());
        patient.setCity(patientDTO.getCity());
        patient.setState(patientDTO.getState());
        patient.setZipCode(patientDTO.getZipCode());
        
        Patient savedPatient = patientRepository.save(patient);
        return convertToDTO(savedPatient);
    }
    
    public PatientDTO updatePatient(Long id, PatientDTO patientDTO) {
        Optional<Patient> patient = patientRepository.findById(id);
        if (patient.isEmpty()) {
            throw new RuntimeException("Patient not found");
        }
        
        Patient existingPatient = patient.get();
        if (patientDTO.getDateOfBirth() != null) {
            existingPatient.setDateOfBirth(patientDTO.getDateOfBirth());
        }
        if (patientDTO.getBloodType() != null) {
            existingPatient.setBloodType(patientDTO.getBloodType());
        }
        if (patientDTO.getMedicalHistory() != null) {
            existingPatient.setMedicalHistory(patientDTO.getMedicalHistory());
        }
        if (patientDTO.getAllergies() != null) {
            existingPatient.setAllergies(patientDTO.getAllergies());
        }
        if (patientDTO.getAddress() != null) {
            existingPatient.setAddress(patientDTO.getAddress());
        }
        if (patientDTO.getCity() != null) {
            existingPatient.setCity(patientDTO.getCity());
        }
        if (patientDTO.getState() != null) {
            existingPatient.setState(patientDTO.getState());
        }
        if (patientDTO.getZipCode() != null) {
            existingPatient.setZipCode(patientDTO.getZipCode());
        }
        
        Patient updatedPatient = patientRepository.save(existingPatient);
        return convertToDTO(updatedPatient);
    }
    
    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }
    
    private PatientDTO convertToDTO(Patient patient) {
        PatientDTO dto = new PatientDTO();
        dto.setId(patient.getId());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setBloodType(patient.getBloodType());
        dto.setMedicalHistory(patient.getMedicalHistory());
        dto.setAllergies(patient.getAllergies());
        dto.setAddress(patient.getAddress());
        dto.setCity(patient.getCity());
        dto.setState(patient.getState());
        dto.setZipCode(patient.getZipCode());
        
        if (patient.getUser() != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(patient.getUser().getId());
            userDTO.setEmail(patient.getUser().getEmail());
            userDTO.setFirstName(patient.getUser().getFirstName());
            userDTO.setLastName(patient.getUser().getLastName());
            userDTO.setPhone(patient.getUser().getPhone());
            userDTO.setRole(patient.getUser().getRole());
            userDTO.setIsActive(patient.getUser().getIsActive());
            dto.setUser(userDTO);
        }
        
        return dto;
    }
}

