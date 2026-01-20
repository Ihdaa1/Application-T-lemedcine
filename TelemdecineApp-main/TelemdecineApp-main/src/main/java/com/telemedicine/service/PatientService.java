package com.telemedicine.service;

import com.telemedicine.dto.AppointmentResponse;
import com.telemedicine.dto.MedicalRecordResponse;
import com.telemedicine.dto.PatientProfileRequest;
import com.telemedicine.dto.PatientResponse;
import com.telemedicine.dto.PrescriptionResponse;
import com.telemedicine.entity.*;
import com.telemedicine.exception.ResourceNotFoundException;
import com.telemedicine.exception.UnauthorizedException;
import com.telemedicine.repository.AppointmentRepository;
import com.telemedicine.repository.MedicalRecordRepository;
import com.telemedicine.repository.PatientRepository;
import com.telemedicine.repository.PrescriptionRepository;
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
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    @Transactional(readOnly = true)
    public PatientResponse getMyProfile(Long userId) {
        log.info("Fetching patient profile for user ID: {}", userId);
        
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + userId));
        
        return mapToResponse(patient);
    }

    @Transactional
    public PatientResponse updateMyProfile(Long userId, PatientProfileRequest request) {
        log.info("Updating patient profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        if (user.getRole() != UserRole.PATIENT) {
            throw new UnauthorizedException("Only patients can update patient profiles");
        }
        
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
        
        // Update profile
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setBloodType(request.getBloodType());
        patient.setAllergies(request.getAllergies());
        patient.setMedicalHistory(request.getMedicalHistory());
        patient.setEmergencyContact(request.getEmergencyContact());
        patient.setEmergencyPhone(request.getEmergencyPhone());
        patient.setAddress(request.getAddress());
        patient.setCity(request.getCity());
        patient.setCountry(request.getCountry());
        patient.setPostalCode(request.getPostalCode());
        
        patient = patientRepository.save(patient);
        log.info("Patient profile updated successfully for user ID: {}", userId);
        
        return mapToResponse(patient);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments(Long userId) {
        log.info("Fetching appointments for patient with user ID: {}", userId);
        
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + userId));
        
        List<Appointment> appointments = appointmentRepository.findByPatientIdOrderByAppointmentDateDesc(patient.getId());
        
        return appointments.stream()
                .map(this::mapAppointmentToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointmentsByStatus(Long userId, AppointmentStatus status) {
        log.info("Fetching appointments with status {} for patient with user ID: {}", status, userId);
        
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + userId));
        
        List<Appointment> appointments = appointmentRepository.findByPatientIdAndStatus(patient.getId(), status);
        
        return appointments.stream()
                .map(this::mapAppointmentToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getMyPrescriptions(Long userId) {
        log.info("Fetching prescriptions for patient with user ID: {}", userId);
        
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + userId));
        
        List<Prescription> prescriptions = prescriptionRepository.findByPatientIdOrderByStartDateDesc(patient.getId());
        
        return prescriptions.stream()
                .map(this::mapPrescriptionToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getMyActivePrescriptions(Long userId) {
        log.info("Fetching active prescriptions for patient with user ID: {}", userId);
        
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + userId));
        
        List<Prescription> prescriptions = prescriptionRepository.findByPatientIdAndIsActiveTrue(patient.getId());
        
        return prescriptions.stream()
                .map(this::mapPrescriptionToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getMyMedicalRecords(Long userId) {
        log.info("Fetching medical records for patient with user ID: {}", userId);
        
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + userId));
        
        List<MedicalRecord> records = medicalRecordRepository.findByPatientIdOrderByRecordDateDesc(patient.getId());
        
        return records.stream()
                .map(this::mapMedicalRecordToResponse)
                .collect(Collectors.toList());
    }

    private PatientResponse mapToResponse(Patient patient) {
        PatientResponse response = new PatientResponse();
        response.setId(patient.getId());
        response.setUserId(patient.getUser().getId());
        response.setEmail(patient.getUser().getEmail());
        response.setFirstName(patient.getUser().getFirstName());
        response.setLastName(patient.getUser().getLastName());
        response.setPhoneNumber(patient.getUser().getPhoneNumber());
        response.setDateOfBirth(patient.getDateOfBirth());
        response.setGender(patient.getGender());
        response.setBloodType(patient.getBloodType());
        response.setAllergies(patient.getAllergies());
        response.setMedicalHistory(patient.getMedicalHistory());
        response.setEmergencyContact(patient.getEmergencyContact());
        response.setEmergencyPhone(patient.getEmergencyPhone());
        response.setAddress(patient.getAddress());
        response.setCity(patient.getCity());
        response.setCountry(patient.getCountry());
        response.setPostalCode(patient.getPostalCode());
        response.setCreatedAt(patient.getCreatedAt());
        response.setUpdatedAt(patient.getUpdatedAt());
        return response;
    }

    private AppointmentResponse mapAppointmentToResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setPatientId(appointment.getPatient().getId());
        response.setPatientName(appointment.getPatient().getUser().getFirstName() + " " + 
                               appointment.getPatient().getUser().getLastName());
        response.setDoctorId(appointment.getDoctor().getId());
        response.setDoctorName(appointment.getDoctor().getUser().getFirstName() + " " + 
                              appointment.getDoctor().getUser().getLastName());
        response.setDoctorSpecialization(appointment.getDoctor().getSpecialization());
        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setType(appointment.getType());
        response.setStatus(appointment.getStatus());
        response.setReason(appointment.getReason());
        response.setSymptoms(appointment.getSymptoms());
        response.setDurationMinutes(appointment.getDurationMinutes());
        response.setCreatedAt(appointment.getCreatedAt());
        return response;
    }

    private PrescriptionResponse mapPrescriptionToResponse(Prescription prescription) {
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

    private MedicalRecordResponse mapMedicalRecordToResponse(MedicalRecord record) {
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
