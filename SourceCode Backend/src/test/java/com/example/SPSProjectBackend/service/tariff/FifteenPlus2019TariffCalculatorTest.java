package com.example.SPSProjectBackend.service.tariff;

import com.example.SPSProjectBackend.model.NcreDevTariffRate;
import com.example.SPSProjectBackend.repository.NcreDevTariffRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FifteenPlus2019TariffCalculatorTest {

    @Mock
    private NcreDevTariffRateRepository tariffRateRepository;

    private FifteenPlus2019TariffCalculator calculator;
    private FlatTariffCalculator flatCalculator;
    private TariffCalculatorResolver resolver;

    @BeforeEach
    void setUp() {
        calculator = new FifteenPlus2019TariffCalculator(tariffRateRepository);
        flatCalculator = new FlatTariffCalculator(tariffRateRepository);
        resolver = new TariffCalculatorResolver(Arrays.asList(flatCalculator, calculator));
    }

    @Test
    @DisplayName("TEST 1: current bill cycle month > change date month -> cur_tariff_rate selected")
    void testBillingMonthAfterTariffChanged_SelectsCurRate() {
        Short folioNo = 201;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2019")
                .prvTariffRate(new BigDecimal("10.55"))
                .curTariffRate(new BigDecimal("10.86"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 7, 15))) // July 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2019")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .billingMonth(YearMonth.of(2026, 8)) // August 2026 (> July 2026)
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(new BigDecimal("10.86"), result.getSelectedTariffRate());
        assertEquals(TariffRateType.CUR_TARIFF_RATE, result.getSelectedRateType());
        assertTrue(result.isCurTariffRateSelected());
        assertFalse(result.isPrvTariffRateSelected());
        assertFalse(result.isMultiRateSelected());
        assertNull(result.getChargeLines());
        assertEquals(new BigDecimal("100000.0"), result.getOriginalEnergySentToGrid());
        assertEquals(new BigDecimal("100000.00"), result.getEligibleEnergy());
        assertEquals(new BigDecimal("1086000.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 2: current bill cycle month < change date month -> prv_tariff_rate selected")
    void testBillingMonthBeforeTariffChanged_SelectsPrvRate() {
        Short folioNo = 202;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2019")
                .prvTariffRate(new BigDecimal("10.55"))
                .curTariffRate(new BigDecimal("10.86"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 9, 10))) // September 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2019")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .billingMonth(YearMonth.of(2026, 8)) // August 2026 (< September 2026)
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(new BigDecimal("10.55"), result.getSelectedTariffRate());
        assertEquals(TariffRateType.PRV_TARIFF_RATE, result.getSelectedRateType());
        assertTrue(result.isPrvTariffRateSelected());
        assertFalse(result.isCurTariffRateSelected());
        assertFalse(result.isMultiRateSelected());
        assertNull(result.getChargeLines());
        assertEquals(new BigDecimal("1055000.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 3: Transition Month 31 Days (March 2026, changed March 15) -> Prorating and Charge Lines")
    void testTransitionMonth_31Days_SplitMarch15() {
        Short folioNo = 203;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2019")
                .prvTariffRate(new BigDecimal("17.48"))
                .curTariffRate(new BigDecimal("16.92"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 3, 15))) // March 15, 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        // 100,000 kWh eligible energy in 31-day month
        // beforeDays = 15 - 1 = 14 (March 1 to 14)
        // afterDays = 31 - 14 = 17 (March 15 to 31)
        // beforeEnergy = 100,000 * 14 / 31 = 45161.29
        // afterEnergy = 100,000 - 45161.29 = 54838.71 (Exact conservation)
        // beforeCost = 45161.29 * 17.48 = 789419.35
        // afterCost = 54838.71 * 16.92 = 927870.97
        // totalCost = 789419.35 + 927870.97 = 1717290.32
        // effectiveRate = 1717290.32 / 100000 = 17.17
        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2019")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .billingMonth(YearMonth.of(2026, 3))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(TariffRateType.MULTI_RATE, result.getSelectedRateType());
        assertTrue(result.isMultiRateSelected());
        assertEquals(new BigDecimal("100000.00"), result.getEligibleEnergy());
        assertEquals(new BigDecimal("1717290.32"), result.getCostOfEnergy());
        assertEquals(new BigDecimal("17.17"), result.getSelectedTariffRate());

        List<TariffChargeLine> lines = result.getChargeLines();
        assertNotNull(lines);
        assertEquals(2, lines.size());

        TariffChargeLine line1 = lines.get(0);
        assertEquals("Before Tariff Change", line1.getLabel());
        assertEquals(LocalDate.of(2026, 3, 1), line1.getPeriodStart());
        assertEquals(LocalDate.of(2026, 3, 14), line1.getPeriodEnd());
        assertEquals(14, line1.getNumberOfDays());
        assertEquals(new BigDecimal("45161.29"), line1.getEnergyKwh());
        assertEquals(new BigDecimal("17.48"), line1.getRatePerKwh());
        assertEquals(new BigDecimal("789419.35"), line1.getCostOfEnergy());
        assertEquals(TariffRateType.PRV_TARIFF_RATE, line1.getRateType());

        TariffChargeLine line2 = lines.get(1);
        assertEquals("From Tariff Change Date", line2.getLabel());
        assertEquals(LocalDate.of(2026, 3, 15), line2.getPeriodStart());
        assertEquals(LocalDate.of(2026, 3, 31), line2.getPeriodEnd());
        assertEquals(17, line2.getNumberOfDays());
        assertEquals(new BigDecimal("54838.71"), line2.getEnergyKwh());
        assertEquals(new BigDecimal("16.92"), line2.getRatePerKwh());
        assertEquals(new BigDecimal("927870.97"), line2.getCostOfEnergy());
        assertEquals(TariffRateType.CUR_TARIFF_RATE, line2.getRateType());

        // Verify exact energy sum matches total eligible
        assertEquals(result.getEligibleEnergy(), line1.getEnergyKwh().add(line2.getEnergyKwh()));
        // Verify cost sum matches total cost
        assertEquals(result.getCostOfEnergy(), line1.getCostOfEnergy().add(line2.getCostOfEnergy()));
    }

    @Test
    @DisplayName("TEST 4: Transition Month 30 Days (April 2026, changed April 11) -> 10 and 20 Days")
    void testTransitionMonth_30Days_SplitApril11() {
        Short folioNo = 204;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2019")
                .prvTariffRate(new BigDecimal("15.00"))
                .curTariffRate(new BigDecimal("18.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 4, 11))) // April 11, 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        // 30,000 kWh eligible energy in 30-day month
        // beforeDays = 11 - 1 = 10 (April 1 to 10)
        // afterDays = 30 - 10 = 20 (April 11 to 30)
        // beforeEnergy = 30,000 * 10 / 30 = 10,000.00
        // afterEnergy = 30,000 - 10,000 = 20,000.00
        // beforeCost = 10,000.00 * 15.00 = 150,000.00
        // afterCost = 20,000.00 * 18.00 = 360,000.00
        // totalCost = 510,000.00
        // effectiveRate = 510,000.00 / 30,000 = 17.00
        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2019")
                .totalEnergySentToGrid(new BigDecimal("30000.0"))
                .billingMonth(YearMonth.of(2026, 4))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(TariffRateType.MULTI_RATE, result.getSelectedRateType());
        assertEquals(new BigDecimal("30000.00"), result.getEligibleEnergy());
        assertEquals(new BigDecimal("510000.00"), result.getCostOfEnergy());
        assertEquals(new BigDecimal("17.00"), result.getSelectedTariffRate());

        List<TariffChargeLine> lines = result.getChargeLines();
        assertEquals(2, lines.size());
        assertEquals(10, lines.get(0).getNumberOfDays());
        assertEquals(new BigDecimal("10000.00"), lines.get(0).getEnergyKwh());
        assertEquals(20, lines.get(1).getNumberOfDays());
        assertEquals(new BigDecimal("20000.00"), lines.get(1).getEnergyKwh());
    }

    @Test
    @DisplayName("TEST 5: Transition Month February 28 Days Non-Leap Year (Feb 2026, changed Feb 15)")
    void testTransitionMonth_February28Days_NonLeapYear() {
        Short folioNo = 205;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2019")
                .prvTariffRate(new BigDecimal("12.00"))
                .curTariffRate(new BigDecimal("14.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 2, 15))) // Feb 15, 2026 (28 days total)
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        // beforeDays = 15 - 1 = 14 (Feb 1 to 14)
        // afterDays = 28 - 14 = 14 (Feb 15 to 28)
        // exactly 50/50 split!
        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2019")
                .totalEnergySentToGrid(new BigDecimal("28000.0"))
                .billingMonth(YearMonth.of(2026, 2))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(new BigDecimal("28000.00"), result.getEligibleEnergy());
        List<TariffChargeLine> lines = result.getChargeLines();
        assertEquals(2, lines.size());
        assertEquals(14, lines.get(0).getNumberOfDays());
        assertEquals(new BigDecimal("14000.00"), lines.get(0).getEnergyKwh());
        assertEquals(14, lines.get(1).getNumberOfDays());
        assertEquals(new BigDecimal("14000.00"), lines.get(1).getEnergyKwh());
        // beforeCost = 14000 * 12 = 168,000; afterCost = 14000 * 14 = 196,000; total = 364,000
        assertEquals(new BigDecimal("364000.00"), result.getCostOfEnergy());
        assertEquals(new BigDecimal("13.00"), result.getSelectedTariffRate());
    }

    @Test
    @DisplayName("TEST 6: Transition Month February 29 Days Leap Year (Feb 2028, changed Feb 15)")
    void testTransitionMonth_February29Days_LeapYear() {
        Short folioNo = 206;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2019")
                .prvTariffRate(new BigDecimal("10.00"))
                .curTariffRate(new BigDecimal("20.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2028, 2, 15))) // 2028 is leap year (29 days)
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        // beforeDays = 14, afterDays = 15, total = 29
        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2019")
                .totalEnergySentToGrid(new BigDecimal("29000.0"))
                .billingMonth(YearMonth.of(2028, 2))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        List<TariffChargeLine> lines = result.getChargeLines();
        assertEquals(2, lines.size());
        assertEquals(14, lines.get(0).getNumberOfDays());
        assertEquals(new BigDecimal("14000.00"), lines.get(0).getEnergyKwh());
        assertEquals(15, lines.get(1).getNumberOfDays());
        assertEquals(new BigDecimal("15000.00"), lines.get(1).getEnergyKwh());
        assertEquals(LocalDate.of(2028, 2, 29), lines.get(1).getPeriodEnd());
    }

    @Test
    @DisplayName("TEST 7: Boundary Condition - Tariff Change on Day 1 (beforeDays = 0, all energy to curRate)")
    void testTransitionMonth_ChangeOnDay1() {
        Short folioNo = 207;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2019")
                .prvTariffRate(new BigDecimal("10.00"))
                .curTariffRate(new BigDecimal("15.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 3, 1))) // Day 1 of March
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2019")
                .totalEnergySentToGrid(new BigDecimal("31000.0"))
                .billingMonth(YearMonth.of(2026, 3))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        List<TariffChargeLine> lines = result.getChargeLines();
        // Since beforeDays = 0, only line 2 (from day 1 to 31) exists
        assertEquals(1, lines.size());
        assertEquals(31, lines.get(0).getNumberOfDays());
        assertEquals(new BigDecimal("31000.00"), lines.get(0).getEnergyKwh());
        assertEquals(new BigDecimal("15.00"), lines.get(0).getRatePerKwh());
        assertEquals(new BigDecimal("465000.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 8: Boundary Condition - Tariff Change on Last Day (March 31: 30 days before, 1 day after)")
    void testTransitionMonth_ChangeOnLastDay() {
        Short folioNo = 208;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2019")
                .prvTariffRate(new BigDecimal("10.00"))
                .curTariffRate(new BigDecimal("20.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 3, 31))) // Day 31
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2019")
                .totalEnergySentToGrid(new BigDecimal("31000.0"))
                .billingMonth(YearMonth.of(2026, 3))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        List<TariffChargeLine> lines = result.getChargeLines();
        assertEquals(2, lines.size());
        assertEquals(30, lines.get(0).getNumberOfDays());
        assertEquals(new BigDecimal("30000.00"), lines.get(0).getEnergyKwh());
        assertEquals(1, lines.get(1).getNumberOfDays());
        assertEquals(new BigDecimal("1000.00"), lines.get(1).getEnergyKwh());
        assertEquals(LocalDate.of(2026, 3, 31), lines.get(1).getPeriodStart());
        assertEquals(LocalDate.of(2026, 3, 31), lines.get(1).getPeriodEnd());
    }

    @Test
    @DisplayName("TEST 9: Single Generation Loss applied to monthly energy before split prorating")
    void testTransitionMonth_GenerationLossAppliedOnceBeforeProrating() {
        Short folioNo = 209;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2019")
                .prvTariffRate(new BigDecimal("15.00"))
                .curTariffRate(new BigDecimal("20.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 4, 16))) // April 16 (15 days before, 15 days after)
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        // 100,000 kWh energy sent, 2% loss -> loss = 2,000 kWh, eligible = 98,000 kWh
        // Month has 30 days. Split at day 16 -> 15 days before, 15 days after
        // beforeEnergy = 98,000 * 15 / 30 = 49,000.00
        // afterEnergy = 98,000 - 49,000 = 49,000.00
        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2019")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .generationLosses(new BigDecimal("2.00")) // 2%
                .billingMonth(YearMonth.of(2026, 4))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(new BigDecimal("2000.00"), result.getGenerationLosses());
        assertEquals(new BigDecimal("98000.00"), result.getEligibleEnergy());
        List<TariffChargeLine> lines = result.getChargeLines();
        assertEquals(2, lines.size());
        assertEquals(new BigDecimal("49000.00"), lines.get(0).getEnergyKwh());
        assertEquals(new BigDecimal("49000.00"), lines.get(1).getEnergyKwh());
        // beforeCost = 49,000 * 15 = 735,000.00
        // afterCost = 49,000 * 20 = 980,000.00
        // totalCost = 1,715,000.00
        assertEquals(new BigDecimal("1715000.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 10: Zero Eligible Energy in transition month produces zero cost without divide-by-zero")
    void testTransitionMonth_ZeroEnergy_NoException() {
        Short folioNo = 210;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2019")
                .prvTariffRate(new BigDecimal("10.00"))
                .curTariffRate(new BigDecimal("20.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 3, 15)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2019")
                .totalEnergySentToGrid(BigDecimal.ZERO)
                .billingMonth(YearMonth.of(2026, 3))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.00"), result.getEligibleEnergy());
        assertEquals(new BigDecimal("0.00"), result.getCostOfEnergy());
        assertEquals(new BigDecimal("0.00"), result.getSelectedTariffRate());
    }

    @Test
    @DisplayName("TEST 11: Missing prv_tariff_rate on transition month throws BAD_REQUEST")
    void testTransitionMonth_MissingPrvRate_ThrowsBadRequest() {
        Short folioNo = 211;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2019")
                .prvTariffRate(null) // missing
                .curTariffRate(new BigDecimal("20.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 3, 15)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2019")
                .totalEnergySentToGrid(new BigDecimal("50000.0"))
                .billingMonth(YearMonth.of(2026, 3))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Previous tariff rate (prv_tariff_rate) is missing"));
    }

    @Test
    @DisplayName("TEST 12: Missing cur_tariff_rate on transition month throws BAD_REQUEST")
    void testTransitionMonth_MissingCurRate_ThrowsBadRequest() {
        Short folioNo = 212;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2019")
                .prvTariffRate(new BigDecimal("10.00"))
                .curTariffRate(null) // missing
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 3, 15)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2019")
                .totalEnergySentToGrid(new BigDecimal("50000.0"))
                .billingMonth(YearMonth.of(2026, 3))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Current tariff rate (cur_tariff_rate) is missing"));
    }

    @Test
    @DisplayName("TEST 13: Missing tariff rate database record throws BAD_REQUEST")
    void testMissingTariffRateRecord_ThrowsBadRequest() {
        Short folioNo = 999;
        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.empty());

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2019")
                .totalEnergySentToGrid(new BigDecimal("50000.0"))
                .billingMonth(YearMonth.of(2026, 8))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("No tariff rate configuration found for folio: 999", ex.getReason());
    }

    @Test
    @DisplayName("TEST 14: Missing tariff_changed date throws BAD_REQUEST")
    void testMissingTariffChangedDate_ThrowsBadRequest() {
        Short folioNo = 214;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2019")
                .prvTariffRate(new BigDecimal("10.55"))
                .curTariffRate(new BigDecimal("10.86"))
                .tariffChanged(null)
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2019")
                .totalEnergySentToGrid(new BigDecimal("50000.0"))
                .billingMonth(YearMonth.of(2026, 8))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Tariff changed date is missing in tariff rate configuration for folio: 214", ex.getReason());
    }

    @Test
    @DisplayName("TEST 15: supports() method accepts 15_2019 and 15+ (2019), rejects others")
    void testSupportsMethod() {
        assertTrue(calculator.supports("15_2019"));
        assertTrue(calculator.supports(" 15_2019 "));
        assertTrue(calculator.supports("15+ (2019)"));
        assertTrue(calculator.supports(" 15+ (2019) "));

        assertFalse(calculator.supports("15+ New (2022)"));
        assertFalse(calculator.supports("15_2022"));
        assertFalse(calculator.supports("FLAT"));
        assertFalse(calculator.supports(null));
        assertFalse(calculator.supports(""));
    }

    @Test
    @DisplayName("TEST 16: TariffCalculatorResolver resolves 15_2019 and 15+ (2019) to FifteenPlus2019TariffCalculator")
    void testTariffCalculatorResolverResolution() {
        assertSame(calculator, resolver.resolve("15_2019"));
        assertSame(calculator, resolver.resolve("15+ (2019)"));
        assertSame(flatCalculator, resolver.resolve("FLAT"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> resolver.resolve("15_2022"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Unsupported tariff type"));
    }

    @Test
    @DisplayName("TEST 17: Losses exceed energy sent to grid -> Eligible energy clamped to zero")
    void testLossesExceedEnergy_ClampedToZero() {
        Short folioNo = 217;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2019")
                .prvTariffRate(new BigDecimal("10.55"))
                .curTariffRate(new BigDecimal("10.86"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 5, 10)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2019")
                .totalEnergySentToGrid(new BigDecimal("100.0"))
                .explicitLossKwh(new BigDecimal("150.0"))
                .billingMonth(YearMonth.of(2026, 8))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.00"), result.getEligibleEnergy());
        assertEquals(new BigDecimal("0.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 18: Fractional generation loss percentage calculation (e.g., 0.05 for 5%)")
    void testFractionalGenerationLoss() {
        Short folioNo = 218;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2019")
                .prvTariffRate(new BigDecimal("10.00"))
                .curTariffRate(new BigDecimal("10.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 5, 10)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        // 100,000 kWh with 0.05 loss factor (5%) -> loss = 5,000 kWh, eligible = 95,000 kWh
        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2019")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .generationLosses(new BigDecimal("0.05"))
                .billingMonth(YearMonth.of(2026, 8))
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(new BigDecimal("5000.00"), result.getGenerationLosses());
        assertEquals(new BigDecimal("95000.00"), result.getEligibleEnergy());
    }

    @Test
    @DisplayName("TEST 19: Missing billingMonth in context falls back to previous calendar month")
    void testMissingBillingMonth_FallsBackToPreviousMonth() {
        Short folioNo = 219;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2019")
                .prvTariffRate(new BigDecimal("10.00"))
                .curTariffRate(new BigDecimal("12.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2020, 1, 1))) // Far in past -> prv_tariff_rate
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2019")
                .totalEnergySentToGrid(new BigDecimal("50000.0"))
                .billingMonth(null) // null month falls back to previous month
                .build();

        TariffCalculationResult result = calculator.calculate(context);
        assertNotNull(result);
        assertEquals(new BigDecimal("12.00"), result.getSelectedTariffRate());
        assertEquals(new BigDecimal("600000.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 20: Missing folioNo in context throws BAD_REQUEST")
    void testMissingFolioNo_ThrowsBadRequest() {
        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(null)
                .tariffType("15_2019")
                .totalEnergySentToGrid(new BigDecimal("50000.0"))
                .billingMonth(YearMonth.of(2026, 8))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Folio number (folio_no) is required for tariff calculation.", ex.getReason());
    }
}
