package com.telemedicine.controller;

import com.telemedicine.dto.ApiResponse;
import com.telemedicine.dto.MedicalRecordRequest;
import com.telemedicine.dto.MedicalRecordResponse;
import com.telemedicine.security.UserPrincipal;
import com.telemedicine.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medical-records")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping
    public ResponseEntity<?> createMedicalRecord(
            @Valid @RequestBody MedicalRecordRequest request,
            Authentication authentication) {
        
        log.info("POST /medical-records - Creating new medical record");
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            MedicalRecordResponse response = medicalRecordService.createMedicalRecord(userPrincipal.getId(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Failed to create medical record: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPatientMedicalRecords(
            @PathVariable Long patientId,
            Authentication authentication) {
        
        log.info("GET /medical-records/patient/{} - Fetching patient medical records", patientId);
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            List<MedicalRecordResponse> records = medicalRecordService.getPatientMedicalRecords(userPrincipal.getId(), patientId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("Failed to fetch medical records: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<?> getMedicalRecordById(
            @PathVariable Long recordId,
            Authentication authentication) {
        
        log.info("GET /medical-records/{} - Fetching medical record details", recordId);
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            MedicalRecordResponse response = medicalRecordService.getMedicalRecordById(userPrincipal.getId(), recordId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch medical record: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/{recordId}")
    public ResponseEntity<?> updateMedicalRecord(
            @PathVariable Long recordId,
            @Valid @RequestBody MedicalRecordRequest request,
            Authentication authentication) {
        
        log.info("PUT /medical-records/{} - Updating medical record", recordId);
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            MedicalRecordResponse response = medicalRecordService.updateMedicalRecord(userPrincipal.getId(), recordId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to update medical record: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{recordId}")
    public ResponseEntity<?> deleteMedicalRecord(
            @PathVariable Long recordId,
            Authentication authentication) {
        
        log.info("DELETE /medical-records/{} - Deleting medical record", recordId);
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            medicalRecordService.deleteMedicalRecord(userPrincipal.getId(), recordId);
            return ResponseEntity.ok(new ApiResponse(true, "Medical record deleted successfully"));
        } catch (Exception e) {
            log.error("Failed to delete medical record: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}
