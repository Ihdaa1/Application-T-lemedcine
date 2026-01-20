package com.telemedicine.service;

import com.telemedicine.dto.DoctorProfileRequest;
import com.telemedicine.dto.DoctorResponse;
import com.telemedicine.entity.Doctor;
import com.telemedicine.entity.User;
import com.telemedicine.entity.UserRole;
import com.telemedicine.exception.ResourceNotFoundException;
import com.telemedicine.exception.UnauthorizedException;
import com.telemedicine.repository.DoctorRepository;
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
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllDoctors() {
        log.info("Fetching all doctors");
        
        List<Doctor> doctors = doctorRepository.findAll();
        return doctors.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> getAvailableDoctors() {
        log.info("Fetching available doctors");
        
        List<Doctor> doctors = doctorRepository.findByAvailableForConsultationTrue();
        return doctors.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> getDoctorsBySpecialization(String specialization) {
        log.info("Fetching doctors by specialization: {}", specialization);
        
        List<Doctor> doctors = doctorRepository.findBySpecialization(specialization);
        return doctors.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long doctorId) {
        log.info("Fetching doctor with ID: {}", doctorId);
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));
        
        return mapToResponse(doctor);
    }

    @Transactional(readOnly = true)
    public DoctorResponse getMyProfile(Long userId) {
        log.info("Fetching doctor profile for user ID: {}", userId);
        
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user ID: " + userId));
        
        return mapToResponse(doctor);
    }

    @Transactional
    public DoctorResponse updateMyProfile(Long userId, DoctorProfileRequest request) {
        log.info("Updating doctor profile for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        if (user.getRole() != UserRole.DOCTOR) {
            throw new UnauthorizedException("Only doctors can update doctor profiles");
        }
        
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        
        // Update profile
        doctor.setSpecialization(request.getSpecialization());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setYearsOfExperience(request.getYearsOfExperience());
        doctor.setBiography(request.getBiography());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setAvailableForConsultation(request.getAvailableForConsultation());
        doctor.setClinicAddress(request.getClinicAddress());
        doctor.setClinicPhone(request.getClinicPhone());
        
        doctor = doctorRepository.save(doctor);
        log.info("Doctor profile updated successfully for user ID: {}", userId);
        
        return mapToResponse(doctor);
    }

    @Transactional
    public DoctorResponse updateAvailability(Long userId, Boolean available) {
        log.info("Updating availability for doctor user ID: {} to {}", userId, available);
        
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        
        doctor.setAvailableForConsultation(available);
        doctor = doctorRepository.save(doctor);
        
        log.info("Doctor availability updated successfully");
        return mapToResponse(doctor);
    }

    private DoctorResponse mapToResponse(Doctor doctor) {
        DoctorResponse response = new DoctorResponse();
        response.setId(doctor.getId());
        response.setFirstName(doctor.getUser().getFirstName());
        response.setLastName(doctor.getUser().getLastName());
        response.setEmail(doctor.getUser().getEmail());
        response.setPhoneNumber(doctor.getUser().getPhoneNumber());
        response.setSpecialization(doctor.getSpecialization());
        response.setLicenseNumber(doctor.getLicenseNumber());
        response.setYearsOfExperience(doctor.getYearsOfExperience());
        response.setBiography(doctor.getBiography());
        response.setConsultationFee(doctor.getConsultationFee());
        response.setAvailableForConsultation(doctor.getAvailableForConsultation());
        response.setClinicAddress(doctor.getClinicAddress());
        response.setClinicPhone(doctor.getClinicPhone());
        return response;
    }
}
