package com.nexushelth.repositories;

import com.nexushelth.entities.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    List<Doctor> findBySpecialization(String specialization);
    List<Doctor> findByIsAvailableTrue();
    @Query("SELECT d FROM Doctor d WHERE d.specialization ILIKE %:specialization% AND d.isAvailable = true")
    List<Doctor> searchBySpecialization(@Param("specialization") String specialization);
}

