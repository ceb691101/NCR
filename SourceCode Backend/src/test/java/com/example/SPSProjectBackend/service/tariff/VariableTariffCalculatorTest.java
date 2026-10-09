package com.example.SPSProjectBackend.service.tariff;

import com.example.SPSProjectBackend.model.NcreDevTariffRate;
import com.example.SPSProjectBackend.repository.NcreDevTariffRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VariableTariffCalculatorTest {

    @Mock
    private NcreDevTariffRateRepository tariffRateRepository;

    private VariableTariffCalculator calculator;
    private AcTariffCalculator calculatorAc;
    private Ttt3TariffCalculator calculatorTtt3;
    private Ttt5TariffCalculator calculatorTtt5;
    private FifteenPlus2019TariffCalculator calculator2019;
    private FifteenPlus2022TariffCalculator calculator2022;
    private FlatTariffCalculator flatCalculator;
    private TariffCalculatorResolver resolver;

    @BeforeEach
    void setUp() {
        calculator = new VariableTariffCalculator(tariffRateRepository);
        calculatorAc = new AcTariffCalculator(tariffRateRepository);
        calculatorTtt3 = new Ttt3TariffCalculator(tariffRateRepository);
        calculatorTtt5 = new Ttt5TariffCalculator(tariffRateRepository);
        calculator2019 = new FifteenPlus2019TariffCalculator(tariffRateRepository);
        calculator2022 = new FifteenPlus2022TariffCalculator(tariffRateRepository);
        flatCalculator = new FlatTariffCalculator(tariffRateRepository);
        resolver = new TariffCalculatorResolver(Arrays.asList(
                flatCalculator, calculator2019, calculator2022, calculatorTtt5, calculatorTtt3, calculatorAc, calculator));
    }

    @Test
    @DisplayName("TEST 1: supports('VT') and its real database variants return true")
    void testSupports_VT() {
        assertTrue(calculator.supports("VT"));
        assertTrue(calculator.supports("VT "));
        assertTrue(calculator.supports(" VT"));
        assertTrue(calculator.supports("vt"));
        assertTrue(calculator.supports("Variable Tariff"));
        assertTrue(calculator.supports(" Variable Tariff "));
        assertTrue(calculator.supports("variable tariff"));
        assertTrue(calculator.supports("Variable Traiff"));
        assertTrue(calculator.supports(" Variable Traiff "));
    }

    @Test
    @DisplayName("TEST 2: Verify VT calculator does not support AC")
    void testDoesNotSupportAC() {
        assertFalse(calculator.supports("AC"));
        assertFalse(calculator.supports("Avoided Cost"));
        assertFalse(calculator.supports("Avoided Cost (AC)"));
        assertFalse(calculator.supports("Avoided Cost  (AC)"));
    }

    @Test
    @DisplayName("TEST 3: Verify VT calculator does not support TTT3")
    void testDoesNotSupportTTT3() {
        assertFalse(calculator.supports("TTT3"));
        assertFalse(calculator.supports("TTT (3 years)"));
        assertFalse(calculator.supports("TTT 3 Years"));
        assertFalse(calculator.supports("TTT (3 yrs)"));
    }

    @Test
    @DisplayName("TEST 4: Verify VT calculator does not support TTT5")
    void testDoesNotSupportTTT5() {
        assertFalse(calculator.supports("TTT5"));
        assertFalse(calculator.supports("TTT (5 years)"));
        assertFalse(calculator.supports("TTT 5 Years"));
    }

    @Test
    @DisplayName("TEST 5: Verify VT calculator does not support 15_2019")
    void testDoesNotSupport15_2019() {
        assertFalse(calculator.supports("15_2019"));
        assertFalse(calculator.supports("15+ (2019)"));
    }

    @Test
    @DisplayName("TEST 6: Verify VT calculator does not support 15_2022")
    void testDoesNotSupport15_2022() {
        assertFalse(calculator.supports("15_2022"));
        assertFalse(calculator.supports("15+ New (2022)"));
    }

    @Test
    @DisplayName("TEST 7: Verify VT calculator does not support FLAT, FLAT_VT, null, or empty")
    void testDoesNotSupportOtherTariffs() {
        assertFalse(calculator.supports("FLAT"));
        assertFalse(calculator.supports("FLAT Tariff"));
        assertFalse(calculator.supports("FLAT_VT"));
        assertFalse(calculator.supports("FLAT  Variable Traiff"));
        assertFalse(calculator.supports(null));
        assertFalse(calculator.supports(""));
        assertFalse(calculator.supports("   "));
    }

    @Test
    @DisplayName("TEST 8: Resolver returns VariableTariffCalculator for VT")
    void testResolver_ResolvesToVariableTariffCalculator() {
        TariffCalculator resolved = resolver.resolve("VT");
        assertNotNull(resolved);
        assertTrue(resolved instanceof VariableTariffCalculator);
        assertSame(calculator, resolved);

        TariffCalculator resolvedName = resolver.resolve("Variable Tariff");
        assertNotNull(resolvedName);
        assertSame(calculator, resolvedName);

        TariffCalculator resolvedDb = resolver.resolve("Variable Traiff");
        assertNotNull(resolvedDb);
        assertSame(calculator, resolvedDb);
    }

    @Test
    @DisplayName("TEST 8b: Resolver maintains independent resolution for all other calculators")
    void testResolver_MaintainsAllCalculators() {
        assertSame(flatCalculator, resolver.resolve("FLAT"));
        assertSame(flatCalculator, resolver.resolve("FLAT_VT"));
        assertSame(calculator2019, resolver.resolve("15_2019"));
        assertSame(calculator2022, resolver.resolve("15_2022"));
        assertSame(calculatorTtt5, resolver.resolve("TTT5"));
        assertSame(calculatorTtt3, resolver.resolve("TTT3"));
        assertSame(calculatorAc, resolver.resolve("AC"));
        assertSame(calculator, resolver.resolve("VT"));
    }

    @Test
    @DisplayName("TEST 9: billingMonth > tariffChangedMonth (tariffChangedMonth < billingMonth) -> cur_tariff_rate selected")
    void testBillingMonthAfterTariffChanged_SelectsCurRate() {
        Short folioNo = 1401;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("VT")
                .prvTariffRate(new BigDecimal("28.68"))
                .curTariffRate(new BigDecimal("31.25"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 9, 20))) // Sept 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .billingMonth(YearMonth.of(2026, 10)) // Oct 2026 (> Sept 2026)
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(new BigDecimal("31.25"), result.getSelectedTariffRate());
        assertEquals(TariffRateType.CUR_TARIFF_RATE, result.getSelectedRateType());
        assertTrue(result.isCurTariffRateSelected());
        assertFalse(result.isPrvTariffRateSelected());
        assertFalse(result.isMultiRateSelected());
        assertNull(result.getChargeLines());
        assertEquals(new BigDecimal("100000.0"), result.getOriginalEnergySentToGrid());
        assertEquals(new BigDecimal("100000.00"), result.getEligibleEnergy());
        assertEquals(new BigDecimal("3125000.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 10: billingMonth < tariffChangedMonth (tariffChangedMonth > billingMonth) -> prv_tariff_rate selected")
    void testBillingMonthBeforeTariffChanged_SelectsPrvRate() {
        Short folioNo = 1401;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("VT")
                .prvTariffRate(new BigDecimal("28.68"))
                .curTariffRate(new BigDecimal("31.25"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 9, 20))) // Sept 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .billingMonth(YearMonth.of(2026, 8)) // Aug 2026 (< Sept 2026)
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(new BigDecimal("28.68"), result.getSelectedTariffRate());
        assertEquals(TariffRateType.PRV_TARIFF_RATE, result.getSelectedRateType());
        assertTrue(result.isPrvTariffRateSelected());
        assertFalse(result.isCurTariffRateSelected());
        assertFalse(result.isMultiRateSelected());
        assertNull(result.getChargeLines());
        assertEquals(new BigDecimal("100000.0"), result.getOriginalEnergySentToGrid());
        assertEquals(new BigDecimal("100000.00"), result.getEligibleEnergy());
        assertEquals(new BigDecimal("2868000.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 11: Transition month: tariffChangedMonth == billingMonth creates two charge lines")
    void testTransitionMonth_CreatesTwoChargeLines() {
        Short folioNo = 1401;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("VT")
                .prvTariffRate(new BigDecimal("28.68"))
                .curTariffRate(new BigDecimal("31.25"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 9, 21))) // Sept 21, 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(new BigDecimal("300000.0"))
                .billingMonth(YearMonth.of(2026, 9)) // Sept 2026 (30 days)
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(TariffRateType.MULTI_RATE, result.getSelectedRateType());
        assertTrue(result.isMultiRateSelected());
        assertNotNull(result.getChargeLines());
        assertEquals(2, result.getChargeLines().size());

        TariffChargeLine line1 = result.getChargeLines().get(0);
        assertEquals("Before Tariff Change", line1.getLabel());
        assertEquals(20, line1.getNumberOfDays()); // Sept 1 to 20
        assertEquals(LocalDate.of(2026, 9, 1), line1.getPeriodStart());
        assertEquals(LocalDate.of(2026, 9, 20), line1.getPeriodEnd());
        assertEquals(new BigDecimal("28.68"), line1.getRatePerKwh());
        assertEquals(TariffRateType.PRV_TARIFF_RATE, line1.getRateType());

        TariffChargeLine line2 = result.getChargeLines().get(1);
        assertEquals("From Tariff Change Date", line2.getLabel());
        assertEquals(10, line2.getNumberOfDays()); // Sept 21 to 30
        assertEquals(LocalDate.of(2026, 9, 21), line2.getPeriodStart());
        assertEquals(LocalDate.of(2026, 9, 30), line2.getPeriodEnd());
        assertEquals(new BigDecimal("31.25"), line2.getRatePerKwh());
        assertEquals(TariffRateType.CUR_TARIFF_RATE, line2.getRateType());
    }

    @Test
    @DisplayName("TEST 12: Day calculation in transition month: beforeDays + afterDays == totalDays")
    void testTransitionMonth_DayCalculations() {
        Short folioNo = 1401;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("VT")
                .prvTariffRate(new BigDecimal("28.68"))
                .curTariffRate(new BigDecimal("31.25"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 9, 21)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(new BigDecimal("300000.0"))
                .billingMonth(YearMonth.of(2026, 9))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        int beforeDays = result.getChargeLines().get(0).getNumberOfDays();
        int afterDays = result.getChargeLines().get(1).getNumberOfDays();
        assertEquals(20, beforeDays);
        assertEquals(10, afterDays);
        assertEquals(30, beforeDays + afterDays);
    }

    @Test
    @DisplayName("TEST 13: Energy conservation: beforeEnergy + afterEnergy == eligibleEnergy")
    void testTransitionMonth_EnergyConservation() {
        Short folioNo = 1401;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("VT")
                .prvTariffRate(new BigDecimal("28.68"))
                .curTariffRate(new BigDecimal("31.25"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 9, 21)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(new BigDecimal("300000.0"))
                .billingMonth(YearMonth.of(2026, 9))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        BigDecimal beforeEnergy = result.getChargeLines().get(0).getEnergyKwh();
        BigDecimal afterEnergy = result.getChargeLines().get(1).getEnergyKwh();

        // 300,000 * 20 / 30 = 200,000.00
        assertEquals(new BigDecimal("200000.00"), beforeEnergy);
        // 300,000 - 200,000 = 100,000.00
        assertEquals(new BigDecimal("100000.00"), afterEnergy);
        assertEquals(result.getEligibleEnergy(), beforeEnergy.add(afterEnergy));
    }

    @Test
    @DisplayName("TEST 14: Cost calculation accuracy: beforeCost, afterCost, totalCost")
    void testTransitionMonth_CostCalculationAccuracy() {
        Short folioNo = 1401;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("VT")
                .prvTariffRate(new BigDecimal("28.68"))
                .curTariffRate(new BigDecimal("31.25"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 9, 21)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(new BigDecimal("300000.0"))
                .billingMonth(YearMonth.of(2026, 9))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        TariffChargeLine line1 = result.getChargeLines().get(0);
        TariffChargeLine line2 = result.getChargeLines().get(1);

        // Line 1: 200,000.00 * 28.68 = 5,736,000.00
        assertEquals(new BigDecimal("5736000.00"), line1.getCostOfEnergy());
        // Line 2: 100,000.00 * 31.25 = 3,125,000.00
        assertEquals(new BigDecimal("3125000.00"), line2.getCostOfEnergy());

        // Total: 5,736,000.00 + 3,125,000.00 = 8,861,000.00
        assertEquals(new BigDecimal("8861000.00"), result.getCostOfEnergy());
        assertEquals(line1.getCostOfEnergy().add(line2.getCostOfEnergy()), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 15: Generation loss applied once on total energy before split")
    void testTransitionMonth_GenerationLossAppliedOnce() {
        Short folioNo = 1401;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("VT")
                .prvTariffRate(new BigDecimal("28.68"))
                .curTariffRate(new BigDecimal("31.25"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 9, 21)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        // 300,000 kWh with 5% generation loss (0.05) -> loss = 15,000 kWh, eligible = 285,000 kWh
        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(new BigDecimal("300000.0"))
                .generationLosses(new BigDecimal("0.05"))
                .billingMonth(YearMonth.of(2026, 9))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertEquals(new BigDecimal("15000.00"), result.getGenerationLosses());
        assertEquals(new BigDecimal("285000.00"), result.getEligibleEnergy());

        BigDecimal beforeEnergy = result.getChargeLines().get(0).getEnergyKwh();
        BigDecimal afterEnergy = result.getChargeLines().get(1).getEnergyKwh();
        assertEquals(result.getEligibleEnergy(), beforeEnergy.add(afterEnergy));
    }

    @Test
    @DisplayName("TEST 16: Generation loss applied once in normal month")
    void testNormalMonth_GenerationLossAppliedOnce() {
        Short folioNo = 1401;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("VT")
                .prvTariffRate(new BigDecimal("28.68"))
                .curTariffRate(new BigDecimal("31.25"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 9, 20)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        // Percentage style: 2.0 (%) on 100,000 -> loss = 2,000, eligible = 98,000
        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .generationLosses(new BigDecimal("2.0"))
                .billingMonth(YearMonth.of(2026, 10))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertEquals(new BigDecimal("2000.00"), result.getGenerationLosses());
        assertEquals(new BigDecimal("98000.00"), result.getEligibleEnergy());
        assertEquals(new BigDecimal("3062500.00"), result.getCostOfEnergy()); // 98,000 * 31.25
    }

    @Test
    @DisplayName("TEST 17: Explicit loss kWh overrides percentage")
    void testExplicitLossKwh() {
        Short folioNo = 1401;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("VT")
                .prvTariffRate(new BigDecimal("28.68"))
                .curTariffRate(new BigDecimal("31.25"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 9, 20)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .generationLosses(new BigDecimal("5.0")) // should be overridden
                .explicitLossKwh(new BigDecimal("1500.00"))
                .billingMonth(YearMonth.of(2026, 10))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertEquals(new BigDecimal("1500.00"), result.getGenerationLosses());
        assertEquals(new BigDecimal("98500.00"), result.getEligibleEnergy());
    }

    @Test
    @DisplayName("TEST 18: Boundary condition: Tariff change on Day 1 of the month")
    void testDay1TariffChange() {
        Short folioNo = 1401;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("VT")
                .prvTariffRate(new BigDecimal("28.68"))
                .curTariffRate(new BigDecimal("31.25"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 9, 1))) // Sept 1
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(new BigDecimal("300000.0"))
                .billingMonth(YearMonth.of(2026, 9))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(1, result.getChargeLines().size());
        TariffChargeLine line = result.getChargeLines().get(0);
        assertEquals("From Tariff Change Date", line.getLabel());
        assertEquals(30, line.getNumberOfDays());
        assertEquals(new BigDecimal("300000.00"), line.getEnergyKwh());
        assertEquals(new BigDecimal("31.25"), line.getRatePerKwh());
        assertEquals(new BigDecimal("9375000.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 19: Boundary condition: Tariff change on the last day of the month")
    void testLastDayTariffChange() {
        Short folioNo = 1401;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("VT")
                .prvTariffRate(new BigDecimal("28.68"))
                .curTariffRate(new BigDecimal("31.25"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 9, 30))) // Sept 30
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(new BigDecimal("300000.0"))
                .billingMonth(YearMonth.of(2026, 9))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(2, result.getChargeLines().size());
        TariffChargeLine line1 = result.getChargeLines().get(0);
        assertEquals("Before Tariff Change", line1.getLabel());
        assertEquals(29, line1.getNumberOfDays());
        assertEquals(LocalDate.of(2026, 9, 1), line1.getPeriodStart());
        assertEquals(LocalDate.of(2026, 9, 29), line1.getPeriodEnd());

        TariffChargeLine line2 = result.getChargeLines().get(1);
        assertEquals("From Tariff Change Date", line2.getLabel());
        assertEquals(1, line2.getNumberOfDays());
        assertEquals(LocalDate.of(2026, 9, 30), line2.getPeriodStart());
        assertEquals(LocalDate.of(2026, 9, 30), line2.getPeriodEnd());
    }

    @Test
    @DisplayName("TEST 20: Leap-year February 29-day month calculation")
    void testFebruaryLeapYear_29Days() {
        Short folioNo = 1401;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("VT")
                .prvTariffRate(new BigDecimal("28.68"))
                .curTariffRate(new BigDecimal("31.25"))
                .tariffChanged(Date.valueOf(LocalDate.of(2024, 2, 15))) // Feb 15 in leap year 2024
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(new BigDecimal("290000.0"))
                .billingMonth(YearMonth.of(2024, 2)) // 29 days
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(2, result.getChargeLines().size());
        int beforeDays = result.getChargeLines().get(0).getNumberOfDays();
        int afterDays = result.getChargeLines().get(1).getNumberOfDays();
        assertEquals(14, beforeDays);
        assertEquals(15, afterDays);
        assertEquals(29, beforeDays + afterDays);
    }

    @Test
    @DisplayName("TEST 21: Non-leap-year February 28-day month calculation")
    void testFebruaryNonLeapYear_28Days() {
        Short folioNo = 1401;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("VT")
                .prvTariffRate(new BigDecimal("28.68"))
                .curTariffRate(new BigDecimal("31.25"))
                .tariffChanged(Date.valueOf(LocalDate.of(2023, 2, 15))) // Feb 15 in non-leap year 2023
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(new BigDecimal("280000.0"))
                .billingMonth(YearMonth.of(2023, 2)) // 28 days
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(2, result.getChargeLines().size());
        int beforeDays = result.getChargeLines().get(0).getNumberOfDays();
        int afterDays = result.getChargeLines().get(1).getNumberOfDays();
        assertEquals(14, beforeDays);
        assertEquals(14, afterDays);
        assertEquals(28, beforeDays + afterDays);
    }

    @Test
    @DisplayName("TEST 22: Error handling: Missing tariff configuration record in database throws ResponseStatusException")
    void testMissingTariffRateRecord_ThrowsException() {
        Short folioNo = 999;
        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.empty());

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertTrue(ex.getMessage().contains("No tariff rate configuration found for folio: 999"));
    }

    @Test
    @DisplayName("TEST 23: Error handling: Missing prv_tariff_rate when required throws ResponseStatusException")
    void testMissingPrvRate_ThrowsException() {
        Short folioNo = 1401;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("VT")
                .prvTariffRate(null) // missing
                .curTariffRate(new BigDecimal("31.25"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 9, 20)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .billingMonth(YearMonth.of(2026, 8)) // before changed date, requires prv
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertTrue(ex.getMessage().contains("Previous tariff rate (prv_tariff_rate) is missing"));
    }

    @Test
    @DisplayName("TEST 24: Error handling: Missing cur_tariff_rate when required throws ResponseStatusException")
    void testMissingCurRate_ThrowsException() {
        Short folioNo = 1401;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("VT")
                .prvTariffRate(new BigDecimal("28.68"))
                .curTariffRate(null) // missing
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 9, 20)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .billingMonth(YearMonth.of(2026, 10)) // after changed date, requires cur
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertTrue(ex.getMessage().contains("Current tariff rate (cur_tariff_rate) is missing"));
    }

    @Test
    @DisplayName("TEST 25: Error handling: Missing tariff_changed date throws ResponseStatusException")
    void testMissingTariffChangedDate_ThrowsException() {
        Short folioNo = 1401;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("VT")
                .prvTariffRate(new BigDecimal("28.68"))
                .curTariffRate(new BigDecimal("31.25"))
                .tariffChanged(null) // missing
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .billingMonth(YearMonth.of(2026, 9))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertTrue(ex.getMessage().contains("Tariff changed date is missing"));
    }

    @Test
    @DisplayName("TEST 26: Error handling: Null context, missing tariffType, missing folioNo")
    void testNullInputs_ThrowsException() {
        assertThrows(ResponseStatusException.class, () -> calculator.calculate(null));

        TariffCalculationContext ctxNoType = TariffCalculationContext.builder()
                .folioNo((short) 1401)
                .build();
        assertThrows(ResponseStatusException.class, () -> calculator.calculate(ctxNoType));

        TariffCalculationContext ctxNoFolio = TariffCalculationContext.builder()
                .tariffType("Variable Tariff")
                .build();
        assertThrows(ResponseStatusException.class, () -> calculator.calculate(ctxNoFolio));
    }

    @Test
    @DisplayName("TEST 27: Zero energy sent to grid produces zero cost and zero eligible energy")
    void testZeroEnergy_ProducesZeroCost() {
        Short folioNo = 1401;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("VT")
                .prvTariffRate(new BigDecimal("28.68"))
                .curTariffRate(new BigDecimal("31.25"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 9, 20)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(BigDecimal.ZERO)
                .billingMonth(YearMonth.of(2026, 10))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertEquals(new BigDecimal("0.00"), result.getEligibleEnergy());
        assertEquals(new BigDecimal("0.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 28: Generation loss exceeding energy floors at zero")
    void testGenerationLossExceedingEnergy_FloorsAtZero() {
        Short folioNo = 1401;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("VT")
                .prvTariffRate(new BigDecimal("28.68"))
                .curTariffRate(new BigDecimal("31.25"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 9, 20)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("Variable Tariff")
                .totalEnergySentToGrid(new BigDecimal("100.0"))
                .explicitLossKwh(new BigDecimal("500.0")) // Loss > energy
                .billingMonth(YearMonth.of(2026, 10))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertEquals(new BigDecimal("0.00"), result.getEligibleEnergy());
        assertEquals(new BigDecimal("0.00"), result.getCostOfEnergy());
    }
}
