package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceGenerationRequestDTO {
    
    @NotBlank(message = "session_id is required")
    @JsonProperty("session_id")
    private String sessionId;

    @NotBlank(message = "user_id is required")
    @JsonProperty("user_id")
    private String userId;

    @NotBlank(message = "account_number is required")
    @JsonProperty("account_number")
    private String accountNumber;

    @NotBlank(message = "area_code is required")
    @JsonProperty("area_code")
    private String areaCode;

    @NotNull(message = "bill_cycle is required")
    @Min(value = 1, message = "bill_cycle must be greater than zero")
    @JsonProperty("bill_cycle")
    private Integer billCycle;

    @JsonProperty("bypass_ru")
    private Boolean bypassRu;
}