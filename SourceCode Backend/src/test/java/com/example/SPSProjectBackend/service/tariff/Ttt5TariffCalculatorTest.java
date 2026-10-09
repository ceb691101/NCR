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
class Ttt5TariffCalculatorTest {

    @Mock
    private NcreDevTariffRateRepository tariffRateRepository;

    private Ttt5TariffCalculator calculator;
    private FifteenPlus2019TariffCalculator calculator2019;
    private FifteenPlus2022TariffCalculator calculator2022;
    private FlatTariffCalculator flatCalculator;
    private TariffCalculatorResolver resolver;

    @BeforeEach
    void setUp() {
        calculator = new Ttt5TariffCalculator(tariffRateRepository);
        calculator2019 = new FifteenPlus2019TariffCalculator(tariffRateRepository);
        calculator2022 = new FifteenPlus2022TariffCalculator(tariffRateRepository);
        flatCalculator = new FlatTariffCalculator(tariffRateRepository);
        resolver = new TariffCalculatorResolver(Arrays.asList(flatCalculator, calculator2019, calculator2022, calculator));
    }

    @Test
    @DisplayName("TEST 1: supports('TTT5'), supports('TTT (5 years)'), supports('TTT 5 Years') returns true")
    void testSupports_TTT5() {
        assertTrue(calculator.supports("TTT5"));
        assertTrue(calculator.supports("TTT5 "));
        assertTrue(calculator.supports(" TTT5"));
        assertTrue(calculator.supports("TTT (5 years)"));
        assertTrue(calculator.supports(" TTT (5 years) "));
        assertTrue(calculator.supports("TTT 5 Years"));
        assertTrue(calculator.supports("ttt5"));
    }

    @Test
    @DisplayName("TEST 2: Verify TTT5 calculator does not support 15_2019")
    void testDoesNotSupport15_2019() {
        assertFalse(calculator.supports("15_2019"));
        assertFalse(calculator.supports("15+ (2019)"));
    }

    @Test
    @DisplayName("TEST 3: Verify TTT5 calculator does not support 15_2022")
    void testDoesNotSupport15_2022() {
        assertFalse(calculator.supports("15_2022"));
        assertFalse(calculator.supports("15+ New (2022)"));
    }

    @Test
    @DisplayName("TEST 4: Verify TTT5 calculator does not support FLAT, TTT3, or null")
    void testDoesNotSupportOtherTariffs() {
        assertFalse(calculator.supports("FLAT"));
        assertFalse(calculator.supports("TTT3"));
        assertFalse(calculator.supports("TTT (3 years)"));
        assertFalse(calculator.supports("TTT (3 yrs)"));
        assertFalse(calculator.supports("Variable Tariff"));
        assertFalse(calculator.supports(null));
        assertFalse(calculator.supports(""));
    }

    @Test
    @DisplayName("TEST 5: Resolver with TTT5 resolves to Ttt5TariffCalculator")
    void testResolver_ResolvesToTtt5Calculator() {
        TariffCalculator resolved = resolver.resolve("TTT5");
        assertNotNull(resolved);
        assertTrue(resolved instanceof Ttt5TariffCalculator);
        assertSame(calculator, resolved);

        TariffCalculator resolvedDesc = resolver.resolve("TTT (5 years)");
        assertNotNull(resolvedDesc);
        assertSame(calculator, resolvedDesc);

        TariffCalculator resolvedAlt = resolver.resolve("TTT 5 Years");
        assertNotNull(resolvedAlt);
        assertSame(calculator, resolvedAlt);
    }

    @Test
    @DisplayName("TEST 6: billingMonth > tariffChangedMonth (tariffChangedMonth < billingMonth) -> cur_tariff_rate selected")
    void testBillingMonthAfterTariffChanged_SelectsCurRate() {
        Short folioNo = 881;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT5")
                .prvTariffRate(new BigDecimal("19.51"))
                .curTariffRate(new BigDecimal("9.93"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 3, 29))) // March 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT5")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .billingMonth(YearMonth.of(2026, 4)) // April 2026 (> March 2026)
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(new BigDecimal("9.93"), result.getSelectedTariffRate());
        assertEquals(TariffRateType.CUR_TARIFF_RATE, result.getSelectedRateType());
        assertTrue(result.isCurTariffRateSelected());
        assertFalse(result.isPrvTariffRateSelected());
        assertFalse(result.isMultiRateSelected());
        assertNull(result.getChargeLines());
        assertEquals(new BigDecimal("100000.0"), result.getOriginalEnergySentToGrid());
        assertEquals(new BigDecimal("100000.00"), result.getEligibleEnergy());
        assertEquals(new BigDecimal("993000.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 7: billingMonth < tariffChangedMonth (tariffChangedMonth > billingMonth) -> prv_tariff_rate selected")
    void testBillingMonthBeforeTariffChanged_SelectsPrvRate() {
        Short folioNo = 881;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT5")
                .prvTariffRate(new BigDecimal("19.51"))
                .curTariffRate(new BigDecimal("9.93"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 3, 29))) // March 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT5")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .billingMonth(YearMonth.of(2026, 2)) // February 2026 (< March 2026)
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(new BigDecimal("19.51"), result.getSelectedTariffRate());
        assertEquals(TariffRateType.PRV_TARIFF_RATE, result.getSelectedRateType());
        assertTrue(result.isPrvTariffRateSelected());
        assertFalse(result.isCurTariffRateSelected());
        assertFalse(result.isMultiRateSelected());
        assertNull(result.getChargeLines());
        assertEquals(new BigDecimal("100000.0"), result.getOriginalEnergySentToGrid());
        assertEquals(new BigDecimal("100000.00"), result.getEligibleEnergy());
        assertEquals(new BigDecimal("1951000.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 8-17: Transition Month (tariffChangedMonth == billingMonth) full verification")
    void testTransitionMonth_ComprehensiveSplit() {
        Short folioNo = 901;
        // Access Hydro Power: changed July 20, 2026. July has 31 days.
        // beforeDays: 20 - 1 = 19 days (July 1 to July 19)
        // afterDays: 31 - 19 = 12 days (July 20 to July 31)
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT5")
                .prvTariffRate(new BigDecimal("19.51"))
                .curTariffRate(new BigDecimal("9.93"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 7, 20)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        // 100,000 kWh sent to grid, 5% generation loss = 5,000 kWh loss -> 95,000.00 kWh eligible
        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT5")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .generationLosses(new BigDecimal("5.0"))
                .billingMonth(YearMonth.of(2026, 7))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertTrue(result.isMultiRateSelected());
        assertNotNull(result.getChargeLines());
        assertEquals(2, result.getChargeLines().size());

        // 12. Days verification
        int beforeDays = 19;
        int afterDays = 12;
        assertEquals(31, beforeDays + afterDays);

        // 14. Generation loss applied once before split
        assertEquals(new BigDecimal("100000.0"), result.getOriginalEnergySentToGrid());
        assertEquals(new BigDecimal("5000.00"), result.getGenerationLosses());
        assertEquals(new BigDecimal("95000.00"), result.getEligibleEnergy());

        // Line 1: Before Tariff Change (PRV)
        TariffChargeLine line1 = result.getChargeLines().get(0);
        assertEquals("Before Tariff Change", line1.getLabel());
        assertEquals(LocalDate.of(2026, 7, 1), line1.getPeriodStart());
        assertEquals(LocalDate.of(2026, 7, 19), line1.getPeriodEnd());
        assertEquals(19, line1.getNumberOfDays());
        assertEquals(TariffRateType.PRV_TARIFF_RATE, line1.getRateType());
        assertEquals(new BigDecimal("19.51"), line1.getRatePerKwh());

        // 13. Energy proration: 95000 * 19 / 31 = 58225.81
        BigDecimal expectedBeforeEnergy = new BigDecimal("58225.81");
        assertEquals(expectedBeforeEnergy, line1.getEnergyKwh());

        // 15. beforeCost = 58225.81 * 19.51 = 1135985.55
        BigDecimal expectedBeforeCost = new BigDecimal("1135985.55");
        assertEquals(expectedBeforeCost, line1.getCostOfEnergy());

        // Line 2: From Tariff Change Date (CUR)
        TariffChargeLine line2 = result.getChargeLines().get(1);
        assertEquals("From Tariff Change Date", line2.getLabel());
        assertEquals(LocalDate.of(2026, 7, 20), line2.getPeriodStart()); // 11. Change date belongs to after period
        assertEquals(LocalDate.of(2026, 7, 31), line2.getPeriodEnd());
        assertEquals(12, line2.getNumberOfDays());
        assertEquals(TariffRateType.CUR_TARIFF_RATE, line2.getRateType());
        assertEquals(new BigDecimal("9.93"), line2.getRatePerKwh());

        // afterEnergy: 95000 - 58225.81 = 36774.19
        BigDecimal expectedAfterEnergy = new BigDecimal("36774.19");
        assertEquals(expectedAfterEnergy, line2.getEnergyKwh());

        // 13. beforeEligibleEnergy + afterEligibleEnergy == eligibleEnergy
        assertEquals(result.getEligibleEnergy(), line1.getEnergyKwh().add(line2.getEnergyKwh()));

        // 16. afterCost = 36774.19 * 9.93 = 365167.71
        BigDecimal expectedAfterCost = new BigDecimal("365167.71");
        assertEquals(expectedAfterCost, line2.getCostOfEnergy());

        // 17. totalCost = beforeCost + afterCost = 1135985.55 + 365167.71 = 1501153.26
        BigDecimal expectedTotalCost = new BigDecimal("1501153.26");
        assertEquals(expectedTotalCost, result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 18: Change on first day of month (beforeDays = 0) -> all energy to after period")
    void testTransitionMonth_ChangeOnDayOne() {
        Short folioNo = 913;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT5")
                .prvTariffRate(new BigDecimal("19.51"))
                .curTariffRate(new BigDecimal("9.93"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 8, 1))) // August 1
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT5")
                .totalEnergySentToGrid(new BigDecimal("50000.0"))
                .billingMonth(YearMonth.of(2026, 8))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(1, result.getChargeLines().size());
        TariffChargeLine line = result.getChargeLines().get(0);
        assertEquals("From Tariff Change Date", line.getLabel());
        assertEquals(31, line.getNumberOfDays());
        assertEquals(new BigDecimal("50000.00"), line.getEnergyKwh());
        assertEquals(new BigDecimal("9.93"), line.getRatePerKwh());
        assertEquals(new BigDecimal("496500.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 19: Change on final day of month (August 31: 30 days before, 1 day after)")
    void testTransitionMonth_ChangeOnFinalDay() {
        Short folioNo = 917;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT5")
                .prvTariffRate(new BigDecimal("19.51"))
                .curTariffRate(new BigDecimal("9.93"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 8, 31))) // August 31
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT5")
                .totalEnergySentToGrid(new BigDecimal("31000.0"))
                .billingMonth(YearMonth.of(2026, 8))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(2, result.getChargeLines().size());
        TariffChargeLine line1 = result.getChargeLines().get(0);
        TariffChargeLine line2 = result.getChargeLines().get(1);

        assertEquals(30, line1.getNumberOfDays());
        assertEquals(1, line2.getNumberOfDays());
        assertEquals(new BigDecimal("30000.00"), line1.getEnergyKwh());
        assertEquals(new BigDecimal("1000.00"), line2.getEnergyKwh());
    }

    @Test
    @DisplayName("TEST 20: February non-leap year (28 days: changed Feb 15 -> 14 days before, 14 days after)")
    void testTransitionMonth_February28Days() {
        Short folioNo = 925;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT5")
                .prvTariffRate(new BigDecimal("19.51"))
                .curTariffRate(new BigDecimal("9.93"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 2, 15))) // Feb 15, 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT5")
                .totalEnergySentToGrid(new BigDecimal("28000.0"))
                .billingMonth(YearMonth.of(2026, 2))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(2, result.getChargeLines().size());
        assertEquals(14, result.getChargeLines().get(0).getNumberOfDays());
        assertEquals(14, result.getChargeLines().get(1).getNumberOfDays());
        assertEquals(new BigDecimal("14000.00"), result.getChargeLines().get(0).getEnergyKwh());
        assertEquals(new BigDecimal("14000.00"), result.getChargeLines().get(1).getEnergyKwh());
    }

    @Test
    @DisplayName("TEST 21: February leap year (2028, 29 days: changed Feb 15 -> 14 days before, 15 days after)")
    void testTransitionMonth_February29Days_LeapYear() {
        Short folioNo = 929;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT5")
                .prvTariffRate(new BigDecimal("44.09"))
                .curTariffRate(new BigDecimal("38.14"))
                .tariffChanged(Date.valueOf(LocalDate.of(2028, 2, 15))) // Feb 15, 2028
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT5")
                .totalEnergySentToGrid(new BigDecimal("29000.0"))
                .billingMonth(YearMonth.of(2028, 2))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(2, result.getChargeLines().size());
        assertEquals(14, result.getChargeLines().get(0).getNumberOfDays());
        assertEquals(15, result.getChargeLines().get(1).getNumberOfDays());
        assertEquals(new BigDecimal("14000.00"), result.getChargeLines().get(0).getEnergyKwh());
        assertEquals(new BigDecimal("15000.00"), result.getChargeLines().get(1).getEnergyKwh());
    }

    @Test
    @DisplayName("TEST 22: Missing tariff configuration record throws BAD_REQUEST")
    void testMissingTariffConfig_ThrowsBadRequest() {
        Short folioNo = 999;
        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.empty());

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT5")
                .totalEnergySentToGrid(new BigDecimal("10000.0"))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("No tariff rate configuration found"));
    }

    @Test
    @DisplayName("TEST 23: Missing prv_tariff_rate in transition month throws BAD_REQUEST")
    void testMissingPrvRate_ThrowsBadRequest() {
        Short folioNo = 933;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT5")
                .prvTariffRate(null) // missing
                .curTariffRate(new BigDecimal("9.93"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 5, 10)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT5")
                .totalEnergySentToGrid(new BigDecimal("10000.0"))
                .billingMonth(YearMonth.of(2026, 5))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("prv_tariff_rate"));
    }

    @Test
    @DisplayName("TEST 24: Missing cur_tariff_rate in transition month throws BAD_REQUEST")
    void testMissingCurRate_ThrowsBadRequest() {
        Short folioNo = 941;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT5")
                .prvTariffRate(new BigDecimal("19.51"))
                .curTariffRate(null) // missing
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 5, 10)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT5")
                .totalEnergySentToGrid(new BigDecimal("10000.0"))
                .billingMonth(YearMonth.of(2026, 5))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("cur_tariff_rate"));
    }

    @Test
    @DisplayName("TEST 25: Missing tariff_changed date throws BAD_REQUEST")
    void testMissingTariffChangedDate_ThrowsBadRequest() {
        Short folioNo = 953;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT5")
                .prvTariffRate(new BigDecimal("19.51"))
                .curTariffRate(new BigDecimal("9.93"))
                .tariffChanged(null) // missing
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT5")
                .totalEnergySentToGrid(new BigDecimal("10000.0"))
                .billingMonth(YearMonth.of(2026, 5))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Tariff changed date is missing"));
    }

    @Test
    @DisplayName("TEST 26: Null context throws BAD_REQUEST")
    void testNullContext_ThrowsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(null));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("TEST 27: Null or empty tariffType throws BAD_REQUEST")
    void testMissingTariffType_ThrowsBadRequest() {
        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo((short) 100)
                .tariffType("")
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("TEST 28: Null folioNo throws BAD_REQUEST")
    void testMissingFolioNo_ThrowsBadRequest() {
        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(null)
                .tariffType("TTT5")
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("TEST 29: Generation loss exceeding energy clamps eligible energy to zero")
    void testLossExceedingEnergy_ClampsToZero() {
        Short folioNo = 961;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT5")
                .prvTariffRate(new BigDecimal("19.51"))
                .curTariffRate(new BigDecimal("9.93"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 3, 10)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT5")
                .totalEnergySentToGrid(new BigDecimal("100.0"))
                .explicitLossKwh(new BigDecimal("500.0")) // greater than energy
                .billingMonth(YearMonth.of(2026, 3))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertEquals(BigDecimal.ZERO.setScale(2), result.getEligibleEnergy());
        assertEquals(BigDecimal.ZERO.setScale(2), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 30: Fractional generation loss (0.02 = 2%) applied correctly")
    void testFractionalGenerationLoss() {
        Short folioNo = 965;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT5")
                .prvTariffRate(new BigDecimal("19.51"))
                .curTariffRate(new BigDecimal("9.93"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 1, 1))) // in past
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT5")
                .totalEnergySentToGrid(new BigDecimal("10000.0"))
                .generationLosses(new BigDecimal("0.02")) // 2% as fraction
                .billingMonth(YearMonth.of(2026, 6))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertEquals(new BigDecimal("200.00"), result.getGenerationLosses());
        assertEquals(new BigDecimal("9800.00"), result.getEligibleEnergy());
        assertEquals(new BigDecimal("97314.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 31: Multi-rate effective rate is computed accurately for aggregate rate_per_kwh")
    void testTransitionMonth_EffectiveRate() {
        Short folioNo = 969;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("TTT5")
                .prvTariffRate(new BigDecimal("20.00"))
                .curTariffRate(new BigDecimal("10.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 4, 16))) // April 16 (15 days before, 15 days after in 30-day month)
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("TTT5")
                .totalEnergySentToGrid(new BigDecimal("2000.0"))
                .billingMonth(YearMonth.of(2026, 4))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        // 1000 kWh @ 20.00 = 20000; 1000 kWh @ 10.00 = 10000; total = 30000
        // Effective rate = 30000 / 2000 = 15.00
        assertEquals(new BigDecimal("15.00"), result.getSelectedTariffRate());
        assertEquals(TariffRateType.MULTI_RATE, result.getSelectedRateType());
        assertEquals(new BigDecimal("30000.00"), result.getCostOfEnergy());
    }
}
