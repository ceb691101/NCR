package com.example.SPSProjectBackend.service.tariff;

import com.example.SPSProjectBackend.model.NcreDeveloper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Context payload containing only the information required for tariff calculation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffCalculationContext {

    private NcreDeveloper developer;
    private Short folioNo;
    private String tariffType;
    private BigDecimal totalEnergySentToGrid;
    private BigDecimal generationLosses;
    private BigDecimal explicitLossKwh;
    private Integer billCycle;
    private YearMonth billingMonth;
    private LocalDate presentReadingDate;
    private LocalDate previousReadingDate;
}
