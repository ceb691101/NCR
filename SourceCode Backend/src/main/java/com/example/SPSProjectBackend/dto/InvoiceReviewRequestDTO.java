package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceReviewRequestDTO {

    @NotBlank(message = "session_id is required")
    @JsonProperty("session_id")
    private String sessionId;

    @NotBlank(message = "user_id is required")
    @JsonProperty("user_id")
    private String userId;

    @NotNull(message = "invoice_id is required")
    @JsonProperty("invoice_id")
    private Long invoiceId;

    @JsonProperty("remarks")
    private String remarks;
}
