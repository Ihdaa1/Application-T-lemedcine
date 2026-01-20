package com.telemedicine.service;

import com.telemedicine.dto.AuthResponse;
import com.telemedicine.dto.LoginRequest;
import com.telemedicine.dto.RegisterRequest;
import com.telemedicine.entity.Doctor;
import com.telemedicine.entity.Patient;
import com.telemedicine.entity.User;
import com.telemedicine.entity.UserRole;
import com.telemedicine.exception.BadRequestException;
import com.telemedicine.repository.DoctorRepository;
import com.telemedicine.repository.PatientRepository;
import com.telemedicine.repository.UserRepository;
import com.telemedicine.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());

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

        // Generate JWT token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String jwt = tokenProvider.generateToken(authentication);

        return new AuthResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.getActive()
        );
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Attempting to login user with email: {}", request.getEmail());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String jwt = tokenProvider.generateToken(authentication);

        // Get user details
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        log.info("User logged in successfully: {}", user.getEmail());

        return new AuthResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.getActive()
        );
    }
}
