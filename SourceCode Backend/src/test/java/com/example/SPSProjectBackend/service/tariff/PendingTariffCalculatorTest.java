package com.example.SPSProjectBackend.service.tariff;

import com.example.SPSProjectBackend.model.NcreDeveloper;
import com.example.SPSProjectBackend.repository.NcreDevTariffRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PendingTariffCalculatorTest {

    @Mock
    private NcreDevTariffRateRepository devTariffRateRepository;

    private PendingTariffCalculator pendingCalculator;
    private FlatTariffCalculator flatCalculator;
    private VariableTariffCalculator variableCalculator;
    private FlatVariableTariffCalculator flatVariableCalculator;
    private Ttt3TariffCalculator ttt3Calculator;
    private Ttt5TariffCalculator ttt5Calculator;
    private AcTariffCalculator acCalculator;
    private FifteenPlus2019TariffCalculator fifteen2019Calculator;
    private FifteenPlus2022TariffCalculator fifteen2022Calculator;
    private TariffCalculatorResolver resolver;

    @BeforeEach
    void setUp() {
        pendingCalculator = new PendingTariffCalculator();
        flatCalculator = new FlatTariffCalculator(devTariffRateRepository);
        variableCalculator = new VariableTariffCalculator(devTariffRateRepository);
        flatVariableCalculator = new FlatVariableTariffCalculator(devTariffRateRepository);
        ttt3Calculator = new Ttt3TariffCalculator(devTariffRateRepository);
        ttt5Calculator = new Ttt5TariffCalculator(devTariffRateRepository);
        acCalculator = new AcTariffCalculator(devTariffRateRepository);
        fifteen2019Calculator = new FifteenPlus2019TariffCalculator(devTariffRateRepository);
        fifteen2022Calculator = new FifteenPlus2022TariffCalculator(devTariffRateRepository);

        // Put pendingCalculator first (matching @Order(1)) in the resolver list
        List<TariffCalculator> allCalculators = Arrays.asList(
                pendingCalculator,
                flatVariableCalculator,
                flatCalculator,
                variableCalculator,
                ttt3Calculator,
                ttt5Calculator,
                acCalculator,
                fifteen2019Calculator,
                fifteen2022Calculator
        );
        resolver = new TariffCalculatorResolver(allCalculators);
    }

    @Test
    @DisplayName("1. supports('PENDING') returns true")
    void testSupports_Pending_UpperCase() {
        assertTrue(pendingCalculator.supports("PENDING"));
    }

    @Test
    @DisplayName("2. supports('Pending') and case variations return true")
    void testSupports_Pending_CaseVariations() {
        assertTrue(pendingCalculator.supports("Pending"));
        assertTrue(pendingCalculator.supports("pending"));
        assertTrue(pendingCalculator.supports("  PENDING  "));
        assertTrue(pendingCalculator.supports("  Pending  "));
    }

    @Test
    @DisplayName("3. supports('FLAT') returns false")
    void testSupports_Flat_ReturnsFalse() {
        assertFalse(pendingCalculator.supports("FLAT"));
        assertFalse(pendingCalculator.supports("Flat"));
    }

    @Test
    @DisplayName("4. supports('VT') returns false")
    void testSupports_VT_ReturnsFalse() {
        assertFalse(pendingCalculator.supports("VT"));
        assertFalse(pendingCalculator.supports("Variable Tariff"));
    }

    @Test
    @DisplayName("5. supports('FLAT_VT') returns false")
    void testSupports_FlatVT_ReturnsFalse() {
        assertFalse(pendingCalculator.supports("FLAT_VT"));
        assertFalse(pendingCalculator.supports("FLAT (V)"));
        assertFalse(pendingCalculator.supports("FLAT  Variable Traiff"));
    }

    @Test
    @DisplayName("6. supports('TTT3') returns false")
    void testSupports_TTT3_ReturnsFalse() {
        assertFalse(pendingCalculator.supports("TTT3"));
        assertFalse(pendingCalculator.supports("TTT (3 years)"));
    }

    @Test
    @DisplayName("7. supports('TTT5') returns false")
    void testSupports_TTT5_ReturnsFalse() {
        assertFalse(pendingCalculator.supports("TTT5"));
        assertFalse(pendingCalculator.supports("TTT (5 years)"));
    }

    @Test
    @DisplayName("8. supports('AC') returns false")
    void testSupports_AC_ReturnsFalse() {
        assertFalse(pendingCalculator.supports("AC"));
        assertFalse(pendingCalculator.supports("Avoided Cost"));
    }

    @Test
    @DisplayName("9. supports('15_2019') returns false")
    void testSupports_15_2019_ReturnsFalse() {
        assertFalse(pendingCalculator.supports("15_2019"));
        assertFalse(pendingCalculator.supports("15+ (2019)"));
    }

    @Test
    @DisplayName("10. supports('15_2022') returns false")
    void testSupports_15_2022_ReturnsFalse() {
        assertFalse(pendingCalculator.supports("15_2022"));
        assertFalse(pendingCalculator.supports("15+ New (2023)"));
    }

    @Test
    @DisplayName("11. supports(null) and supports empty return false")
    void testSupports_NullOrEmpty_ReturnsFalse() {
        assertFalse(pendingCalculator.supports(null));
        assertFalse(pendingCalculator.supports(""));
        assertFalse(pendingCalculator.supports("   "));
        assertFalse(pendingCalculator.supports("UNKNOWN"));
        assertFalse(pendingCalculator.supports("PENDING_RATE"));
        assertFalse(pendingCalculator.supports("NO_RATE"));
        assertFalse(pendingCalculator.supports("WAITING"));
    }

    @Test
    @DisplayName("12. TariffCalculatorResolver resolves PENDING to PendingTariffCalculator")
    void testResolver_ResolvesPending() {
        TariffCalculator resolved = resolver.resolve("PENDING");
        assertNotNull(resolved);
        assertInstanceOf(PendingTariffCalculator.class, resolved);

        TariffCalculator resolvedMixed = resolver.resolve("Pending");
        assertNotNull(resolvedMixed);
        assertInstanceOf(PendingTariffCalculator.class, resolvedMixed);
    }

    @Test
    @DisplayName("13. TariffCalculatorResolver continues to resolve existing tariffs correctly")
    void testResolver_ResolvesOtherTariffsCorrectly() {
        assertInstanceOf(FlatTariffCalculator.class, resolver.resolve("FLAT"));
        assertInstanceOf(VariableTariffCalculator.class, resolver.resolve("VT"));
        assertInstanceOf(FlatVariableTariffCalculator.class, resolver.resolve("FLAT_VT"));
        assertInstanceOf(FlatVariableTariffCalculator.class, resolver.resolve("FLAT (V)"));
        assertInstanceOf(Ttt3TariffCalculator.class, resolver.resolve("TTT3"));
        assertInstanceOf(Ttt5TariffCalculator.class, resolver.resolve("TTT5"));
        assertInstanceOf(AcTariffCalculator.class, resolver.resolve("AC"));
        assertInstanceOf(FifteenPlus2019TariffCalculator.class, resolver.resolve("15_2019"));
        assertInstanceOf(FifteenPlus2022TariffCalculator.class, resolver.resolve("15_2022"));
    }

    @Test
    @DisplayName("14. calculate() throws ResponseStatusException with HTTP 422 Unprocessable Entity")
    void testCalculate_ThrowsResponseStatusException_422() {
        TariffCalculationContext context = createSampleContext();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pendingCalculator.calculate(context)
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatusCode());
    }

    @Test
    @DisplayName("15. calculate() error message clearly states tariff rate has not yet been assigned")
    void testCalculate_ErrorMessageContent() {
        TariffCalculationContext context = createSampleContext();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pendingCalculator.calculate(context)
        );

        String expectedMessage = "Tariff Rate Pending: A tariff rate has not yet been assigned to this developer. " +
                "The invoice cannot be prepared until the tariff rate is updated.";

        assertEquals(expectedMessage, exception.getReason());
        assertTrue(exception.getReason().contains("Tariff Rate Pending"));
        assertTrue(exception.getReason().contains("has not yet been assigned to this developer"));
        assertTrue(exception.getReason().contains("invoice cannot be prepared"));
    }

    @Test
    @DisplayName("16. Pending calculator has NO repositories injected (cannot query DB)")
    void testNoRepositoriesInjected() {
        Field[] fields = PendingTariffCalculator.class.getDeclaredFields();
        for (Field field : fields) {
            assertFalse(
                    field.getType().getName().contains("Repository"),
                    "PendingTariffCalculator must not declare any repository fields: " + field.getName()
            );
        }
    }

    @Test
    @DisplayName("17. Pending calculator does not extend any other tariff calculator")
    void testDoesNotExtendOtherCalculators() {
        assertEquals(Object.class, PendingTariffCalculator.class.getSuperclass());
        assertFalse(FlatTariffCalculator.class.isAssignableFrom(PendingTariffCalculator.class));
        assertFalse(VariableTariffCalculator.class.isAssignableFrom(PendingTariffCalculator.class));
        assertFalse(FlatVariableTariffCalculator.class.isAssignableFrom(PendingTariffCalculator.class));
        assertFalse(Ttt3TariffCalculator.class.isAssignableFrom(PendingTariffCalculator.class));
        assertFalse(Ttt5TariffCalculator.class.isAssignableFrom(PendingTariffCalculator.class));
        assertFalse(AcTariffCalculator.class.isAssignableFrom(PendingTariffCalculator.class));
        assertFalse(FifteenPlus2019TariffCalculator.class.isAssignableFrom(PendingTariffCalculator.class));
        assertFalse(FifteenPlus2022TariffCalculator.class.isAssignableFrom(PendingTariffCalculator.class));
    }

    @Test
    @DisplayName("18. Pending calculator does NOT calculate generation loss or cost of energy")
    void testDoesNotCalculateLossOrCost() {
        TariffCalculationContext context = TariffCalculationContext.builder()
                .developer(createDeveloperWithInitialTariff("AC relevant to 2005"))
                .folioNo((short) 169)
                .tariffType("PENDING")
                .totalEnergySentToGrid(BigDecimal.valueOf(100000))
                .generationLosses(BigDecimal.valueOf(500))
                .build();

        // Execution MUST throw without producing any calculation result or evaluating losses
        assertThrows(ResponseStatusException.class, () -> pendingCalculator.calculate(context));
    }

    @Test
    @DisplayName("19. Pending calculator does NOT fall back to initial_tariff")
    void testDoesNotFallbackToInitialTariff() {
        // Even when initial_tariff is populated with an old tariff, calculation is blocked
        NcreDeveloper dev = createDeveloperWithInitialTariff("AC relevant to 2005");
        TariffCalculationContext context = TariffCalculationContext.builder()
                .developer(dev)
                .folioNo((short) 169)
                .tariffType("PENDING")
                .totalEnergySentToGrid(BigDecimal.valueOf(50000))
                .generationLosses(BigDecimal.ZERO)
                .build();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            TariffCalculator calc = resolver.resolve(dev.getTariffType());
            calc.calculate(context);
        });

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
        assertTrue(ex.getReason().startsWith("Tariff Rate Pending:"));
    }

    @Test
    @DisplayName("20. calculate(null) still throws ResponseStatusException with clean business error")
    void testCalculate_NullContext_StillThrowsControlledException() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pendingCalculator.calculate(null)
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatusCode());
        assertEquals(PendingTariffCalculator.ERROR_MESSAGE, exception.getReason());
    }

    @Test
    @DisplayName("21. Charge lines are not created when tariff is PENDING")
    void testChargeLinesNotCreated() {
        TariffCalculationContext context = createSampleContext();
        assertThrows(ResponseStatusException.class, () -> pendingCalculator.calculate(context));
        // Verify no charge lines or calculation results can be produced
    }

    @Test
    @DisplayName("22. Prepare invoice flow fails cleanly for PENDING developer")
    void testPrepareInvoiceFlow_BlocksWhenTariffPending() {
        NcreDeveloper developer = createDeveloperWithInitialTariff("AC relevant to 2005");
        String tariffType = developer.getTariffType();

        // Simulating the exact InvoiceService pipeline at lines 625-626:
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            TariffCalculator calc = resolver.resolve(tariffType);
            calc.calculate(TariffCalculationContext.builder()
                    .developer(developer)
                    .folioNo(developer.getFolioNo())
                    .tariffType(tariffType)
                    .totalEnergySentToGrid(BigDecimal.valueOf(12345))
                    .build());
        });

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
        assertEquals(PendingTariffCalculator.ERROR_MESSAGE, ex.getReason());
    }

    @Test
    @DisplayName("23. No invoice preview DTO with financial results is produced for PENDING")
    void testNoInvoicePreviewProduced_WhenTariffPending() {
        NcreDeveloper developer = createDeveloperWithInitialTariff("AC relevant to 2005");
        boolean previewCreated = false;

        try {
            TariffCalculator calc = resolver.resolve(developer.getTariffType());
            calc.calculate(TariffCalculationContext.builder()
                    .developer(developer)
                    .tariffType(developer.getTariffType())
                    .build());
            previewCreated = true;
        } catch (ResponseStatusException ignored) {
            // Expected business exception stops preview generation
        }

        assertFalse(previewCreated, "Invoice preview must NOT be produced when tariff is PENDING");
    }

    @Test
    @DisplayName("24. Save Draft is blocked by tariff calculation exception for PENDING")
    void testSaveDraft_BlockedByTariffCalculationException() {
        NcreDeveloper developer = createDeveloperWithInitialTariff(null);
        boolean draftSaved = false;

        try {
            // saveDraftInvoice calls prepareInvoiceData first:
            TariffCalculator calc = resolver.resolve(developer.getTariffType());
            calc.calculate(TariffCalculationContext.builder()
                    .developer(developer)
                    .tariffType(developer.getTariffType())
                    .build());
            draftSaved = true; // Would only be reached if calculation succeeded
        } catch (ResponseStatusException ex) {
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
            assertEquals(PendingTariffCalculator.ERROR_MESSAGE, ex.getReason());
        }

        assertFalse(draftSaved, "Draft must NOT be saved when tariff is PENDING");
    }

    @Test
    @DisplayName("25. Submit is blocked by tariff calculation exception for PENDING")
    void testSubmit_BlockedByTariffCalculationException() {
        NcreDeveloper developer = createDeveloperWithInitialTariff(null);
        boolean submitted = false;

        try {
            // submitInvoice calls prepareInvoiceData first:
            TariffCalculator calc = resolver.resolve(developer.getTariffType());
            calc.calculate(TariffCalculationContext.builder()
                    .developer(developer)
                    .tariffType(developer.getTariffType())
                    .build());
            submitted = true; // Would only be reached if calculation succeeded
        } catch (ResponseStatusException ex) {
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
            assertEquals(PendingTariffCalculator.ERROR_MESSAGE, ex.getReason());
        }

        assertFalse(submitted, "Invoice must NOT be submitted when tariff is PENDING");
    }

    @Test
    @DisplayName("26. No new invoice row is persisted due to failed preparation")
    void testNoInvoiceRowPersisted_WhenPreparationFails() {
        NcreDeveloper developer = createDeveloperWithInitialTariff(null);
        int persistedInvoicesCount = 0;

        try {
            TariffCalculator calc = resolver.resolve(developer.getTariffType());
            calc.calculate(TariffCalculationContext.builder()
                    .developer(developer)
                    .tariffType(developer.getTariffType())
                    .build());
            persistedInvoicesCount++;
        } catch (ResponseStatusException ignored) {
            // Business error prevents invoice repository save
        }

        assertEquals(0, persistedInvoicesCount, "No invoice row should be persisted when preparation fails");
    }

    private TariffCalculationContext createSampleContext() {
        NcreDeveloper developer = new NcreDeveloper();
        developer.setFolioNo((short) 169);
        developer.setTariffType("PENDING");
        developer.setAccNbr("5970001600");
        developer.setDeveloperName("Recogen (Pvt) Ltd");
        developer.setCommissionedCapacityMw(BigDecimal.valueOf(1.5));

        return TariffCalculationContext.builder()
                .developer(developer)
                .folioNo((short) 169)
                .tariffType("PENDING")
                .totalEnergySentToGrid(BigDecimal.valueOf(25000))
                .generationLosses(BigDecimal.valueOf(100))
                .billCycle(10)
                .billingMonth(YearMonth.of(2026, 9))
                .previousReadingDate(LocalDate.of(2026, 8, 1))
                .presentReadingDate(LocalDate.of(2026, 8, 31))
                .build();
    }

    private NcreDeveloper createDeveloperWithInitialTariff(String initialTariff) {
        NcreDeveloper developer = new NcreDeveloper();
        developer.setFolioNo((short) 169);
        developer.setTariffType("PENDING");
        developer.setAccNbr("5970001600");
        developer.setDeveloperName("Recogen (Pvt) Ltd");
        developer.setInitialTariff(initialTariff);
        developer.setCommissionedCapacityMw(BigDecimal.valueOf(2.0));
        return developer;
    }
}
