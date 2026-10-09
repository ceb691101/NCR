package com.example.SPSProjectBackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvoiceCalculationServiceTest {

    private InvoiceCalculationService calculationService;

    @BeforeEach
    void setUp() {
        calculationService = new InvoiceCalculationService();
    }

    @Test
    void testCalculateEnergyFromIntervals_Standard() {
        BigDecimal prsntR1 = new BigDecimal("150.0");
        BigDecimal prvR1 = new BigDecimal("100.0"); // diff = 50.0

        BigDecimal prsntR2 = new BigDecimal("300.0");
        BigDecimal prvR2 = new BigDecimal("200.0"); // diff = 100.0

        BigDecimal prsntR3 = new BigDecimal("450.0");
        BigDecimal prvR3 = new BigDecimal("400.0"); // diff = 50.0

        BigDecimal total = calculationService.calculateEnergyFromIntervals(prsntR1, prvR1, prsntR2, prvR2, prsntR3, prvR3);

        assertEquals(new BigDecimal("200.0"), total);
    }

    @Test
    void testCalculateEnergyFromIntervals_WithNulls() {
        BigDecimal prsntR1 = new BigDecimal("100.0");
        BigDecimal prvR1 = null; // null treated as 0 -> diff = 100.0

        BigDecimal prsntR2 = null;
        BigDecimal prvR2 = null; // 0

        BigDecimal prsntR3 = new BigDecimal("250.0");
        BigDecimal prvR3 = new BigDecimal("200.0"); // diff = 50.0

        BigDecimal total = calculationService.calculateEnergyFromIntervals(prsntR1, prvR1, prsntR2, prvR2, prsntR3, prvR3);

        assertEquals(new BigDecimal("150.0"), total);
    }

    @Test
    void testCalculateEnergyFromIntervals_NegativeDifferenceClampedToZero() {
        BigDecimal prsntR1 = new BigDecimal("100.0");
        BigDecimal prvR1 = new BigDecimal("150.0"); // negative -> clamped to 0

        BigDecimal prsntR2 = new BigDecimal("200.0");
        BigDecimal prvR2 = new BigDecimal("150.0"); // diff = 50.0

        BigDecimal prsntR3 = new BigDecimal("300.0");
        BigDecimal prvR3 = new BigDecimal("250.0"); // diff = 50.0

        BigDecimal total = calculationService.calculateEnergyFromIntervals(prsntR1, prvR1, prsntR2, prvR2, prsntR3, prvR3);

        assertEquals(new BigDecimal("100.0"), total);
    }

    @Test
    void testCalculateCostOfEnergy() {
        BigDecimal energy = new BigDecimal("1000.0");
        BigDecimal rate = new BigDecimal("25.50");

        BigDecimal cost = calculationService.calculateCostOfEnergy(energy, rate);

        assertEquals(new BigDecimal("25500.00"), cost);
    }

    @Test
    void testDetermineInvoiceMonth_FromYearAndMonth() {
        java.time.YearMonth ym = calculationService.determineInvoiceMonth(2026, 8);
        assertEquals(java.time.YearMonth.of(2026, 8), ym);

        java.time.YearMonth sept = calculationService.determineInvoiceMonth(2026, 9);
        assertEquals(java.time.YearMonth.of(2026, 9), sept);
    }

    @Test
    void testDetermineInvoiceMonth_FallbackWhenNull() {
        java.time.YearMonth fallback = calculationService.determineInvoiceMonth(null, null);
        assertEquals(java.time.YearMonth.now().minusMonths(1), fallback);
    }

    @Test
    void testCalculatePeriodOfGeneration_LengthOfMonth() {
        assertEquals(31, calculationService.calculatePeriodOfGeneration(java.time.YearMonth.of(2026, 8))); // August: 31 days
        assertEquals(30, calculationService.calculatePeriodOfGeneration(java.time.YearMonth.of(2026, 9))); // September: 30 days
        assertEquals(28, calculationService.calculatePeriodOfGeneration(java.time.YearMonth.of(2025, 2))); // Feb non-leap: 28 days
        assertEquals(29, calculationService.calculatePeriodOfGeneration(java.time.YearMonth.of(2024, 2))); // Feb leap: 29 days
        assertEquals(30, calculationService.calculatePeriodOfGeneration(null)); // null fallback
    }
}
