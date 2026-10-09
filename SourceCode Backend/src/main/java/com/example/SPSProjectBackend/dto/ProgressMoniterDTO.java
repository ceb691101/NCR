package com.example.SPSProjectBackend.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProgressMoniterDTO {

    private String id;
    private String deptId;
    private String name;
    private Float percentage;

    // Getters and Setters
}