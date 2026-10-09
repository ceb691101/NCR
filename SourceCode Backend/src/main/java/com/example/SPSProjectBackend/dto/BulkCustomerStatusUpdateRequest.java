package com.example.SPSProjectBackend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BulkCustomerStatusUpdateRequest {
    @NotBlank(message = "Account number is required")
    private String acc_nbr;

    @NotNull(message = "Status is required")
    @Min(value = 1, message = "Status must be 1 (Inactive) or 2 (Active)")
    @Max(value = 2, message = "Status must be 1 (Inactive) or 2 (Active)")
    private Integer status;
}
