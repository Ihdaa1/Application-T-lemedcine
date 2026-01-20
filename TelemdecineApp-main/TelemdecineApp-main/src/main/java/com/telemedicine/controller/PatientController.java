package com.telemedicine.controller;

import com.telemedicine.dto.*;
import com.telemedicine.entity.AppointmentStatus;
import com.telemedicine.security.UserPrincipal;
import com.telemedicine.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/profile/me")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        log.info("GET /patients/profile/me - Fetching patient profile");
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            PatientResponse response = patientService.getMyProfile(userPrincipal.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch patient profile: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/profile/me")
    public ResponseEntity<?> updateMyProfile(
            @Valid @RequestBody PatientProfileRequest request,
            Authentication authentication) {
        
        log.info("PUT /patients/profile/me - Updating patient profile");
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            PatientResponse response = patientService.updateMyProfile(userPrincipal.getId(), request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to update patient profile: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/appointments/me")
    public ResponseEntity<?> getMyAppointments(Authentication authentication) {
        log.info("GET /patients/appointments/me - Fetching patient's appointments");
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            List<AppointmentResponse> appointments = patientService.getMyAppointments(userPrincipal.getId());
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            log.error("Failed to fetch appointments: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/appointments/me/status/{status}")
    public ResponseEntity<?> getMyAppointmentsByStatus(
            @PathVariable AppointmentStatus status,
            Authentication authentication) {
        
        log.info("GET /patients/appointments/me/status/{} - Fetching appointments by status", status);
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            List<AppointmentResponse> appointments = patientService.getMyAppointmentsByStatus(
                    userPrincipal.getId(), status);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            log.error("Failed to fetch appointments by status: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/prescriptions/me")
    public ResponseEntity<?> getMyPrescriptions(Authentication authentication) {
        log.info("GET /patients/prescriptions/me - Fetching patient's prescriptions");
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            List<PrescriptionResponse> prescriptions = patientService.getMyPrescriptions(userPrincipal.getId());
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            log.error("Failed to fetch prescriptions: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/prescriptions/me/active")
    public ResponseEntity<?> getMyActivePrescriptions(Authentication authentication) {
        log.info("GET /patients/prescriptions/me/active - Fetching patient's active prescriptions");
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            List<PrescriptionResponse> prescriptions = patientService.getMyActivePrescriptions(userPrincipal.getId());
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            log.error("Failed to fetch active prescriptions: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/medical-records/me")
    public ResponseEntity<?> getMyMedicalRecords(Authentication authentication) {
        log.info("GET /patients/medical-records/me - Fetching patient's medical records");
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            List<MedicalRecordResponse> records = patientService.getMyMedicalRecords(userPrincipal.getId());
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("Failed to fetch medical records: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}
