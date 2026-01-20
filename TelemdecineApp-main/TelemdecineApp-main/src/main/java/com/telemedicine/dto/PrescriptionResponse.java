package com.telemedicine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String medicationName;
    private String dosage;
    private String frequency;
    private Integer duration;
    private String instructions;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private String notes;
    private LocalDateTime createdAt;
}
