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
class FifteenPlus2022TariffCalculatorTest {

    @Mock
    private NcreDevTariffRateRepository tariffRateRepository;

    private FifteenPlus2022TariffCalculator calculator2022;
    private FifteenPlus2019TariffCalculator calculator2019;
    private FlatTariffCalculator flatCalculator;
    private TariffCalculatorResolver resolver;

    @BeforeEach
    void setUp() {
        calculator2022 = new FifteenPlus2022TariffCalculator(tariffRateRepository);
        calculator2019 = new FifteenPlus2019TariffCalculator(tariffRateRepository);
        flatCalculator = new FlatTariffCalculator(tariffRateRepository);
        resolver = new TariffCalculatorResolver(Arrays.asList(flatCalculator, calculator2019, calculator2022));
    }

    @Test
    @DisplayName("TEST 1: supports('15_2022') returns true (and supports '15+ New (2022)')")
    void testSupports_15_2022() {
        assertTrue(calculator2022.supports("15_2022"));
        assertTrue(calculator2022.supports("15_2022 "));
        assertTrue(calculator2022.supports("15+ New (2022)"));
        assertTrue(calculator2022.supports(" 15+ New (2022) "));
    }

    @Test
    @DisplayName("TEST 2: Verify FifteenPlus2022TariffCalculator does not support 15_2019, FLAT, TTT, or null")
    void testDoesNotSupportOtherTariffs() {
        assertFalse(calculator2022.supports("15_2019"));
        assertFalse(calculator2022.supports("15+ (2019)"));
        assertFalse(calculator2022.supports("FLAT"));
        assertFalse(calculator2022.supports("TTT"));
        assertFalse(calculator2022.supports("15"));
        assertFalse(calculator2022.supports(null));
        assertFalse(calculator2022.supports(""));
    }

    @Test
    @DisplayName("TEST 3: Resolver with 15_2022 resolves to FifteenPlus2022TariffCalculator")
    void testResolver_ResolvesTo2022Calculator() {
        TariffCalculator resolved = resolver.resolve("15_2022");
        assertNotNull(resolved);
        assertTrue(resolved instanceof FifteenPlus2022TariffCalculator);
        assertSame(calculator2022, resolved);

        TariffCalculator resolvedDesc = resolver.resolve("15+ New (2022)");
        assertNotNull(resolvedDesc);
        assertSame(calculator2022, resolvedDesc);
    }

    @Test
    @DisplayName("TEST 4: billingMonth > tariffChangedMonth (after change date) -> cur_tariff_rate selected")
    void testBillingMonthAfterTariffChanged_SelectsCurRate() {
        Short folioNo = 241;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2022")
                .prvTariffRate(new BigDecimal("10.25"))
                .curTariffRate(new BigDecimal("12.50"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 7, 15))) // July 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2022")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .billingMonth(YearMonth.of(2026, 8)) // August 2026 (> July 2026)
                .build();

        TariffCalculationResult result = calculator2022.calculate(context);

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
    @DisplayName("TEST 5: billingMonth < tariffChangedMonth (before change date) -> prv_tariff_rate selected")
    void testBillingMonthBeforeTariffChanged_SelectsPrvRate() {
        Short folioNo = 245;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2022")
                .prvTariffRate(new BigDecimal("15.24"))
                .curTariffRate(new BigDecimal("16.80"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 9, 10))) // September 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2022")
                .totalEnergySentToGrid(new BigDecimal("50000.0"))
                .billingMonth(YearMonth.of(2026, 8)) // August 2026 (< September 2026)
                .build();

        TariffCalculationResult result = calculator2022.calculate(context);

        assertNotNull(result);
        assertEquals(new BigDecimal("15.24"), result.getSelectedTariffRate());
        assertEquals(TariffRateType.PRV_TARIFF_RATE, result.getSelectedRateType());
        assertTrue(result.isPrvTariffRateSelected());
        assertFalse(result.isCurTariffRateSelected());
        assertFalse(result.isMultiRateSelected());
        assertNull(result.getChargeLines());
        assertEquals(new BigDecimal("50000.00"), result.getEligibleEnergy());
        assertEquals(new BigDecimal("762000.00"), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 6: Transition month -> returns two tariff charge lines")
    void testTransitionMonth_ReturnsTwoChargeLines() {
        Short folioNo = 253;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2022")
                .prvTariffRate(new BigDecimal("11.05"))
                .curTariffRate(new BigDecimal("13.50"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 8, 13))) // August 13, 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2022")
                .totalEnergySentToGrid(new BigDecimal("216029.0"))
                .billingMonth(YearMonth.of(2026, 8)) // August 2026
                .build();

        TariffCalculationResult result = calculator2022.calculate(context);

        assertNotNull(result);
        assertTrue(result.isMultiRateSelected());
        List<TariffChargeLine> lines = result.getChargeLines();
        assertNotNull(lines);
        assertEquals(2, lines.size());
    }

    @Test
    @DisplayName("TEST 7: Verify before period uses prv_tariff_rate")
    void testTransitionMonth_BeforePeriodUsesPrvRate() {
        Short folioNo = 257;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2022")
                .prvTariffRate(new BigDecimal("13.17"))
                .curTariffRate(new BigDecimal("14.50"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 8, 13)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2022")
                .totalEnergySentToGrid(new BigDecimal("216029.0"))
                .billingMonth(YearMonth.of(2026, 8))
                .build();

        TariffCalculationResult result = calculator2022.calculate(context);
        TariffChargeLine beforeLine = result.getChargeLines().get(0);

        assertEquals("Before Tariff Change", beforeLine.getLabel());
        assertEquals(LocalDate.of(2026, 8, 1), beforeLine.getPeriodStart());
        assertEquals(LocalDate.of(2026, 8, 12), beforeLine.getPeriodEnd());
        assertEquals(12, beforeLine.getNumberOfDays());
        assertEquals(new BigDecimal("13.17"), beforeLine.getRatePerKwh());
        assertEquals(TariffRateType.PRV_TARIFF_RATE, beforeLine.getRateType());
    }

    @Test
    @DisplayName("TEST 8: Verify change date and after period uses cur_tariff_rate")
    void testTransitionMonth_AfterPeriodUsesCurRate() {
        Short folioNo = 261;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2022")
                .prvTariffRate(new BigDecimal("16.43"))
                .curTariffRate(new BigDecimal("17.80"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 8, 13)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2022")
                .totalEnergySentToGrid(new BigDecimal("216029.0"))
                .billingMonth(YearMonth.of(2026, 8))
                .build();

        TariffCalculationResult result = calculator2022.calculate(context);
        TariffChargeLine afterLine = result.getChargeLines().get(1);

        assertEquals("From Tariff Change Date", afterLine.getLabel());
        assertEquals(LocalDate.of(2026, 8, 13), afterLine.getPeriodStart());
        assertEquals(LocalDate.of(2026, 8, 31), afterLine.getPeriodEnd());
        assertEquals(19, afterLine.getNumberOfDays());
        assertEquals(new BigDecimal("17.80"), afterLine.getRatePerKwh());
        assertEquals(TariffRateType.CUR_TARIFF_RATE, afterLine.getRateType());
    }

    @Test
    @DisplayName("TEST 9: Verify beforeDays + afterDays == billing month total days")
    void testTransitionMonth_DaysConservation() {
        Short folioNo = 265;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2022")
                .prvTariffRate(new BigDecimal("16.43"))
                .curTariffRate(new BigDecimal("17.80"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 8, 13)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        YearMonth billingMonth = YearMonth.of(2026, 8);
        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2022")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .billingMonth(billingMonth)
                .build();

        TariffCalculationResult result = calculator2022.calculate(context);
        List<TariffChargeLine> lines = result.getChargeLines();

        int beforeDays = lines.get(0).getNumberOfDays();
        int afterDays = lines.get(1).getNumberOfDays();

        assertEquals(billingMonth.lengthOfMonth(), beforeDays + afterDays);
        assertEquals(31, beforeDays + afterDays);
        assertEquals(12, beforeDays);
        assertEquals(19, afterDays);
    }

    @Test
    @DisplayName("TEST 10: Verify beforeEnergy + afterEnergy == monthlyEligibleEnergy")
    void testTransitionMonth_EnergyConservation() {
        Short folioNo = 269;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2022")
                .prvTariffRate(new BigDecimal("16.43"))
                .curTariffRate(new BigDecimal("17.80"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 8, 13)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        BigDecimal totalEnergy = new BigDecimal("216029.0");
        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2022")
                .totalEnergySentToGrid(totalEnergy)
                .billingMonth(YearMonth.of(2026, 8))
                .build();

        TariffCalculationResult result = calculator2022.calculate(context);
        List<TariffChargeLine> lines = result.getChargeLines();

        BigDecimal beforeEnergy = lines.get(0).getEnergyKwh();
        BigDecimal afterEnergy = lines.get(1).getEnergyKwh();

        assertEquals(result.getEligibleEnergy(), beforeEnergy.add(afterEnergy));
    }

    @Test
    @DisplayName("TEST 11: Verify generation loss is applied once before energy splitting")
    void testTransitionMonth_GenerationLossDeductedOnceBeforeSplit() {
        Short folioNo = 273;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2022")
                .prvTariffRate(new BigDecimal("16.43"))
                .curTariffRate(new BigDecimal("17.80"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 8, 13)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        // Total energy: 100,000, 2.0% generation losses -> 2,000 loss -> 98,000 eligible
        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2022")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .generationLosses(new BigDecimal("2.00")) // 2%
                .billingMonth(YearMonth.of(2026, 8))
                .build();

        TariffCalculationResult result = calculator2022.calculate(context);

        assertEquals(new BigDecimal("2000.00"), result.getGenerationLosses());
        assertEquals(new BigDecimal("98000.00"), result.getEligibleEnergy());

        List<TariffChargeLine> lines = result.getChargeLines();
        BigDecimal sumLinesEnergy = lines.get(0).getEnergyKwh().add(lines.get(1).getEnergyKwh());
        assertEquals(new BigDecimal("98000.00"), sumLinesEnergy);
    }

    @Test
    @DisplayName("TEST 12: Verify totalCostOfEnergy = beforeCost + afterCost")
    void testTransitionMonth_TotalCostMatchesSumOfSubtotals() {
        Short folioNo = 277;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2022")
                .prvTariffRate(new BigDecimal("15.93"))
                .curTariffRate(new BigDecimal("16.50"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 8, 13)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2022")
                .totalEnergySentToGrid(new BigDecimal("216029.0"))
                .billingMonth(YearMonth.of(2026, 8))
                .build();

        TariffCalculationResult result = calculator2022.calculate(context);
        List<TariffChargeLine> lines = result.getChargeLines();

        BigDecimal beforeCost = lines.get(0).getCostOfEnergy();
        BigDecimal afterCost = lines.get(1).getCostOfEnergy();

        assertEquals(result.getCostOfEnergy(), beforeCost.add(afterCost));
    }

    @Test
    @DisplayName("TEST 13: Missing tariff-rate configuration throws clean BAD_REQUEST")
    void testMissingTariffRateConfiguration_ThrowsBadRequest() {
        Short folioNo = 999;
        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.empty());

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2022")
                .totalEnergySentToGrid(new BigDecimal("10000.0"))
                .billingMonth(YearMonth.of(2026, 8))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator2022.calculate(context));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("No tariff rate configuration found for folio: 999"));
    }

    @Test
    @DisplayName("TEST 14: Missing required tariff rate throws clean BAD_REQUEST")
    void testMissingRequiredTariffRate_ThrowsBadRequest() {
        Short folioNo = 281;
        // Case: transition month but cur_tariff_rate is null
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("15_2022")
                .prvTariffRate(new BigDecimal("14.61"))
                .curTariffRate(null) // Missing!
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 8, 13)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("15_2022")
                .totalEnergySentToGrid(new BigDecimal("10000.0"))
                .billingMonth(YearMonth.of(2026, 8))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator2022.calculate(context));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Current tariff rate (cur_tariff_rate) is missing"));
    }

    @Test
    @DisplayName("TEST 15: Verify FLAT still resolves to FlatTariffCalculator")
    void testResolver_FlatResolvesToFlatCalculator() {
        TariffCalculator resolved = resolver.resolve("FLAT");
        assertNotNull(resolved);
        assertTrue(resolved instanceof FlatTariffCalculator);
        assertSame(flatCalculator, resolved);
    }

    @Test
    @DisplayName("TEST 16: Verify 15_2019 still resolves to FifteenPlus2019TariffCalculator")
    void testResolver_15_2019ResolvesTo2019Calculator() {
        TariffCalculator resolved = resolver.resolve("15_2019");
        assertNotNull(resolved);
        assertTrue(resolved instanceof FifteenPlus2019TariffCalculator);
        assertSame(calculator2019, resolved);
    }

    @Test
    @DisplayName("TEST 17: Verify 15_2022 resolves only to FifteenPlus2022TariffCalculator")
    void testResolver_15_2022ResolvesOnlyTo2022Calculator() {
        TariffCalculator resolved = resolver.resolve("15_2022");
        assertNotNull(resolved);
        assertTrue(resolved instanceof FifteenPlus2022TariffCalculator);
        assertFalse(resolved instanceof FifteenPlus2019TariffCalculator);
        assertSame(calculator2022, resolved);
    }
}
