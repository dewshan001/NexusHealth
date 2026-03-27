package com.nexushelth.services;

import com.nexushelth.dto.DoctorDTO;
import com.nexushelth.dto.UserDTO;
import com.nexushelth.entities.Doctor;
import com.nexushelth.entities.User;
import com.nexushelth.repositories.DoctorRepository;
import com.nexushelth.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorService {
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public List<DoctorDTO> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<DoctorDTO> getDoctorById(Long id) {
        return doctorRepository.findById(id).map(this::convertToDTO);
    }
    
    public Optional<DoctorDTO> getDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId).map(this::convertToDTO);
    }
    
    public List<DoctorDTO> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.searchBySpecialization(specialization).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<DoctorDTO> getAvailableDoctors() {
        return doctorRepository.findByIsAvailableTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public DoctorDTO createDoctor(Long userId, DoctorDTO doctorDTO) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        Doctor doctor = new Doctor();
        doctor.setUser(user.get());
        doctor.setSpecialization(doctorDTO.getSpecialization());
        doctor.setLicenseNumber(doctorDTO.getLicenseNumber());
        doctor.setBio(doctorDTO.getBio());
        doctor.setConsultationFee(doctorDTO.getConsultationFee());
        doctor.setAvailableHours(doctorDTO.getAvailableHours());
        doctor.setIsAvailable(doctorDTO.getIsAvailable() != null ? doctorDTO.getIsAvailable() : true);
        
        Doctor savedDoctor = doctorRepository.save(doctor);
        return convertToDTO(savedDoctor);
    }
    
    public DoctorDTO updateDoctor(Long id, DoctorDTO doctorDTO) {
        Optional<Doctor> doctor = doctorRepository.findById(id);
        if (doctor.isEmpty()) {
            throw new RuntimeException("Doctor not found");
        }
        
        Doctor existingDoctor = doctor.get();
        if (doctorDTO.getSpecialization() != null) {
            existingDoctor.setSpecialization(doctorDTO.getSpecialization());
        }
        if (doctorDTO.getLicenseNumber() != null) {
            existingDoctor.setLicenseNumber(doctorDTO.getLicenseNumber());
        }
        if (doctorDTO.getBio() != null) {
            existingDoctor.setBio(doctorDTO.getBio());
        }
        if (doctorDTO.getConsultationFee() != null) {
            existingDoctor.setConsultationFee(doctorDTO.getConsultationFee());
        }
        if (doctorDTO.getAvailableHours() != null) {
            existingDoctor.setAvailableHours(doctorDTO.getAvailableHours());
        }
        if (doctorDTO.getIsAvailable() != null) {
            existingDoctor.setIsAvailable(doctorDTO.getIsAvailable());
        }
        
        Doctor updatedDoctor = doctorRepository.save(existingDoctor);
        return convertToDTO(updatedDoctor);
    }
    
    public void deleteDoctor(Long id) {
        doctorRepository.deleteById(id);
    }
    
    private DoctorDTO convertToDTO(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctor.getId());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setLicenseNumber(doctor.getLicenseNumber());
        dto.setBio(doctor.getBio());
        dto.setConsultationFee(doctor.getConsultationFee());
        dto.setAvailableHours(doctor.getAvailableHours());
        dto.setIsAvailable(doctor.getIsAvailable());
        
        if (doctor.getUser() != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(doctor.getUser().getId());
            userDTO.setEmail(doctor.getUser().getEmail());
            userDTO.setFirstName(doctor.getUser().getFirstName());
            userDTO.setLastName(doctor.getUser().getLastName());
            userDTO.setPhone(doctor.getUser().getPhone());
            userDTO.setRole(doctor.getUser().getRole());
            userDTO.setIsActive(doctor.getUser().getIsActive());
            dto.setUser(userDTO);
        }
        
        return dto;
    }
}

