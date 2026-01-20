package com.telemedicine.repository;

import com.telemedicine.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    Optional<Doctor> findByLicenseNumber(String licenseNumber);
    List<Doctor> findBySpecialization(String specialization);
    List<Doctor> findByAvailableForConsultationTrue();
    
    void deleteByUserId(Long userId);
}
