package com.telemedicine.controller;

import com.telemedicine.dto.ApiResponse;
import com.telemedicine.dto.AppointmentRequest;
import com.telemedicine.dto.AppointmentResponse;
import com.telemedicine.dto.AppointmentUpdateRequest;
import com.telemedicine.entity.AppointmentStatus;
import com.telemedicine.security.UserPrincipal;
import com.telemedicine.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<?> createAppointment(
            @Valid @RequestBody AppointmentRequest request,
            Authentication authentication) {
        
        log.info("POST /appointments - Creating new appointment");
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            AppointmentResponse response = appointmentService.createAppointment(userPrincipal.getId(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Failed to create appointment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/my-appointments")
    public ResponseEntity<?> getMyAppointments(Authentication authentication) {
        log.info("GET /appointments/my-appointments - Fetching user appointments");
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            List<AppointmentResponse> appointments = appointmentService.getPatientAppointments(userPrincipal.getId());
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            log.error("Failed to fetch appointments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/doctor/appointments")
    public ResponseEntity<?> getDoctorAppointments(Authentication authentication) {
        log.info("GET /appointments/doctor/appointments - Fetching doctor appointments");
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            List<AppointmentResponse> appointments = appointmentService.getDoctorAppointments(userPrincipal.getId());
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            log.error("Failed to fetch doctor appointments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<?> getAppointmentById(
            @PathVariable Long appointmentId,
            Authentication authentication) {
        
        log.info("GET /appointments/{} - Fetching appointment details", appointmentId);
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            AppointmentResponse response = appointmentService.getAppointmentById(userPrincipal.getId(), appointmentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch appointment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/{appointmentId}")
    public ResponseEntity<?> updateAppointment(
            @PathVariable Long appointmentId,
            @Valid @RequestBody AppointmentUpdateRequest request,
            Authentication authentication) {
        
        log.info("PUT /appointments/{} - Updating appointment", appointmentId);
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            AppointmentResponse response = appointmentService.updateAppointment(userPrincipal.getId(), appointmentId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to update appointment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long appointmentId,
            Authentication authentication) {
        
        log.info("DELETE /appointments/{} - Cancelling appointment", appointmentId);
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            appointmentService.cancelAppointment(userPrincipal.getId(), appointmentId);
            return ResponseEntity.ok(new ApiResponse(true, "Appointment cancelled successfully"));
        } catch (Exception e) {
            log.error("Failed to cancel appointment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getAppointmentsByStatus(
            @PathVariable AppointmentStatus status,
            Authentication authentication) {
        
        log.info("GET /appointments/status/{} - Fetching appointments by status", status);
        
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            List<AppointmentResponse> appointments = appointmentService.getAppointmentsByStatus(userPrincipal.getId(), status);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            log.error("Failed to fetch appointments by status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}
