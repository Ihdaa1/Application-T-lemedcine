package com.telemedicine.service;

import com.telemedicine.dto.MedicalRecordRequest;
import com.telemedicine.dto.MedicalRecordResponse;
import com.telemedicine.entity.*;
import com.telemedicine.exception.ResourceNotFoundException;
import com.telemedicine.exception.UnauthorizedException;
import com.telemedicine.repository.MedicalRecordRepository;
import com.telemedicine.repository.PatientRepository;
import com.telemedicine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    @Transactional
    public MedicalRecordResponse createMedicalRecord(Long userId, MedicalRecordRequest request) {
        log.info("Creating medical record for patient ID: {} by user ID: {}", request.getPatientId(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Verify patient exists
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + request.getPatientId()));

        // Verify access - only doctors, admins, or the patient themselves can create records
        boolean canCreate = false;
        if (user.getRole() == UserRole.DOCTOR || user.getRole() == UserRole.ADMIN) {
            canCreate = true;
        } else if (user.getRole() == UserRole.PATIENT && patient.getUser().getId().equals(userId)) {
            canCreate = true;
        }

        if (!canCreate) {
            throw new UnauthorizedException("You don't have permission to create medical records for this patient");
        }

        // Create medical record
        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setTitle(request.getTitle());
        record.setDescription(request.getDescription());
        record.setRecordDate(request.getRecordDate());
        record.setRecordType(request.getRecordType());
        record.setFileUrl(request.getFileUrl());
        record.setFileName(request.getFileName());
        record.setUploadedBy(user.getFirstName() + " " + user.getLastName() + " (" + user.getRole() + ")");

        record = medicalRecordRepository.save(record);
        log.info("Medical record created successfully with ID: {}", record.getId());

        return mapToResponse(record);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getPatientMedicalRecords(Long userId, Long patientId) {
        log.info("Fetching medical records for patient ID: {} by user ID: {}", patientId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));

        // Verify access
        boolean hasAccess = false;
        if (user.getRole() == UserRole.PATIENT && patient.getUser().getId().equals(userId)) {
            hasAccess = true;
        } else if (user.getRole() == UserRole.DOCTOR || user.getRole() == UserRole.ADMIN) {
            hasAccess = true;
        }

        if (!hasAccess) {
            throw new UnauthorizedException("You don't have permission to view these medical records");
        }

        List<MedicalRecord> records = medicalRecordRepository.findByPatientIdOrderByRecordDateDesc(patientId);
        return records.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse getMedicalRecordById(Long userId, Long recordId) {
        log.info("Fetching medical record ID: {} by user ID: {}", recordId, userId);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with ID: " + recordId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Verify access
        boolean hasAccess = false;
        if (user.getRole() == UserRole.PATIENT && record.getPatient().getUser().getId().equals(userId)) {
            hasAccess = true;
        } else if (user.getRole() == UserRole.DOCTOR || user.getRole() == UserRole.ADMIN) {
            hasAccess = true;
        }

        if (!hasAccess) {
            throw new UnauthorizedException("You don't have permission to view this medical record");
        }

        return mapToResponse(record);
    }

    @Transactional
    public MedicalRecordResponse updateMedicalRecord(Long userId, Long recordId, MedicalRecordRequest request) {
        log.info("Updating medical record ID: {} by user ID: {}", recordId, userId);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with ID: " + recordId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Only doctors and admins can update records
        if (user.getRole() != UserRole.DOCTOR && user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Only doctors and admins can update medical records");
        }

        // Update fields
        record.setTitle(request.getTitle());
        record.setDescription(request.getDescription());
        record.setRecordDate(request.getRecordDate());
        record.setRecordType(request.getRecordType());
        record.setFileUrl(request.getFileUrl());
        record.setFileName(request.getFileName());

        record = medicalRecordRepository.save(record);
        log.info("Medical record updated successfully: {}", recordId);

        return mapToResponse(record);
    }

    @Transactional
    public void deleteMedicalRecord(Long userId, Long recordId) {
        log.info("Deleting medical record ID: {} by user ID: {}", recordId, userId);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with ID: " + recordId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Only admins can delete records
        if (user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Only admins can delete medical records");
        }

        medicalRecordRepository.delete(record);
        log.info("Medical record deleted successfully: {}", recordId);
    }

    private MedicalRecordResponse mapToResponse(MedicalRecord record) {
        MedicalRecordResponse response = new MedicalRecordResponse();
        response.setId(record.getId());
        response.setPatientId(record.getPatient().getId());
        response.setPatientName(record.getPatient().getUser().getFirstName() + " " + 
                               record.getPatient().getUser().getLastName());
        response.setTitle(record.getTitle());
        response.setDescription(record.getDescription());
        response.setRecordDate(record.getRecordDate());
        response.setRecordType(record.getRecordType());
        response.setFileUrl(record.getFileUrl());
        response.setFileName(record.getFileName());
        response.setUploadedBy(record.getUploadedBy());
        response.setCreatedAt(record.getCreatedAt());
        response.setUpdatedAt(record.getUpdatedAt());
        return response;
    }
}
