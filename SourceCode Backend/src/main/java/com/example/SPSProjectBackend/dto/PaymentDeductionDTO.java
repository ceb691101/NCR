package com.example.SPSProjectBackend.dto;

import lombok.Data;
import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class PaymentDeductionDTO {
    private String type;
    private BigDecimal percentage;
}
