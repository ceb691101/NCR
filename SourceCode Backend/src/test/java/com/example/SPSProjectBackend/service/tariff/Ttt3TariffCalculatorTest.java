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
class Ttt3TariffCalculatorTest {

    @Mock
    private NcreDevTariffRateRepository tariffRateRepository;

    private Ttt3TariffCalculator calculator;
    private Ttt5TariffCalculator calculatorTtt5;
    private FifteenPlus2019TariffCalculator calculator2019;
    private FifteenPlus2022TariffCalculator calculator2022;
    private FlatTariffCalculator flatCalculator;
    private TariffCalculatorResolver resolver;

    @BeforeEach
    void setUp() {
        calculator = new Ttt3TariffCalculator(tariffRateRepository);
        calculatorTtt5 = new Ttt5TariffCalculator(tariffRateRepository);
        calculator2019 = new FifteenPlus2019TariffCalculator(tariffRateRepository);
        calculator2022 = new FifteenPlus2022TariffCalculator(tariffRateRepository);
        flatCalculator = new FlatTariffCalculator(tariffRateRepository);
        resolver = new TariffCalculatorResolver(Arrays.asList(flatCalculator, calculator2019, calculator2022, calculatorTtt5, calculator));
    }

    @Test
    @DisplayName("TEST 1: supports('TTT3') and its real database variants return true")
    void testSupports_TTT3() {
        assertTrue(calculator.supports("TTT3"));
        assertTrue(calculator.supports("TTT3 "));
        assertTrue(calculator.supports(" TTT3"));
        assertTrue(calculator.supports("TTT (3 years)"));
        assertTrue(calculator.supports(" TTT (3 years) "));
        assertTrue(calculator.supports("TTT 3 Years"));
        assertTrue(calculator.supports("TTT (3 yrs)"));
        assertTrue(calculator.supports("TTT(3YRS)"));
        assertTrue(calculator.supports("TTT(3 YRS)"));
        assertTrue(calculator.supports("ttt3"));
    }

    @Test
    @DisplayName("TEST 2: Verify TTT3 calculator does not support TTT5")
    void testDoesNotSupportTTT5() {
        assertFalse(calculator.supports("TTT5"));
        assertFalse(calculator.supports("TTT (5 years)"));
        assertFalse(calculator.supports("TTT 5 Years"));
    }

    @Test
    @DisplayName("TEST 3: Verify TTT3 calculator does not support 15_2019")
    void testDoesNotSupport15_2019() {
        assertFalse(calculator.supports("15_2019"));
        assertFalse(calculator.supports("15+ (2019)"));
    }

    @Test
    @DisplayName("TEST 4: Verify TTT3 calculator does not support 15_2022")
    void testDoesNotSupport15_2022() {
        assertFalse(calculator.supports("15_2022"));
        assertFalse(calculator.supports("15+ New (2022)"));
    }

    @Test
    @DisplayName("TEST 5: Verify TTT3 calculator does not support FLAT, AC, or null")
    void testDoesNotSupportOtherTariffs() {
        assertFalse(calculator.supports("FLAT"));
        assertFalse(calculator.supports("AC"));
        assertFalse(calculator.supports("Variable Tariff"));
        assertFalse(calculator.supports(null));
        assertFalse(calculator.supports(""));
    }

    @Test
    @DisplayName("TEST 6: Resolver returns Ttt3TariffCalculator for TTT3")
    void testResolver_ResolvesToTtt3Calculator() {
        TariffCalculator resolved = resolver.resolve("TTT3");
        assertNotNull(resolved);
        assertTrue(resolved instanceof Ttt3TariffCalculator);
        assertSame(calculator, resolved);

        TariffCalculator resolvedDesc = resolver.resolve("TTT (3 years)");
        assertNotNull(resolvedDesc);
        assertSame(calculator, resolvedDesc);

        TariffCalculator resolvedYrs = resolver.resolve("TTT (3 yrs)");
        assertNotNull(resolvedYrs);
        assertSame(calculator, resolvedYrs);
    }

    @Test
    @DisplayName("TEST 7: billingMonth > tariffChangedMonth (tariffChangedMonth < billingMonth) -> cur_tariff_rate selected")
    void testBillingMonthAfterTariffChanged_SelectsCurRate() {
        Short folioNo = 49;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT3")
                .prvTariffRate(new BigDecimal("11.33"))
                .curTariffRate(new BigDecimal("12.50"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 3, 15))) // March 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT3")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .billingMonth(YearMonth.of(2026, 4)) // April 2026 (> March 2026)
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(new BigDecimal("12.50"), result.getSelectedTariffRate());
        assertEquals(TariffRateType.CUR_TARIFF_RATE, result.getSelectedRateType());
        assertTrue(result.isCurTariffRateSelected());
        assertFalse(result.isPrvTariffRateSelected());
        assertFalse(result.isMultiRateSelected());
        assertNull(result.getChargeLines());
        assertEquals(new BigDecimal("100000.0"), result.getOriginalEnergySentToGrid());
        assertEquals(new BigDecimal("100000.00"), result.getEligibleEnergy());
        assertEquals(new BigDecimal("1250000.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 8: billingMonth < tariffChangedMonth (tariffChangedMonth > billingMonth) -> prv_tariff_rate selected")
    void testBillingMonthBeforeTariffChanged_SelectsPrvRate() {
        Short folioNo = 49;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT3")
                .prvTariffRate(new BigDecimal("11.33"))
                .curTariffRate(new BigDecimal("12.50"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 3, 15))) // March 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT3")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .billingMonth(YearMonth.of(2026, 2)) // February 2026 (< March 2026)
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(new BigDecimal("11.33"), result.getSelectedTariffRate());
        assertEquals(TariffRateType.PRV_TARIFF_RATE, result.getSelectedRateType());
        assertTrue(result.isPrvTariffRateSelected());
        assertFalse(result.isCurTariffRateSelected());
        assertFalse(result.isMultiRateSelected());
        assertNull(result.getChargeLines());
        assertEquals(new BigDecimal("100000.0"), result.getOriginalEnergySentToGrid());
        assertEquals(new BigDecimal("100000.00"), result.getEligibleEnergy());
        assertEquals(new BigDecimal("1133000.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 9-18: Transition Month (tariffChangedMonth == billingMonth) full verification")
    void testTransitionMonth_ComprehensiveSplit() {
        Short folioNo = 1305;
        // Rathganga Hydro: changed May 10, 2026. May has 31 days.
        // beforeDays: 10 - 1 = 9 days (May 1 to May 9)
        // afterDays: 31 - 9 = 22 days (May 10 to May 31)
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT (3 yrs)")
                .prvTariffRate(new BigDecimal("37.22"))
                .curTariffRate(new BigDecimal("25.48"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 5, 10)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        // 100,000 kWh sent to grid, 2% generation loss = 2,000 kWh loss -> 98,000.00 kWh eligible
        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT3")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .generationLosses(new BigDecimal("2.0"))
                .billingMonth(YearMonth.of(2026, 5))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertTrue(result.isMultiRateSelected());
        assertNotNull(result.getChargeLines());
        assertEquals(2, result.getChargeLines().size());

        // 13. Days verification: beforeDays + afterDays == totalDays (9 + 22 == 31)
        int beforeDays = 9;
        int afterDays = 22;
        assertEquals(31, beforeDays + afterDays);

        // 14. Generation loss applied once before split
        assertEquals(new BigDecimal("100000.0"), result.getOriginalEnergySentToGrid());
        assertEquals(new BigDecimal("2000.00"), result.getGenerationLosses());
        assertEquals(new BigDecimal("98000.00"), result.getEligibleEnergy());

        // Line 1: Before Tariff Change (PRV)
        TariffChargeLine line1 = result.getChargeLines().get(0);
        assertEquals("Before Tariff Change", line1.getLabel());
        assertEquals(LocalDate.of(2026, 5, 1), line1.getPeriodStart());
        assertEquals(LocalDate.of(2026, 5, 9), line1.getPeriodEnd());
        assertEquals(9, line1.getNumberOfDays());
        assertEquals(TariffRateType.PRV_TARIFF_RATE, line1.getRateType());
        assertEquals(new BigDecimal("37.22"), line1.getRatePerKwh());

        // 15. Energy proration: 98000 * 9 / 31 = 28451.61
        BigDecimal expectedBeforeEnergy = new BigDecimal("28451.61");
        assertEquals(expectedBeforeEnergy, line1.getEnergyKwh());

        // 16. beforeCost = 28451.61 * 37.22 = 1058968.92
        BigDecimal expectedBeforeCost = new BigDecimal("1058968.92");
        assertEquals(expectedBeforeCost, line1.getCostOfEnergy());

        // Line 2: From Tariff Change Date (CUR)
        TariffChargeLine line2 = result.getChargeLines().get(1);
        assertEquals("From Tariff Change Date", line2.getLabel());
        assertEquals(LocalDate.of(2026, 5, 10), line2.getPeriodStart()); // 12. Change date belongs to after period
        assertEquals(LocalDate.of(2026, 5, 31), line2.getPeriodEnd());
        assertEquals(22, line2.getNumberOfDays());
        assertEquals(TariffRateType.CUR_TARIFF_RATE, line2.getRateType());
        assertEquals(new BigDecimal("25.48"), line2.getRatePerKwh());

        // afterEnergy: 98000 - 28451.61 = 69548.39
        BigDecimal expectedAfterEnergy = new BigDecimal("69548.39");
        assertEquals(expectedAfterEnergy, line2.getEnergyKwh());

        // 15. beforeEligibleEnergy + afterEligibleEnergy == eligibleEnergy
        assertEquals(result.getEligibleEnergy(), line1.getEnergyKwh().add(line2.getEnergyKwh()));

        // 17. afterCost = 69548.39 * 25.48 = 1772092.98
        BigDecimal expectedAfterCost = new BigDecimal("1772092.98");
        assertEquals(expectedAfterCost, line2.getCostOfEnergy());

        // 18. totalCost = beforeCost + afterCost = 1058968.92 + 1772092.98 = 2831061.90
        BigDecimal expectedTotalCost = new BigDecimal("2831061.90");
        assertEquals(expectedTotalCost, result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 19: Change on first day of month (beforeDays = 0) -> all energy to after period")
    void testTransitionMonth_ChangeOnDayOne() {
        Short folioNo = 1325;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT3")
                .prvTariffRate(new BigDecimal("37.22"))
                .curTariffRate(new BigDecimal("25.48"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 6, 1))) // June 1
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT3")
                .totalEnergySentToGrid(new BigDecimal("60000.0"))
                .billingMonth(YearMonth.of(2026, 6))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(1, result.getChargeLines().size());
        TariffChargeLine line = result.getChargeLines().get(0);
        assertEquals("From Tariff Change Date", line.getLabel());
        assertEquals(30, line.getNumberOfDays());
        assertEquals(new BigDecimal("60000.00"), line.getEnergyKwh());
        assertEquals(new BigDecimal("25.48"), line.getRatePerKwh());
        assertEquals(new BigDecimal("1528800.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 20: Change on final day of month (June 30: 29 days before, 1 day after)")
    void testTransitionMonth_ChangeOnFinalDay() {
        Short folioNo = 1341;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT3")
                .prvTariffRate(new BigDecimal("41.93"))
                .curTariffRate(new BigDecimal("30.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 6, 30))) // June 30
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT3")
                .totalEnergySentToGrid(new BigDecimal("30000.0"))
                .billingMonth(YearMonth.of(2026, 6))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(2, result.getChargeLines().size());
        TariffChargeLine line1 = result.getChargeLines().get(0);
        TariffChargeLine line2 = result.getChargeLines().get(1);

        assertEquals(29, line1.getNumberOfDays());
        assertEquals(1, line2.getNumberOfDays());
        assertEquals(new BigDecimal("29000.00"), line1.getEnergyKwh());
        assertEquals(new BigDecimal("1000.00"), line2.getEnergyKwh());
    }

    @Test
    @DisplayName("TEST 21: February non-leap year (28 days: changed Feb 10 -> 9 days before, 19 days after)")
    void testTransitionMonth_February28Days() {
        Short folioNo = 1349;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT3")
                .prvTariffRate(new BigDecimal("37.22"))
                .curTariffRate(new BigDecimal("25.48"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 2, 10))) // Feb 10, 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT3")
                .totalEnergySentToGrid(new BigDecimal("28000.0"))
                .billingMonth(YearMonth.of(2026, 2))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(2, result.getChargeLines().size());
        assertEquals(9, result.getChargeLines().get(0).getNumberOfDays());
        assertEquals(19, result.getChargeLines().get(1).getNumberOfDays());
        assertEquals(new BigDecimal("9000.00"), result.getChargeLines().get(0).getEnergyKwh());
        assertEquals(new BigDecimal("19000.00"), result.getChargeLines().get(1).getEnergyKwh());
    }

    @Test
    @DisplayName("TEST 22: February leap year (2028, 29 days: changed Feb 10 -> 9 days before, 20 days after)")
    void testTransitionMonth_February29Days_LeapYear() {
        Short folioNo = 1353;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT3")
                .prvTariffRate(new BigDecimal("36.24"))
                .curTariffRate(new BigDecimal("24.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2028, 2, 10))) // Feb 10, 2028
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT3")
                .totalEnergySentToGrid(new BigDecimal("29000.0"))
                .billingMonth(YearMonth.of(2028, 2))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(2, result.getChargeLines().size());
        assertEquals(9, result.getChargeLines().get(0).getNumberOfDays());
        assertEquals(20, result.getChargeLines().get(1).getNumberOfDays());
        assertEquals(new BigDecimal("9000.00"), result.getChargeLines().get(0).getEnergyKwh());
        assertEquals(new BigDecimal("20000.00"), result.getChargeLines().get(1).getEnergyKwh());
    }

    @Test
    @DisplayName("TEST 23: Missing tariff configuration record throws BAD_REQUEST")
    void testMissingTariffConfig_ThrowsBadRequest() {
        Short folioNo = 999;
        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.empty());

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT3")
                .totalEnergySentToGrid(new BigDecimal("10000.0"))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("No tariff rate configuration found"));
    }

    @Test
    @DisplayName("TEST 24: Missing prv_tariff_rate in transition month throws BAD_REQUEST")
    void testMissingPrvRate_ThrowsBadRequest() {
        Short folioNo = 1385;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT3")
                .prvTariffRate(null) // missing
                .curTariffRate(new BigDecimal("25.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 5, 10)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT3")
                .totalEnergySentToGrid(new BigDecimal("10000.0"))
                .billingMonth(YearMonth.of(2026, 5))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("prv_tariff_rate"));
    }

    @Test
    @DisplayName("TEST 25: Missing cur_tariff_rate in transition month throws BAD_REQUEST")
    void testMissingCurRate_ThrowsBadRequest() {
        Short folioNo = 1389;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT3")
                .prvTariffRate(new BigDecimal("35.87"))
                .curTariffRate(null) // missing
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 5, 10)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT3")
                .totalEnergySentToGrid(new BigDecimal("10000.0"))
                .billingMonth(YearMonth.of(2026, 5))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("cur_tariff_rate"));
    }

    @Test
    @DisplayName("TEST 26: Missing tariff_changed date throws BAD_REQUEST")
    void testMissingTariffChangedDate_ThrowsBadRequest() {
        Short folioNo = 1397;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT3")
                .prvTariffRate(new BigDecimal("35.54"))
                .curTariffRate(new BigDecimal("25.00"))
                .tariffChanged(null) // missing
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT3")
                .totalEnergySentToGrid(new BigDecimal("10000.0"))
                .billingMonth(YearMonth.of(2026, 5))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Tariff changed date is missing"));
    }

    @Test
    @DisplayName("TEST 27: Null context throws BAD_REQUEST")
    void testNullContext_ThrowsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(null));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("TEST 28: Null or empty tariffType throws BAD_REQUEST")
    void testMissingTariffType_ThrowsBadRequest() {
        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo((short) 100)
                .tariffType("")
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("TEST 29: Null folioNo throws BAD_REQUEST")
    void testMissingFolioNo_ThrowsBadRequest() {
        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(null)
                .tariffType("TTT3")
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("TEST 30: Generation loss exceeding energy clamps eligible energy to zero")
    void testLossExceedingEnergy_ClampsToZero() {
        Short folioNo = 1409;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT3")
                .prvTariffRate(new BigDecimal("35.54"))
                .curTariffRate(new BigDecimal("25.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 3, 10)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT3")
                .totalEnergySentToGrid(new BigDecimal("100.0"))
                .explicitLossKwh(new BigDecimal("500.0"))
                .billingMonth(YearMonth.of(2026, 3))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertEquals(BigDecimal.ZERO.setScale(2), result.getEligibleEnergy());
        assertEquals(BigDecimal.ZERO.setScale(2), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 31: Fractional generation loss (0.03 = 3%) applied correctly")
    void testFractionalGenerationLoss() {
        Short folioNo = 1437;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT3")
                .prvTariffRate(new BigDecimal("37.46"))
                .curTariffRate(new BigDecimal("25.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 1, 1))) // in past
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT3")
                .totalEnergySentToGrid(new BigDecimal("10000.0"))
                .generationLosses(new BigDecimal("0.03")) // 3% as fraction
                .billingMonth(YearMonth.of(2026, 6))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertEquals(new BigDecimal("300.00"), result.getGenerationLosses());
        assertEquals(new BigDecimal("9700.00"), result.getEligibleEnergy());
        assertEquals(new BigDecimal("242500.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 32: Multi-rate effective rate is computed accurately for aggregate rate_per_kwh")
    void testTransitionMonth_EffectiveRate() {
        Short folioNo = 1469;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT3")
                .prvTariffRate(new BigDecimal("30.00"))
                .curTariffRate(new BigDecimal("10.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 4, 16))) // April 16 (15 days before, 15 days after in 30-day month)
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT3")
                .totalEnergySentToGrid(new BigDecimal("2000.0"))
                .billingMonth(YearMonth.of(2026, 4))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        // 1000 kWh @ 30.00 = 30000; 1000 kWh @ 10.00 = 10000; total = 40000
        // Effective rate = 40000 / 2000 = 20.00
        assertEquals(new BigDecimal("20.00"), result.getSelectedTariffRate());
        assertEquals(TariffRateType.MULTI_RATE, result.getSelectedRateType());
        assertEquals(new BigDecimal("40000.00"), result.getCostOfEnergy());
    }
}
