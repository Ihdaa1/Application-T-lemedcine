package com.telemedicine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationRequest {
    @NotBlank(message = "Diagnosis is required")
    private String diagnosis;

    private String treatment;
    private String recommendations;
    private String followUpInstructions;
    private Boolean followUpRequired = false;
    private String doctorNotes;
    private String vitalSigns;
}
