package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.model.TmpReadings;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

/**
 * Service responsible for invoice-specific calculations.
 * Follows Single Responsibility Principle — calculation logic only.
 */
@Service
public class InvoiceCalculationService {

    private static final Set<String> ENERGY_METER_TYPES = Set.of("KWO", "KWD", "KWP");
    private static final int HOURS_PER_DAY = 24;
    private static final int KW_PER_MW = 1000;

    /**
     * Sum present readings across KWO, KWD, KWP meter types.
     */
    public int calculateTotalPresentReading(List<TmpReadings> readings) {
        return readings.stream()
                .filter(r -> r.getMtrType() != null && ENERGY_METER_TYPES.contains(r.getMtrType().trim().toUpperCase()))
                .filter(r -> r.getPrsntRdn() != null)
                .mapToInt(TmpReadings::getPrsntRdn)
                .sum();
    }

    /**
     * Sum previous readings across KWO, KWD, KWP meter types.
     */
    public int calculateTotalPreviousReading(List<TmpReadings> readings) {
        return readings.stream()
                .filter(r -> r.getMtrType() != null && ENERGY_METER_TYPES.contains(r.getMtrType().trim().toUpperCase()))
                .filter(r -> r.getPrvRdn() != null)
                .mapToInt(TmpReadings::getPrvRdn)
                .sum();
    }

    /**
     * Calculate energy in kWh: total present - total previous.
     */
    public BigDecimal calculateEnergyKwh(int totalPresentReading, int totalPreviousReading) {
        int difference = totalPresentReading - totalPreviousReading;
        return new BigDecimal(Math.max(0, difference)).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * Calculate energy sent to grid for a single interval: max(0, present - previous).
     */
    public BigDecimal calculateIntervalEnergy(BigDecimal present, BigDecimal previous) {
        BigDecimal p = present != null ? present : BigDecimal.ZERO;
        BigDecimal v = previous != null ? previous : BigDecimal.ZERO;
        BigDecimal diff = p.subtract(v);
        return diff.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : diff;
    }

    /**
     * Calculate sum of energy sent to grid across R1, R2, and R3 intervals.
     * Each interval energy is max(0, present - previous).
     */
    public BigDecimal calculateEnergyFromIntervals(BigDecimal prsntR1, BigDecimal prvR1,
                                                   BigDecimal prsntR2, BigDecimal prvR2,
                                                   BigDecimal prsntR3, BigDecimal prvR3) {
        BigDecimal r1 = calculateIntervalEnergy(prsntR1, prvR1);
        BigDecimal r2 = calculateIntervalEnergy(prsntR2, prvR2);
        BigDecimal r3 = calculateIntervalEnergy(prsntR3, prvR3);

        return r1.add(r2).add(r3).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * Calculate the number of days in the given invoice month.
     * The invoice month is the month of the bill cycle.
     */
    public int calculatePeriodOfGeneration(YearMonth invoiceMonth) {
        if (invoiceMonth == null) {
            return 30;
        }
        return invoiceMonth.lengthOfMonth();
    }

    /**
     * Determine the invoice month from bill cycle year and month.
     */
    public YearMonth determineInvoiceMonth(Integer billYear, Integer billMonth) {
        if (billYear != null && billMonth != null && billMonth >= 1 && billMonth <= 12 && billYear >= 1900 && billYear <= 2100) {
            return YearMonth.of(billYear, billMonth);
        }
        return determineInvoiceMonth();
    }

    /**
     * Determine the fallback invoice month (previous month relative to today).
     */
    public YearMonth determineInvoiceMonth() {
        return YearMonth.now().minusMonths(1);
    }

    /**
     * Calculate plant factor percentage.
     * Formula: (energy / (periodOfGeneration * 24 * allowedPowerGenerationMW * 1000)) * 100
     */
    public BigDecimal calculatePlantFactor(BigDecimal energyKwh, int periodDays, BigDecimal allowedGenerationMw) {
        if (energyKwh == null || allowedGenerationMw == null
                || allowedGenerationMw.compareTo(BigDecimal.ZERO) == 0
                || periodDays <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal denominator = allowedGenerationMw
                .multiply(new BigDecimal(KW_PER_MW))
                .multiply(new BigDecimal(periodDays))
                .multiply(new BigDecimal(HOURS_PER_DAY));

        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return energyKwh
                .divide(denominator, 10, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate cost of energy: energy * rate per kWh.
     */
    public BigDecimal calculateCostOfEnergy(BigDecimal energyKwh, BigDecimal ratePerKwh) {
        if (energyKwh == null || ratePerKwh == null) {
            return BigDecimal.ZERO;
        }
        return energyKwh.multiply(ratePerKwh).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Extract the multiply factor (mFactor) from readings.
     * Takes the first non-null mFactor found across energy meter types.
     * Defaults to 1.000 if none found.
     */
    public BigDecimal extractMultiplyFactor(List<TmpReadings> readings) {
        return readings.stream()
                .filter(r -> r.getMtrType() != null && ENERGY_METER_TYPES.contains(r.getMtrType().trim().toUpperCase()))
                .filter(r -> r.getMFactor() != null)
                .map(TmpReadings::getMFactor)
                .findFirst()
                .orElse(new BigDecimal("1.000"));
    }
}
