package com.example.SPSProjectBackend.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApplicationTypeDto {
    private String apptype;
    private String description;
}
