package com.telemedicine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "consultations")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Consultation extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(length = 5000)
    private String diagnosis;

    @Column(length = 5000)
    private String treatment;

    @Column(length = 2000)
    private String recommendations;

    @Column(length = 2000)
    private String followUpInstructions;

    @Column(name = "follow_up_required")
    private Boolean followUpRequired = false;

    @Column(length = 2000)
    private String doctorNotes;

    @Column(name = "vital_signs", length = 1000)
    private String vitalSigns;
}
