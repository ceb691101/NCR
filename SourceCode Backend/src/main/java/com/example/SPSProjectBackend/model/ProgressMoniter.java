package com.example.SPSProjectBackend.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;

import java.util.Objects;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "PMMILESTONEMASTER")
@IdClass(ProgressMoniterId.class)
public class ProgressMoniter implements Serializable {

    @Id
    @Column(name = "ID", nullable = false, length = 10)
    private String id;

    @Id
    @Column(name = "DEPT_ID", nullable = false, length = 20)
    private String deptId;

    @Column(name = "NAME", length = 100)
    private String name;

    @Column(name = "PERCENTAGE")
    private Float percentage;

    // Getters and Setters


}