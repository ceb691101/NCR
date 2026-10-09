package com.example.SPSProjectBackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "APPLICATIONTYPES")
public class ApplicationType {
    @Id
    @Column(name = "APPTYPE", nullable = false, length = 50)
    private String apptype;

    @Column(name = "DESCRIPTION", length = 300)
    private String description;

    @Column(name = "SYS_TYPE", length = 3)
    private String sysType;

}