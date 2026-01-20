package com.telemedicine.service;

import com.telemedicine.dto.AppointmentRequest;
import com.telemedicine.dto.AppointmentResponse;
import com.telemedicine.dto.AppointmentUpdateRequest;
import com.telemedicine.entity.*;
import com.telemedicine.exception.BadRequestException;
import com.telemedicine.exception.ResourceNotFoundException;
import com.telemedicine.exception.UnauthorizedException;
import com.telemedicine.repository.AppointmentRepository;
import com.telemedicine.repository.DoctorRepository;
import com.telemedicine.repository.PatientRepository;
import com.telemedicine.repository.UserRepository;
import com.telemedicine.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public AppointmentResponse createAppointment(Long userId, AppointmentRequest request) {
        log.info("Creating appointment for user ID: {} with doctor ID: {}", userId, request.getDoctorId());

        // Get patient by user ID
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + userId));

        // Get doctor
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + request.getDoctorId()));

        // Validate appointment date is in the future
        if (request.getAppointmentDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Appointment date must be in the future");
        }

        // Check if doctor is available
        if (!doctor.getAvailableForConsultation()) {
            throw new BadRequestException("Doctor is not available for consultation");
        }

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setType(request.getType());
        appointment.setReason(request.getReason());
        appointment.setSymptoms(request.getSymptoms());
        appointment.setDurationMinutes(request.getDurationMinutes());
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        appointment = appointmentRepository.save(appointment);
        log.info("Appointment created successfully with ID: {}", appointment.getId());

        return mapToResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientAppointments(Long userId) {
        log.info("Fetching appointments for patient user ID: {}", userId);
        
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user ID: " + userId));

        List<Appointment> appointments = appointmentRepository.findByPatientId(patient.getId());
        return appointments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointments(Long userId) {
        log.info("Fetching appointments for doctor user ID: {}", userId);
        
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user ID: " + userId));

        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctor.getId());
        return appointments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long userId, Long appointmentId) {
        log.info("Fetching appointment ID: {} for user ID: {}", appointmentId, userId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        // Verify user has access to this appointment
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (user.getRole() == UserRole.PATIENT) {
            if (!appointment.getPatient().getUser().getId().equals(userId)) {
                throw new UnauthorizedException("You don't have permission to view this appointment");
            }
        } else if (user.getRole() == UserRole.DOCTOR) {
            if (!appointment.getDoctor().getUser().getId().equals(userId)) {
                throw new UnauthorizedException("You don't have permission to view this appointment");
            }
        }

        return mapToResponse(appointment);
    }

    @Transactional
    public AppointmentResponse updateAppointment(Long userId, Long appointmentId, AppointmentUpdateRequest request) {
        log.info("Updating appointment ID: {} by user ID: {}", appointmentId, userId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        // Verify user has access to update this appointment
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        boolean canUpdate = false;
        if (user.getRole() == UserRole.PATIENT && appointment.getPatient().getUser().getId().equals(userId)) {
            canUpdate = true;
        } else if (user.getRole() == UserRole.DOCTOR && appointment.getDoctor().getUser().getId().equals(userId)) {
            canUpdate = true;
        } else if (user.getRole() == UserRole.ADMIN) {
            canUpdate = true;
        }

        if (!canUpdate) {
            throw new UnauthorizedException("You don't have permission to update this appointment");
        }

        // Update fields if provided
        if (request.getAppointmentDate() != null) {
            if (request.getAppointmentDate().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Appointment date must be in the future");
            }
            appointment.setAppointmentDate(request.getAppointmentDate());
        }

        if (request.getStatus() != null) {
            AppointmentStatus previousStatus = appointment.getStatus();
            appointment.setStatus(request.getStatus());
            
            // Send confirmation email if appointment is confirmed by doctor
            if (request.getStatus() == AppointmentStatus.CONFIRMED && 
                previousStatus != AppointmentStatus.CONFIRMED) {
                sendAppointmentConfirmationEmail(appointment);
            }
        }

        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }

        if (request.getMeetingLink() != null) {
            appointment.setMeetingLink(request.getMeetingLink());
        }

        appointment = appointmentRepository.save(appointment);
        log.info("Appointment updated successfully: {}", appointmentId);

        return mapToResponse(appointment);
    }

    @Transactional
    public void cancelAppointment(Long userId, Long appointmentId) {
        log.info("Cancelling appointment ID: {} by user ID: {}", appointmentId, userId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        // Verify user has access to cancel this appointment
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        boolean canCancel = false;
        if (user.getRole() == UserRole.PATIENT && appointment.getPatient().getUser().getId().equals(userId)) {
            canCancel = true;
        } else if (user.getRole() == UserRole.DOCTOR && appointment.getDoctor().getUser().getId().equals(userId)) {
            canCancel = true;
        } else if (user.getRole() == UserRole.ADMIN) {
            canCancel = true;
        }

        if (!canCancel) {
            throw new UnauthorizedException("You don't have permission to cancel this appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        log.info("Appointment cancelled successfully: {}", appointmentId);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByStatus(Long userId, AppointmentStatus status) {
        log.info("Fetching appointments by status: {} for user ID: {}", status, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        List<Appointment> appointments;
        if (user.getRole() == UserRole.PATIENT) {
            Patient patient = patientRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
            appointments = appointmentRepository.findByPatientIdAndStatus(patient.getId(), status);
        } else if (user.getRole() == UserRole.DOCTOR) {
            Doctor doctor = doctorRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
            appointments = appointmentRepository.findByDoctorIdAndStatus(doctor.getId(), status);
        } else {
            appointments = appointmentRepository.findByStatus(status);
        }

        return appointments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
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
        response.setNotes(appointment.getNotes());
        response.setMeetingLink(appointment.getMeetingLink());
        response.setDurationMinutes(appointment.getDurationMinutes());
        response.setCreatedAt(appointment.getCreatedAt());
        return response;
    }
    
    private void sendAppointmentConfirmationEmail(Appointment appointment) {
        try {
            String patientEmail = appointment.getPatient().getUser().getEmail();
            String patientName = appointment.getPatient().getUser().getFirstName() + " " + 
                                 appointment.getPatient().getUser().getLastName();
            String doctorName = appointment.getDoctor().getUser().getFirstName() + " " + 
                               appointment.getDoctor().getUser().getLastName();
            String specialty = appointment.getDoctor().getSpecialization();
            String appointmentDate = appointment.getAppointmentDate()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy 'à' HH:mm"));
            
            emailService.sendAppointmentConfirmationEmail(
                patientEmail,
                patientName,
                doctorName,
                specialty,
                appointmentDate
            );
        } catch (Exception e) {
            log.error("Failed to send confirmation email for appointment ID: {}", appointment.getId(), e);
        }
    }
}
