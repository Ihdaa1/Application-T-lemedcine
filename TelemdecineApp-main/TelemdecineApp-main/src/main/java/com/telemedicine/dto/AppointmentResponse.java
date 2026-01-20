package com.telemedicine.dto;

import com.telemedicine.entity.AppointmentStatus;
import com.telemedicine.entity.AppointmentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private LocalDateTime appointmentDate;
    private AppointmentType type;
    private AppointmentStatus status;
    private String reason;
    private String symptoms;
    private String notes;
    private String meetingLink;
    private Integer durationMinutes;
    private LocalDateTime createdAt;
}
