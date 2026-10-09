package com.example.SPSProjectBackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class AgreementDTO {
    private String initialTariff;
    private BigDecimal voltageLevelKv;
    private BigDecimal generationLosses;
    private List<PaymentDeductionDTO> paymentDeductions;
    private List<AddendumDTO> addendums;
    private List<String> tariffFileNames;
    private List<String> agreementFileNames;
}
