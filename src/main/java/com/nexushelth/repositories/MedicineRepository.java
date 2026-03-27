package com.nexushelth.repositories;

import com.nexushelth.entities.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    Optional<Medicine> findByName(String name);
    List<Medicine> findByIsAvailableTrue();
    List<Medicine> findByManufacturer(String manufacturer);
}

