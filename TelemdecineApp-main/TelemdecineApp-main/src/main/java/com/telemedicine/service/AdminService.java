package com.telemedicine.service;

import com.telemedicine.dto.AuthResponse;
import com.telemedicine.dto.RegisterRequest;
import com.telemedicine.dto.StatisticsResponse;
import com.telemedicine.entity.AppointmentStatus;
import com.telemedicine.entity.Doctor;
import com.telemedicine.entity.Patient;
import com.telemedicine.entity.User;
import com.telemedicine.entity.UserRole;
import com.telemedicine.exception.BadRequestException;
import com.telemedicine.exception.ResourceNotFoundException;
import com.telemedicine.repository.AppointmentRepository;
import com.telemedicine.repository.ConsultationRepository;
import com.telemedicine.repository.DoctorRepository;
import com.telemedicine.repository.MedicalRecordRepository;
import com.telemedicine.repository.PatientRepository;
import com.telemedicine.repository.PrescriptionRepository;
import com.telemedicine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final ConsultationRepository consultationRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<AuthResponse> getAllUsers() {
        log.info("Fetching all users");
        
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToAuthResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AuthResponse getUserById(Long userId) {
        log.info("Fetching user by ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        return mapToAuthResponse(user);
    }

    @Transactional
    public AuthResponse createUser(RegisterRequest request) {
        log.info("Admin creating user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email address already in use");
        }

        // Create and save user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole());
        user.setActive(true);

        user = userRepository.save(user);
        log.info("User created successfully with ID: {}", user.getId());

        // Create Patient or Doctor profile based on role
        if (request.getRole() == UserRole.PATIENT) {
            Patient patient = new Patient();
            patient.setUser(user);
            patientRepository.save(patient);
            log.info("Patient profile created for user ID: {}", user.getId());
        } else if (request.getRole() == UserRole.DOCTOR) {
            Doctor doctor = new Doctor();
            doctor.setUser(user);
            // Default values for required fields
            doctor.setSpecialization("General Medicine");
            doctor.setLicenseNumber("LICENSE-" + System.currentTimeMillis());
            doctor.setAvailableForConsultation(true);
            doctorRepository.save(doctor);
            log.info("Doctor profile created for user ID: {}", user.getId());
        }

        return mapToAuthResponse(user);
    }

    @Transactional
    public AuthResponse updateUserRole(Long userId, UserRole newRole) {
        log.info("Updating user {} role to: {}", userId, newRole);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Update role
        UserRole oldRole = user.getRole();
        user.setRole(newRole);
        user = userRepository.save(user);

        log.info("User {} role updated from {} to {}", userId, oldRole, newRole);

        // Handle profile creation/deletion based on role change
        if (oldRole == UserRole.PATIENT && newRole == UserRole.DOCTOR) {
            // Remove patient profile and create doctor profile
            patientRepository.deleteByUserId(userId);
            Doctor doctor = new Doctor();
            doctor.setUser(user);
            doctor.setSpecialization("General Medicine");
            doctor.setLicenseNumber("LICENSE-" + System.currentTimeMillis());
            doctor.setAvailableForConsultation(true);
            doctorRepository.save(doctor);
            log.info("Converted patient profile to doctor profile for user ID: {}", userId);
        } else if (oldRole == UserRole.DOCTOR && newRole == UserRole.PATIENT) {
            // Remove doctor profile and create patient profile
            doctorRepository.deleteByUserId(userId);
            Patient patient = new Patient();
            patient.setUser(user);
            patientRepository.save(patient);
            log.info("Converted doctor profile to patient profile for user ID: {}", userId);
        } else if (oldRole == UserRole.PATIENT && newRole == UserRole.ADMIN) {
            // Remove patient profile
            patientRepository.deleteByUserId(userId);
            log.info("Removed patient profile for user ID: {} (now admin)", userId);
        } else if (oldRole == UserRole.DOCTOR && newRole == UserRole.ADMIN) {
            // Remove doctor profile
            doctorRepository.deleteByUserId(userId);
            log.info("Removed doctor profile for user ID: {} (now admin)", userId);
        } else if (oldRole == UserRole.ADMIN && newRole == UserRole.PATIENT) {
            // Create patient profile
            Patient patient = new Patient();
            patient.setUser(user);
            patientRepository.save(patient);
            log.info("Created patient profile for user ID: {} (was admin)", userId);
        } else if (oldRole == UserRole.ADMIN && newRole == UserRole.DOCTOR) {
            // Create doctor profile
            Doctor doctor = new Doctor();
            doctor.setUser(user);
            doctor.setSpecialization("General Medicine");
            doctor.setLicenseNumber("LICENSE-" + System.currentTimeMillis());
            doctor.setAvailableForConsultation(true);
            doctorRepository.save(doctor);
            log.info("Created doctor profile for user ID: {} (was admin)", userId);
        }

        return mapToAuthResponse(user);
    }

    @Transactional
    public AuthResponse updateUserStatus(Long userId, Boolean active) {
        log.info("Updating user {} status to: {}", userId, active);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setActive(active);
        user = userRepository.save(user);

        log.info("User {} status updated to: {}", userId, active);

        return mapToAuthResponse(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Delete associated profiles
        if (user.getRole() == UserRole.PATIENT && user.getPatient() != null) {
            patientRepository.deleteById(user.getPatient().getId());
        } else if (user.getRole() == UserRole.DOCTOR && user.getDoctor() != null) {
            doctorRepository.deleteById(user.getDoctor().getId());
        }

        userRepository.delete(user);
        log.info("User {} deleted successfully", userId);
    }

    @Transactional(readOnly = true)
    public StatisticsResponse getStatistics() {
        log.info("Fetching application statistics");
        
        Long totalUsers = userRepository.count();
        Long totalPatients = (long) userRepository.findByRole(UserRole.PATIENT).size();
        Long totalDoctors = (long) userRepository.findByRole(UserRole.DOCTOR).size();
        Long totalAdmins = (long) userRepository.findByRole(UserRole.ADMIN).size();
        Long activeUsers = (long) userRepository.findByActiveTrue().size();
        
        Long totalAppointments = appointmentRepository.count();
        Long scheduledAppointments = (long) appointmentRepository.findByStatus(AppointmentStatus.SCHEDULED).size();
        Long completedAppointments = (long) appointmentRepository.findByStatus(AppointmentStatus.COMPLETED).size();
        Long cancelledAppointments = (long) appointmentRepository.findByStatus(AppointmentStatus.CANCELLED).size();
        
        Long totalPrescriptions = prescriptionRepository.count();
        Long activePrescriptions = prescriptionRepository.findAll().stream()
                .filter(p -> p.getIsActive() != null && p.getIsActive())
                .count();
        
        Long totalMedicalRecords = medicalRecordRepository.count();
        Long totalConsultations = consultationRepository.count();
        
        return new StatisticsResponse(
                totalUsers,
                totalPatients,
                totalDoctors,
                totalAdmins,
                activeUsers,
                totalAppointments,
                scheduledAppointments,
                completedAppointments,
                cancelledAppointments,
                totalPrescriptions,
                activePrescriptions,
                totalMedicalRecords,
                totalConsultations
        );
    }

    private AuthResponse mapToAuthResponse(User user) {
        return new AuthResponse(
                null, // No token for admin operations
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.getActive()
        );
    }
}