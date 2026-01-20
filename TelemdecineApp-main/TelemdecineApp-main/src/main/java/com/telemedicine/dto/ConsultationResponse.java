package com.telemedicine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationResponse {
    private Long id;
    private Long appointmentId;
    private String diagnosis;
    private String treatment;
    private String recommendations;
    private String followUpInstructions;
    private Boolean followUpRequired;
    private String doctorNotes;
    private String vitalSigns;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
