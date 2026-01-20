package com.telemedicine.dto;

import com.telemedicine.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientProfileRequest {
    private LocalDate dateOfBirth;
    private Gender gender;
    private String bloodType;
    private String allergies;
    private String medicalHistory;
    private String emergencyContact;
    private String emergencyPhone;
    private String address;
    private String city;
    private String country;
    private String postalCode;
}
