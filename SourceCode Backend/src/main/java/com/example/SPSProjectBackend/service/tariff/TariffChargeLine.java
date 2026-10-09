package com.example.SPSProjectBackend.service.tariff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Generic domain model representing an individual tariff charge line
 * within a tariff calculation. Suitable for multi-rate and split-invoice tariffs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffChargeLine {

    private String label;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer numberOfDays;
    private BigDecimal energyKwh;
    private BigDecimal ratePerKwh;
    private BigDecimal costOfEnergy;
    private TariffRateType rateType;
}
