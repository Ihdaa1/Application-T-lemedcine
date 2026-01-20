package com.telemedicine.controller;

import com.telemedicine.dto.ApiResponse;
import com.telemedicine.dto.PrescriptionRequest;
import com.telemedicine.dto.PrescriptionResponse;
import com.telemedicine.security.UserPrincipal;
import com.telemedicine.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prescriptions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    public ResponseEntity<?> createPrescription(
            @Valid @RequestBody PrescriptionRequest request,
            Authentication authentication) {
        
        log.info("POST /prescriptions - Creating new prescription");
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            PrescriptionResponse response = prescriptionService.createPrescription(userPrincipal.getId(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Failed to create prescription: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientPrescriptions(
            @PathVariable Long patientId,
            Authentication authentication) {
        
        log.info("GET /prescriptions/patient/{} - Fetching patient prescriptions", patientId);
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            List<PrescriptionResponse> prescriptions = prescriptionService.getPatientPrescriptions(userPrincipal.getId(), patientId);
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            log.error("Failed to fetch patient prescriptions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}/active")
    public ResponseEntity<?> getActivePrescriptions(
            @PathVariable Long patientId,
            Authentication authentication) {
        
        log.info("GET /prescriptions/patient/{}/active - Fetching active prescriptions", patientId);
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            List<PrescriptionResponse> prescriptions = prescriptionService.getActivePrescriptions(userPrincipal.getId(), patientId);
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            log.error("Failed to fetch active prescriptions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/doctor/my-prescriptions")
    public ResponseEntity<?> getDoctorPrescriptions(Authentication authentication) {
        log.info("GET /prescriptions/doctor/my-prescriptions - Fetching doctor prescriptions");
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            List<PrescriptionResponse> prescriptions = prescriptionService.getDoctorPrescriptions(userPrincipal.getId());
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            log.error("Failed to fetch doctor prescriptions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/{prescriptionId}")
    public ResponseEntity<?> getPrescriptionById(
            @PathVariable Long prescriptionId,
            Authentication authentication) {
        
        log.info("GET /prescriptions/{} - Fetching prescription details", prescriptionId);
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            PrescriptionResponse response = prescriptionService.getPrescriptionById(userPrincipal.getId(), prescriptionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch prescription: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/{prescriptionId}/deactivate")
    public ResponseEntity<?> deactivatePrescription(
            @PathVariable Long prescriptionId,
            Authentication authentication) {
        
        log.info("PUT /prescriptions/{}/deactivate - Deactivating prescription", prescriptionId);
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            PrescriptionResponse response = prescriptionService.deactivatePrescription(userPrincipal.getId(), prescriptionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to deactivate prescription: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}
