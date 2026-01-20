package com.telemedicine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileRequest {
    @NotBlank(message = "Specialization is required")
    private String specialization;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    private Integer yearsOfExperience;
    private String biography;
    private Double consultationFee;
    private Boolean availableForConsultation = true;
    private String clinicAddress;
    private String clinicPhone;
}
