package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkInvoiceReviewRequestDTO {

    @NotBlank(message = "session_id is required")
    @JsonProperty("session_id")
    private String sessionId;

    @NotBlank(message = "user_id is required")
    @JsonProperty("user_id")
    private String userId;

    @NotEmpty(message = "invoice_ids cannot be empty")
    @JsonProperty("invoice_ids")
    private List<Long> invoiceIds;

    @NotBlank(message = "action is required")
    @JsonProperty("action")
    private String action; // "APPROVE" or "REJECT"

    @JsonProperty("remarks")
    private String remarks;
}
