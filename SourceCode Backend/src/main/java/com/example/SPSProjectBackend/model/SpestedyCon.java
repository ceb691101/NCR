package com.example.SPSProjectBackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "SPESTEDY_CONS")
public class SpestedyCon {
    @EmbeddedId
    private SpestedyConId id;

    @Column(name = "APPOINMENT_TYPE", length = 10)
    private String appoinmentType;

    @Column(name = "REFERENCE_NO", length = 25)
    private String referenceNo;

    @Column(name = "APPOINTMENT_DATE")
    private LocalDate appointmentDate;

    @Column(name = "SUBURB", length = 50)
    private String suburb;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "ALLOCATED_BY", length = 10)
    private String allocatedBy;

    @Column(name = "ALLOCATED_DATE")
    private LocalDate allocatedDate;

    @Column(name = "ALLOCATED_TIME", length = 11)
    private String allocatedTime;

    @Column(name = "ALLOCATED_TO", length = 20)
    private String allocatedTo;

    @Column(name = "DESCRIPTION", length = 800)
    private String description;

    @Column(name = "WESTIMATE_NO", length = 20)
    private String westimateNo;

    @Column(name = "TIME_SESSION", length = 10)
    private String timeSession;

}