package com.telemedicine.dto;

import com.telemedicine.entity.AppointmentStatus;
import com.telemedicine.entity.AppointmentType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Appointment date is required")
    private LocalDateTime appointmentDate;

    private AppointmentType type = AppointmentType.VIDEO_CONSULTATION;

    private String reason;

    private String symptoms;

    private Integer durationMinutes = 30;
}
