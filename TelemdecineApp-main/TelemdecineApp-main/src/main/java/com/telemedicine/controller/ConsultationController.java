package com.telemedicine.controller;

import com.telemedicine.dto.ApiResponse;
import com.telemedicine.dto.ConsultationRequest;
import com.telemedicine.dto.ConsultationResponse;
import com.telemedicine.security.UserPrincipal;
import com.telemedicine.service.ConsultationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/consultations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ConsultationController {

    private final ConsultationService consultationService;

    @PostMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> createConsultation(
            @PathVariable Long appointmentId,
            @Valid @RequestBody ConsultationRequest request,
            Authentication authentication) {
        
        log.info("POST /consultations/appointment/{} - Creating consultation", appointmentId);
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            ConsultationResponse response = consultationService.createConsultation(userPrincipal.getId(), appointmentId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Failed to create consultation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> getConsultationByAppointment(
            @PathVariable Long appointmentId,
            Authentication authentication) {
        
        log.info("GET /consultations/appointment/{} - Fetching consultation by appointment", appointmentId);
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            ConsultationResponse response = consultationService.getConsultationByAppointmentId(userPrincipal.getId(), appointmentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch consultation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/{consultationId}")
    public ResponseEntity<?> getConsultationById(
            @PathVariable Long consultationId,
            Authentication authentication) {
        
        log.info("GET /consultations/{} - Fetching consultation details", consultationId);
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            ConsultationResponse response = consultationService.getConsultationById(userPrincipal.getId(), consultationId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch consultation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/{consultationId}")
    public ResponseEntity<?> updateConsultation(
            @PathVariable Long consultationId,
            @Valid @RequestBody ConsultationRequest request,
            Authentication authentication) {
        
        log.info("PUT /consultations/{} - Updating consultation", consultationId);
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            ConsultationResponse response = consultationService.updateConsultation(userPrincipal.getId(), consultationId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to update consultation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}
