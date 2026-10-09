package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.model.Agreement;
import com.example.SPSProjectBackend.model.Invoice;
import com.example.SPSProjectBackend.model.InvoiceStatus;
import com.example.SPSProjectBackend.model.PaymentDeduction;
import com.example.SPSProjectBackend.repository.AgreementRepository;
import com.example.SPSProjectBackend.repository.InvoiceRepository;
import com.example.SPSProjectBackend.repository.NcreDeveloperRepository;
import com.example.SPSProjectBackend.repository.NcreInvRdngsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceIntervalAndDeductionTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private AgreementRepository agreementRepository;

    @Mock
    private NcreDeveloperRepository ncreDeveloperRepository;

    @Mock
    private NcreInvRdngsRepository ncreInvRdngsRepository;

    private InvoiceCalculationService invoiceCalculationService;

    @InjectMocks
    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        invoiceCalculationService = new InvoiceCalculationService();
        // inject invoiceCalculationService manually
        org.springframework.test.util.ReflectionTestUtils.setField(invoiceService, "invoiceCalculationService", invoiceCalculationService);
    }

    @Test
    @DisplayName("calculateIntervalEnergy: standard difference, negative clamped to zero, and null safety")
    void testCalculateIntervalEnergy() {
        assertEquals(new BigDecimal("50.0"), invoiceCalculationService.calculateIntervalEnergy(new BigDecimal("150.0"), new BigDecimal("100.0")));
        assertEquals(BigDecimal.ZERO, invoiceCalculationService.calculateIntervalEnergy(new BigDecimal("50.0"), new BigDecimal("100.0")));
        assertEquals(new BigDecimal("100.0"), invoiceCalculationService.calculateIntervalEnergy(new BigDecimal("100.0"), null));
        assertEquals(BigDecimal.ZERO, invoiceCalculationService.calculateIntervalEnergy(null, new BigDecimal("50.0")));
        assertEquals(BigDecimal.ZERO, invoiceCalculationService.calculateIntervalEnergy(null, null));
    }

    @Test
    @DisplayName("isDed1PercDeduction correctly recognizes 1% deduction variants")
    void testIsDed1PercDeduction() {
        assertTrue(invoiceService.isDed1PercDeduction("1% Deduction (%)"));
        assertTrue(invoiceService.isDed1PercDeduction("1% Deduction"));
        assertTrue(invoiceService.isDed1PercDeduction("1DED"));
        assertTrue(invoiceService.isDed1PercDeduction("ded_1_perc"));
        assertTrue(invoiceService.isDed1PercDeduction("1%"));
        assertTrue(invoiceService.isDed1PercDeduction("1% deduction (masl)"));

        assertFalse(invoiceService.isDed1PercDeduction(null));
        assertFalse(invoiceService.isDed1PercDeduction("ESCROW Deduction (%)"));
        assertFalse(invoiceService.isDed1PercDeduction("Loyalty Deduction (%)"));
        assertFalse(invoiceService.isDed1PercDeduction("Treasury"));
        assertFalse(invoiceService.isDed1PercDeduction("Mahaweli Deduction (%)"));
    }

    @Test
    @DisplayName("Deduction type matchers recognize standard deduction names")
    void testOtherDeductionMatchers() {
        assertTrue(invoiceService.isLoyaltyDeduction("Loyalty Deduction (%)"));
        assertTrue(invoiceService.isEscrowDeduction("ESCROW Deduction (%)"));
        assertTrue(invoiceService.isMahaweliDeduction("Mahaweli Deduction (%)"));
        assertTrue(invoiceService.isMahaweliDeduction("Mahaveli Deduction (%)"));
        assertTrue(invoiceService.isTreasuryDeduction("Treasury"));

        assertFalse(invoiceService.isLoyaltyDeduction(null));
        assertFalse(invoiceService.isEscrowDeduction(null));
        assertFalse(invoiceService.isMahaweliDeduction(null));
        assertFalse(invoiceService.isTreasuryDeduction(null));
    }

    @Test
    @DisplayName("populateFinalizedInvoiceFields calculates ded_1_perc from agreement deductions")
    void testPopulateFinalizedInvoiceFields_Ded1Perc() {
        Invoice invoice = new Invoice();
        invoice.setId(101L);
        invoice.setStatus(InvoiceStatus.APPROVE);
        invoice.setFolioNo(501);
        invoice.setCostOfEnergy(new BigDecimal("100000.00")); // Cost of energy = 100,000

        // Agreement with Loyalty 2% and 1% Deduction 1%
        Agreement agreement = new Agreement();
        agreement.setAgreementId(1L);
        agreement.setFolioNo((short) 501);

        PaymentDeduction loyalty = new PaymentDeduction();
        loyalty.setDeductionType("Loyalty Deduction (%)");
        loyalty.setPercentage(new BigDecimal("2.00")); // 2% of 100,000 = 2,000 -> base for other = 98,000

        PaymentDeduction ded1 = new PaymentDeduction();
        ded1.setDeductionType("1% Deduction (%)");
        ded1.setPercentage(new BigDecimal("1.00")); // 1% of 98,000 = 980.00

        List<PaymentDeduction> deductions = new ArrayList<>();
        deductions.add(loyalty);
        deductions.add(ded1);
        agreement.setPaymentDeductions(deductions);

        when(agreementRepository.findByFolioNoOrderByAgreementIdDesc((short) 501))
                .thenReturn(Collections.singletonList(agreement));

        invoiceService.populateFinalizedInvoiceFields(invoice);

        // ded_1_perc should be calculated as 980.00 (1% of 98,000)
        assertNotNull(invoice.getDed1Perc());
        assertEquals(new BigDecimal("980.00"), invoice.getDed1Perc());

        // Eng send fields should default to 0 if not present in readings
        assertNotNull(invoice.getEngSendR1());
        assertNotNull(invoice.getEngSendR2());
        assertNotNull(invoice.getEngSendR3());
    }

    @Test
    @DisplayName("populateFinalizedInvoiceFields preserves already set interval energy and deduction values")
    void testPopulateFinalizedInvoiceFields_PreservesExistingValues() {
        Invoice invoice = new Invoice();
        invoice.setId(102L);
        invoice.setStatus(InvoiceStatus.APPROVE);
        invoice.setFolioNo(502);
        invoice.setCostOfEnergy(new BigDecimal("50000.00"));
        invoice.setEngSendR1(new BigDecimal("1234.50"));
        invoice.setEngSendR2(new BigDecimal("2345.60"));
        invoice.setEngSendR3(new BigDecimal("3456.70"));
        invoice.setDed1Perc(new BigDecimal("450.00"));

        invoiceService.populateFinalizedInvoiceFields(invoice);

        assertEquals(new BigDecimal("1234.50"), invoice.getEngSendR1());
        assertEquals(new BigDecimal("2345.60"), invoice.getEngSendR2());
        assertEquals(new BigDecimal("3456.70"), invoice.getEngSendR3());
        assertEquals(new BigDecimal("450.00"), invoice.getDed1Perc());

        verifyNoInteractions(agreementRepository);
    }

    @Test
    @DisplayName("populateFinalizedInvoiceFields recalculates R3 even if engSendR3 is BigDecimal.ZERO due to DB default")
    void testPopulateFinalizedInvoiceFields_RecalculatesWhenZeroDueToDbDefault() {
        Invoice invoice = new Invoice();
        invoice.setId(103L);
        invoice.setStatus(InvoiceStatus.APPROVE);
        invoice.setAccountNumber("5470100790");
        invoice.setAreaCode("54");
        invoice.setBillCycle(457);
        invoice.setEngSendR1(new BigDecimal("19761.59"));
        invoice.setEngSendR2(new BigDecimal("5863.32"));
        invoice.setEngSendR3(BigDecimal.ZERO); // Database default was 0.00000000

        com.example.SPSProjectBackend.model.NcreInvRdngs cr = new com.example.SPSProjectBackend.model.NcreInvRdngs();
        cr.setMtrNbr("MTR1");
        cr.setKwhR1(new BigDecimal("379044.000"));
        cr.setKwhR2(new BigDecimal("123375.563"));
        cr.setKwhR3(new BigDecimal("215205.953"));

        com.example.SPSProjectBackend.model.NcreInvRdngs pr = new com.example.SPSProjectBackend.model.NcreInvRdngs();
        pr.setMtrNbr("MTR1");
        pr.setKwhR1(new BigDecimal("359282.406"));
        pr.setKwhR2(new BigDecimal("117512.242"));
        pr.setKwhR3(new BigDecimal("204558.484"));

        when(ncreInvRdngsRepository.findAllByAccNbrAndAreaCdAndAddedBlcyTrimmed("5470100790", "54", "457"))
                .thenReturn(Collections.singletonList(cr));
        when(ncreInvRdngsRepository.findAllByAccNbrAndAreaCdAndAddedBlcyTrimmed("5470100790", "54", "456"))
                .thenReturn(Collections.singletonList(pr));

        invoiceService.populateFinalizedInvoiceFields(invoice);

        assertEquals(new BigDecimal("19761.59"), invoice.getEngSendR1());
        assertEquals(new BigDecimal("5863.32"), invoice.getEngSendR2());
        assertNotNull(invoice.getEngSendR3());
        assertEquals(new BigDecimal("10647.47"), invoice.getEngSendR3());
    }
}
