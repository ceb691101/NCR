package com.example.SPSProjectBackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeRuRequestDTO {
    @NotNull(message = "Folio Number is required")
    private Short folioNo;
    
    @NotNull(message = "New RU Difference is required")
    private Short newRu;
    
    private String userId;
    private String sessionId;
}
