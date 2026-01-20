package com.telemedicine.service;

import com.telemedicine.dto.ConsultationRequest;
import com.telemedicine.dto.ConsultationResponse;
import com.telemedicine.entity.*;
import com.telemedicine.exception.BadRequestException;
import com.telemedicine.exception.ResourceNotFoundException;
import com.telemedicine.exception.UnauthorizedException;
import com.telemedicine.repository.AppointmentRepository;
import com.telemedicine.repository.ConsultationRepository;
import com.telemedicine.repository.DoctorRepository;
import com.telemedicine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    @Transactional
    public ConsultationResponse createConsultation(Long userId, Long appointmentId, ConsultationRequest request) {
        log.info("Creating consultation for appointment ID: {} by user ID: {}", appointmentId, userId);

        // Verify user is a doctor
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (user.getRole() != UserRole.DOCTOR) {
            throw new UnauthorizedException("Only doctors can create consultations");
        }

        // Get appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        // Verify doctor owns this appointment
        if (!appointment.getDoctor().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to create consultation for this appointment");
        }

        // Check if appointment is completed or in progress
        if (appointment.getStatus() != AppointmentStatus.COMPLETED && 
            appointment.getStatus() != AppointmentStatus.IN_PROGRESS) {
            throw new BadRequestException("Consultation can only be created for completed or in-progress appointments");
        }

        // Check if consultation already exists
        if (consultationRepository.findByAppointmentId(appointmentId).isPresent()) {
            throw new BadRequestException("Consultation already exists for this appointment");
        }

        // Create consultation
        Consultation consultation = new Consultation();
        consultation.setAppointment(appointment);
        consultation.setDiagnosis(request.getDiagnosis());
        consultation.setTreatment(request.getTreatment());
        consultation.setRecommendations(request.getRecommendations());
        consultation.setFollowUpInstructions(request.getFollowUpInstructions());
        consultation.setFollowUpRequired(request.getFollowUpRequired());
        consultation.setDoctorNotes(request.getDoctorNotes());
        consultation.setVitalSigns(request.getVitalSigns());

        consultation = consultationRepository.save(consultation);

        // Update appointment status to completed
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        log.info("Consultation created successfully with ID: {}", consultation.getId());
        return mapToResponse(consultation);
    }

    @Transactional(readOnly = true)
    public ConsultationResponse getConsultationByAppointmentId(Long userId, Long appointmentId) {
        log.info("Fetching consultation for appointment ID: {} by user ID: {}", appointmentId, userId);

        // Get appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        // Verify user has access
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        boolean hasAccess = false;
        if (user.getRole() == UserRole.PATIENT && appointment.getPatient().getUser().getId().equals(userId)) {
            hasAccess = true;
        } else if (user.getRole() == UserRole.DOCTOR && appointment.getDoctor().getUser().getId().equals(userId)) {
            hasAccess = true;
        } else if (user.getRole() == UserRole.ADMIN) {
            hasAccess = true;
        }

        if (!hasAccess) {
            throw new UnauthorizedException("You don't have permission to view this consultation");
        }

        // Get consultation
        Consultation consultation = consultationRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found for appointment ID: " + appointmentId));

        return mapToResponse(consultation);
    }

    @Transactional(readOnly = true)
    public ConsultationResponse getConsultationById(Long userId, Long consultationId) {
        log.info("Fetching consultation ID: {} by user ID: {}", consultationId, userId);

        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with ID: " + consultationId));

        // Verify user has access
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        boolean hasAccess = false;
        if (user.getRole() == UserRole.PATIENT && 
            consultation.getAppointment().getPatient().getUser().getId().equals(userId)) {
            hasAccess = true;
        } else if (user.getRole() == UserRole.DOCTOR && 
                   consultation.getAppointment().getDoctor().getUser().getId().equals(userId)) {
            hasAccess = true;
        } else if (user.getRole() == UserRole.ADMIN) {
            hasAccess = true;
        }

        if (!hasAccess) {
            throw new UnauthorizedException("You don't have permission to view this consultation");
        }

        return mapToResponse(consultation);
    }

    @Transactional
    public ConsultationResponse updateConsultation(Long userId, Long consultationId, ConsultationRequest request) {
        log.info("Updating consultation ID: {} by user ID: {}", consultationId, userId);

        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with ID: " + consultationId));

        // Verify user is the doctor who created the consultation
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (user.getRole() != UserRole.DOCTOR) {
            throw new UnauthorizedException("Only doctors can update consultations");
        }

        if (!consultation.getAppointment().getDoctor().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to update this consultation");
        }

        // Update fields
        consultation.setDiagnosis(request.getDiagnosis());
        consultation.setTreatment(request.getTreatment());
        consultation.setRecommendations(request.getRecommendations());
        consultation.setFollowUpInstructions(request.getFollowUpInstructions());
        consultation.setFollowUpRequired(request.getFollowUpRequired());
        consultation.setDoctorNotes(request.getDoctorNotes());
        consultation.setVitalSigns(request.getVitalSigns());

        consultation = consultationRepository.save(consultation);
        log.info("Consultation updated successfully: {}", consultationId);

        return mapToResponse(consultation);
    }

    private ConsultationResponse mapToResponse(Consultation consultation) {
        ConsultationResponse response = new ConsultationResponse();
        response.setId(consultation.getId());
        response.setAppointmentId(consultation.getAppointment().getId());
        response.setDiagnosis(consultation.getDiagnosis());
        response.setTreatment(consultation.getTreatment());
        response.setRecommendations(consultation.getRecommendations());
        response.setFollowUpInstructions(consultation.getFollowUpInstructions());
        response.setFollowUpRequired(consultation.getFollowUpRequired());
        response.setDoctorNotes(consultation.getDoctorNotes());
        response.setVitalSigns(consultation.getVitalSigns());
        response.setCreatedAt(consultation.getCreatedAt());
        response.setUpdatedAt(consultation.getUpdatedAt());
        return response;
    }
}
