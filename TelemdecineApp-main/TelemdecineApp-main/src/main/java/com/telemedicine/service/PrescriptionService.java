package com.telemedicine.service;

import com.telemedicine.dto.PrescriptionRequest;
import com.telemedicine.dto.PrescriptionResponse;
import com.telemedicine.entity.*;
import com.telemedicine.exception.ResourceNotFoundException;
import com.telemedicine.exception.UnauthorizedException;
import com.telemedicine.repository.DoctorRepository;
import com.telemedicine.repository.PatientRepository;
import com.telemedicine.repository.PrescriptionRepository;
import com.telemedicine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    @Transactional
    public PrescriptionResponse createPrescription(Long userId, PrescriptionRequest request) {
        log.info("Creating prescription for patient ID: {} by user ID: {}", request.getPatientId(), userId);

        // Verify user is a doctor
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (user.getRole() != UserRole.DOCTOR) {
            throw new UnauthorizedException("Only doctors can create prescriptions");
        }

        // Get doctor
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        // Get patient
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + request.getPatientId()));

        // Create prescription
        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setDoctor(doctor);
        prescription.setMedicationName(request.getMedicationName());
        prescription.setDosage(request.getDosage());
        prescription.setFrequency(request.getFrequency());
        prescription.setDuration(request.getDuration());
        prescription.setInstructions(request.getInstructions());
        prescription.setStartDate(request.getStartDate());
        prescription.setEndDate(request.getStartDate().plusDays(request.getDuration()));
        prescription.setIsActive(true);
        prescription.setNotes(request.getNotes());

        prescription = prescriptionRepository.save(prescription);
        log.info("Prescription created successfully with ID: {}", prescription.getId());

        return mapToResponse(prescription);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPatientPrescriptions(Long userId, Long patientId) {
        log.info("Fetching prescriptions for patient ID: {} by user ID: {}", patientId, userId);

        // Verify access
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (user.getRole() == UserRole.PATIENT) {
            Patient patient = patientRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
            
            if (!patient.getId().equals(patientId)) {
                throw new UnauthorizedException("You can only view your own prescriptions");
            }
        }

        List<Prescription> prescriptions = prescriptionRepository.findByPatientId(patientId);
        return prescriptions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getDoctorPrescriptions(Long userId) {
        log.info("Fetching prescriptions for doctor user ID: {}", userId);

        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        List<Prescription> prescriptions = prescriptionRepository.findByDoctorId(doctor.getId());
        return prescriptions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getActivePrescriptions(Long userId, Long patientId) {
        log.info("Fetching active prescriptions for patient ID: {}", patientId);

        // Verify access
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (user.getRole() == UserRole.PATIENT) {
            Patient patient = patientRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
            
            if (!patient.getId().equals(patientId)) {
                throw new UnauthorizedException("You can only view your own prescriptions");
            }
        }

        List<Prescription> prescriptions = prescriptionRepository.findByPatientIdAndIsActiveTrue(patientId);
        return prescriptions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionById(Long userId, Long prescriptionId) {
        log.info("Fetching prescription ID: {} by user ID: {}", prescriptionId, userId);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + prescriptionId));

        // Verify access
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        boolean hasAccess = false;
        if (user.getRole() == UserRole.PATIENT && 
            prescription.getPatient().getUser().getId().equals(userId)) {
            hasAccess = true;
        } else if (user.getRole() == UserRole.DOCTOR && 
                   prescription.getDoctor().getUser().getId().equals(userId)) {
            hasAccess = true;
        } else if (user.getRole() == UserRole.ADMIN) {
            hasAccess = true;
        }

        if (!hasAccess) {
            throw new UnauthorizedException("You don't have permission to view this prescription");
        }

        return mapToResponse(prescription);
    }

    @Transactional
    public PrescriptionResponse deactivatePrescription(Long userId, Long prescriptionId) {
        log.info("Deactivating prescription ID: {} by user ID: {}", prescriptionId, userId);

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + prescriptionId));

        // Verify user is the doctor who created it
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (user.getRole() != UserRole.DOCTOR || 
            !prescription.getDoctor().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Only the prescribing doctor can deactivate this prescription");
        }

        prescription.setIsActive(false);
        prescription = prescriptionRepository.save(prescription);
        log.info("Prescription deactivated successfully: {}", prescriptionId);

        return mapToResponse(prescription);
    }

    private PrescriptionResponse mapToResponse(Prescription prescription) {
        PrescriptionResponse response = new PrescriptionResponse();
        response.setId(prescription.getId());
        response.setPatientId(prescription.getPatient().getId());
        response.setPatientName(prescription.getPatient().getUser().getFirstName() + " " + 
                               prescription.getPatient().getUser().getLastName());
        response.setDoctorId(prescription.getDoctor().getId());
        response.setDoctorName(prescription.getDoctor().getUser().getFirstName() + " " + 
                              prescription.getDoctor().getUser().getLastName());
        response.setMedicationName(prescription.getMedicationName());
        response.setDosage(prescription.getDosage());
        response.setFrequency(prescription.getFrequency());
        response.setDuration(prescription.getDuration());
        response.setInstructions(prescription.getInstructions());
        response.setStartDate(prescription.getStartDate());
        response.setEndDate(prescription.getEndDate());
        response.setIsActive(prescription.getIsActive());
        response.setNotes(prescription.getNotes());
        response.setCreatedAt(prescription.getCreatedAt());
        return response;
    }
}
