package com.telemedicine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    private Long totalUsers;
    private Long totalPatients;
    private Long totalDoctors;
    private Long totalAdmins;
    private Long activeUsers;
    private Long totalAppointments;
    private Long scheduledAppointments;
    private Long completedAppointments;
    private Long cancelledAppointments;
    private Long totalPrescriptions;
    private Long activePrescriptions;
    private Long totalMedicalRecords;
    private Long totalConsultations;
}
