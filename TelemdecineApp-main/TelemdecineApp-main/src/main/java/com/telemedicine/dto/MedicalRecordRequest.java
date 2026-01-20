package com.telemedicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordRequest {
    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Record date is required")
    private LocalDate recordDate;

    private String recordType;
    private String fileUrl;
    private String fileName;
}
