package com.example.SPSProjectBackend.service.tariff;

import com.example.SPSProjectBackend.model.NcreDevTariffRate;
import com.example.SPSProjectBackend.model.NcreDeveloper;
import com.example.SPSProjectBackend.repository.NcreDevTariffRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlatTariffCalculatorTest {

    @Mock
    private NcreDevTariffRateRepository tariffRateRepository;

    @InjectMocks
    private FlatTariffCalculator calculator;

    private TariffCalculatorResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new TariffCalculatorResolver(Collections.singletonList(calculator));
    }

    @Test
    @DisplayName("supports() recognizes all FLAT tariff variations and rejects others")
    void testSupports() {
        assertTrue(calculator.supports("FLAT"));
        assertTrue(calculator.supports("FLAT Tariff"));
        assertTrue(calculator.supports("FLAT_VT"));
        assertTrue(calculator.supports("flat tariff"));
        assertFalse(calculator.supports("TTT (3 years)"));
        assertFalse(calculator.supports("Avoided Cost (AC)"));
        assertFalse(calculator.supports("15+ (2019)"));
        assertFalse(calculator.supports(null));
    }

    @Test
    @DisplayName("TariffCalculatorResolver correctly resolves FlatTariffCalculator for FLAT tariffs")
    void testResolver() {
        TariffCalculator resolved = resolver.resolve("FLAT Tariff");
        assertNotNull(resolved);
        assertSame(calculator, resolved);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> resolver.resolve("UNKNOWN_TARIFF"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Unsupported tariff type"));
    }

    @Test
    @DisplayName("TEST 1: Billing month before tariff_changed -> prv_tariff_rate selected")
    void testBillingMonthBeforeTariffChanged_SelectsPrvRate() {
        Short folioNo = 100;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("FLAT")
                .prvTariffRate(new BigDecimal("15.50"))
                .curTariffRate(new BigDecimal("18.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 8, 15))) // Changed in Aug 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("FLAT")
                .totalEnergySentToGrid(new BigDecimal("50000.0"))
                .billingMonth(YearMonth.of(2026, 7)) // July 2026 (< August 2026)
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(new BigDecimal("15.50"), result.getSelectedTariffRate());
        assertEquals(TariffRateType.PRV_TARIFF_RATE, result.getSelectedRateType());
        assertTrue(result.isPrvTariffRateSelected());
        assertFalse(result.isCurTariffRateSelected());
        assertEquals(new BigDecimal("50000.0"), result.getOriginalEnergySentToGrid());
        assertEquals(new BigDecimal("50000.00"), result.getEligibleEnergy());
        assertEquals(new BigDecimal("775000.00"), result.getCostOfEnergy()); // 50000 * 15.50
    }

    @Test
    @DisplayName("TEST 2: Billing month after tariff_changed -> cur_tariff_rate selected")
    void testBillingMonthAfterTariffChanged_SelectsCurRate() {
        Short folioNo = 101;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("FLAT")
                .prvTariffRate(new BigDecimal("15.50"))
                .curTariffRate(new BigDecimal("18.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 8, 15))) // Changed in Aug 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("FLAT")
                .totalEnergySentToGrid(new BigDecimal("50000.0"))
                .billingMonth(YearMonth.of(2026, 9)) // Sept 2026 (> August 2026)
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(new BigDecimal("18.00"), result.getSelectedTariffRate());
        assertEquals(TariffRateType.CUR_TARIFF_RATE, result.getSelectedRateType());
        assertTrue(result.isCurTariffRateSelected());
        assertFalse(result.isPrvTariffRateSelected());
        assertEquals(new BigDecimal("900000.00"), result.getCostOfEnergy()); // 50000 * 18.00
    }

    @Test
    @DisplayName("TEST 3: Billing month equals tariff_changed (<=) -> Selects prv_tariff_rate")
    void testBillingMonthEqualsTariffChanged_SelectsPrvRate() {
        Short folioNo = 102;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("FLAT")
                .prvTariffRate(new BigDecimal("15.50"))
                .curTariffRate(new BigDecimal("18.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 8, 15))) // Changed in Aug 2026
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("FLAT")
                .totalEnergySentToGrid(new BigDecimal("50000.0"))
                .billingMonth(YearMonth.of(2026, 8)) // August 2026 (== August 2026)
                .build();

        TariffCalculationResult result = calculator.calculate(context);

        assertNotNull(result);
        assertEquals(new BigDecimal("15.50"), result.getSelectedTariffRate());
        assertEquals(TariffRateType.PRV_TARIFF_RATE, result.getSelectedRateType());
        assertTrue(result.isPrvTariffRateSelected());
        assertFalse(result.isCurTariffRateSelected());
        assertEquals(new BigDecimal("775000.00"), result.getCostOfEnergy()); // 50000 * 15.50
    }

    @Test
    @DisplayName("TEST 4: Generation losses deduction (100,000 - 5,000 = 95,000 * 16.92)")
    void testGenerationLossesDeduction() {
        Short folioNo = 103;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("FLAT")
                .prvTariffRate(new BigDecimal("16.92"))
                .curTariffRate(new BigDecimal("16.92"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 12, 31)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        // Testing with explicit loss kWh (5,000 kWh from 100,000 kWh)
        TariffCalculationContext contextExplicit = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("FLAT")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .explicitLossKwh(new BigDecimal("5000.0"))
                .billingMonth(YearMonth.of(2026, 5))
                .build();

        TariffCalculationResult resultExplicit = calculator.calculate(contextExplicit);
        assertEquals(new BigDecimal("100000.0"), resultExplicit.getOriginalEnergySentToGrid());
        assertEquals(new BigDecimal("5000.00"), resultExplicit.getGenerationLosses());
        assertEquals(new BigDecimal("95000.00"), resultExplicit.getEligibleEnergy());
        assertEquals(new BigDecimal("16.92"), resultExplicit.getSelectedTariffRate());
        assertEquals(new BigDecimal("1607400.00"), resultExplicit.getCostOfEnergy()); // 95,000 * 16.92

        // Testing with percentage loss (5.00% from ncre_developers)
        NcreDeveloper developer = new NcreDeveloper();
        developer.setGenerationLosses(new BigDecimal("5.00")); // 5%

        TariffCalculationContext contextPercentage = TariffCalculationContext.builder()
                .developer(developer)
                .folioNo(folioNo)
                .tariffType("FLAT")
                .totalEnergySentToGrid(new BigDecimal("100000.0"))
                .generationLosses(developer.getGenerationLosses())
                .billingMonth(YearMonth.of(2026, 5))
                .build();

        TariffCalculationResult resultPercentage = calculator.calculate(contextPercentage);
        assertEquals(new BigDecimal("5000.00"), resultPercentage.getGenerationLosses());
        assertEquals(new BigDecimal("95000.00"), resultPercentage.getEligibleEnergy());
        assertEquals(new BigDecimal("1607400.00"), resultPercentage.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 5: Generation losses greater than total energy -> eligibleEnergy = 0, costOfEnergy = 0")
    void testGenerationLossesGreaterThanEnergy() {
        Short folioNo = 104;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("FLAT")
                .prvTariffRate(new BigDecimal("20.00"))
                .curTariffRate(new BigDecimal("20.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 12, 31)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("FLAT")
                .totalEnergySentToGrid(new BigDecimal("1000.0"))
                .explicitLossKwh(new BigDecimal("1500.0"))
                .billingMonth(YearMonth.of(2026, 5))
                .build();

        TariffCalculationResult result = calculator.calculate(context);
        assertEquals(new BigDecimal("1000.0"), result.getOriginalEnergySentToGrid());
        assertEquals(new BigDecimal("1500.00"), result.getGenerationLosses());
        assertEquals(BigDecimal.ZERO.setScale(2), result.getEligibleEnergy());
        assertEquals(BigDecimal.ZERO.setScale(2), result.getCostOfEnergy());
    }

    @Test
    @DisplayName("TEST 6: Missing tariff-rate record -> Throws clean configuration error")
    void testMissingTariffRateRecord_ThrowsConfigurationError() {
        Short folioNo = 999;
        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.empty());

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("FLAT")
                .totalEnergySentToGrid(new BigDecimal("10000.0"))
                .billingMonth(YearMonth.of(2026, 5))
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("No tariff rate configuration found for folio: 999"));
    }

    @Test
    @DisplayName("TEST 7: Missing required selected tariff rate -> Throws clean configuration error")
    void testMissingSelectedTariffRate_ThrowsConfigurationError() {
        Short folioNo = 105;

        // Case A: prv_tariff_rate is null when billingMonth < tariffChangedMonth
        NcreDevTariffRate rateMissingPrv = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("FLAT")
                .prvTariffRate(null) // MISSING
                .curTariffRate(new BigDecimal("18.00"))
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 8, 15)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rateMissingPrv));

        TariffCalculationContext contextPrv = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("FLAT")
                .totalEnergySentToGrid(new BigDecimal("10000.0"))
                .billingMonth(YearMonth.of(2026, 7)) // Before Aug 2026
                .build();

        ResponseStatusException exPrv = assertThrows(ResponseStatusException.class, () -> calculator.calculate(contextPrv));
        assertEquals(HttpStatus.BAD_REQUEST, exPrv.getStatusCode());
        assertTrue(exPrv.getReason().contains("Previous tariff rate (prv_tariff_rate) is missing"));

        // Case B: cur_tariff_rate is null when billingMonth > tariffChangedMonth
        NcreDevTariffRate rateMissingCur = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("FLAT")
                .prvTariffRate(new BigDecimal("15.00"))
                .curTariffRate(null) // MISSING
                .tariffChanged(Date.valueOf(LocalDate.of(2026, 8, 15)))
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rateMissingCur));

        TariffCalculationContext contextCur = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("FLAT")
                .totalEnergySentToGrid(new BigDecimal("10000.0"))
                .billingMonth(YearMonth.of(2026, 9)) // After Aug 2026
                .build();

        ResponseStatusException exCur = assertThrows(ResponseStatusException.class, () -> calculator.calculate(contextCur));
        assertEquals(HttpStatus.BAD_REQUEST, exCur.getStatusCode());
        assertTrue(exCur.getReason().contains("Current tariff rate (cur_tariff_rate) is missing"));
    }

    @Test
    @DisplayName("Validation: Missing folio_no throws clean error")
    void testMissingFolioNo_ThrowsError() {
        TariffCalculationContext context = TariffCalculationContext.builder()
                .tariffType("FLAT")
                .folioNo(null)
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Folio number (folio_no) is required"));
    }

    @Test
    @DisplayName("Validation: Missing tariff_changed throws clean error")
    void testMissingTariffChanged_ThrowsError() {
        Short folioNo = 106;
        NcreDevTariffRate rate = NcreDevTariffRate.builder()
                .folioNo(folioNo)
                .tariffCode("FLAT")
                .prvTariffRate(new BigDecimal("15.00"))
                .curTariffRate(new BigDecimal("18.00"))
                .tariffChanged(null) // MISSING
                .build();

        when(tariffRateRepository.findByFolioNo(folioNo)).thenReturn(Optional.of(rate));

        TariffCalculationContext context = TariffCalculationContext.builder()
                .folioNo(folioNo)
                .tariffType("FLAT")
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> calculator.calculate(context));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Tariff changed date is missing"));
    }
}
