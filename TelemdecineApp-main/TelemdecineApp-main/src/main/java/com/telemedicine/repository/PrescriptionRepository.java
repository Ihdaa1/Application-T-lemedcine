package com.telemedicine.repository;

import com.telemedicine.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatientId(Long patientId);
    List<Prescription> findByPatientIdOrderByStartDateDesc(Long patientId);
    List<Prescription> findByDoctorId(Long doctorId);
    List<Prescription> findByPatientIdAndIsActiveTrue(Long patientId);
    List<Prescription> findByDoctorIdAndIsActiveTrue(Long doctorId);
}
