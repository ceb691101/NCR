package com.example.SPSProjectBackend.service.tariff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Result model returned by a TariffCalculator strategy.
 * Contains financial and energy figures computed strictly using BigDecimal.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffCalculationResult {

    private String tariffType;
    private BigDecimal selectedTariffRate;
    private BigDecimal originalEnergySentToGrid;
    private BigDecimal generationLosses;
    private BigDecimal eligibleEnergy;
    private BigDecimal costOfEnergy;
    private TariffRateType selectedRateType;
    private java.util.List<TariffChargeLine> chargeLines;

    public boolean isPrvTariffRateSelected() {
        return selectedRateType == TariffRateType.PRV_TARIFF_RATE;
    }

    public boolean isCurTariffRateSelected() {
        return selectedRateType == TariffRateType.CUR_TARIFF_RATE;
    }

    public boolean isMultiRateSelected() {
        return selectedRateType == TariffRateType.MULTI_RATE;
    }
}
