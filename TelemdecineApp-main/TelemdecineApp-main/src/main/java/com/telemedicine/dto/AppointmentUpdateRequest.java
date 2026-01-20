package com.telemedicine.dto;

import com.telemedicine.entity.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentUpdateRequest {
    private LocalDateTime appointmentDate;
    private AppointmentStatus status;
    private String notes;
    private String meetingLink;
}
