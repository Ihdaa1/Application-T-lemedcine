package com.telemedicine.controller;

import com.telemedicine.dto.ApiResponse;
import com.telemedicine.dto.DoctorProfileRequest;
import com.telemedicine.dto.DoctorResponse;
import com.telemedicine.security.UserPrincipal;
import com.telemedicine.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        log.info("GET /doctors - Fetching all doctors");
        List<DoctorResponse> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/available")
    public ResponseEntity<List<DoctorResponse>> getAvailableDoctors() {
        log.info("GET /doctors/available - Fetching available doctors");
        List<DoctorResponse> doctors = doctorService.getAvailableDoctors();
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<List<DoctorResponse>> getDoctorsBySpecialization(
            @PathVariable String specialization) {
        log.info("GET /doctors/specialization/{} - Fetching doctors by specialization", specialization);
        List<DoctorResponse> doctors = doctorService.getDoctorsBySpecialization(specialization);
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<?> getDoctorById(@PathVariable Long doctorId) {
        log.info("GET /doctors/{} - Fetching doctor details", doctorId);
        
        try {
            DoctorResponse response = doctorService.getDoctorById(doctorId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch doctor: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/profile/me")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        log.info("GET /doctors/profile/me - Fetching doctor profile");
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            DoctorResponse response = doctorService.getMyProfile(userPrincipal.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch doctor profile: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/profile/me")
    public ResponseEntity<?> updateMyProfile(
            @Valid @RequestBody DoctorProfileRequest request,
            Authentication authentication) {
        
        log.info("PUT /doctors/profile/me - Updating doctor profile");
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            DoctorResponse response = doctorService.updateMyProfile(userPrincipal.getId(), request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to update doctor profile: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/availability")
    public ResponseEntity<?> updateAvailability(
            @RequestBody Map<String, Boolean> request,
            Authentication authentication) {
        
        log.info("PUT /doctors/availability - Updating doctor availability");
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Boolean available = request.get("available");
            DoctorResponse response = doctorService.updateAvailability(userPrincipal.getId(), available);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to update availability: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}
