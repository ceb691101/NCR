package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.model.Invoice;
import com.example.SPSProjectBackend.model.InvoiceStatus;
import com.example.SPSProjectBackend.repository.InvoiceRepository;
import com.example.SPSProjectBackend.repository.InvoiceStatusHistoryRepository;
import com.example.SPSProjectBackend.repository.NcreBillCycleRepository;
import com.example.SPSProjectBackend.repository.NcreDeveloperRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceFilterServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private NcreDeveloperRepository ncreDeveloperRepository;

    @Mock
    private InvoiceStatusHistoryRepository invoiceStatusHistoryRepository;

    @Mock
    private NcreBillCycleRepository ncreBillCycleRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private InvoiceCalculationService invoiceCalculationService;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    void testGetAllInvoices_WithSpecificMonthAndStatus() {
        Invoice inv = new Invoice();
        inv.setId(1L);
        inv.setInvoiceMonth("August 2026");
        inv.setStatus(InvoiceStatus.FINALIZE);

        when(invoiceRepository.findByInvoiceMonthAndStatus("August 2026", InvoiceStatus.FINALIZE))
                .thenReturn(List.of(inv));
        when(ncreDeveloperRepository.findAll()).thenReturn(Collections.emptyList());

        List<Invoice> result = invoiceService.getAllInvoices(null, null, "Director", "August 2026", "FINALIZE");

        assertEquals(1, result.size());
        assertEquals("August 2026", result.get(0).getInvoiceMonth());
        assertEquals(InvoiceStatus.FINALIZE, result.get(0).getStatus());
        verify(invoiceRepository).findByInvoiceMonthAndStatus("August 2026", InvoiceStatus.FINALIZE);
    }

    @Test
    void testGetAllInvoices_DefaultActiveBillCycleMonthWhenNotProvided() {
        when(jdbcTemplate.queryForList("SELECT bill_month, bill_year, bill_cycle FROM dbadmin.ncre_bill_cycle WHERE is_current = 1 ORDER BY bill_cycle DESC"))
                .thenReturn(List.of(Map.of("bill_month", 9, "bill_year", 2026)));

        Invoice inv = new Invoice();
        inv.setId(2L);
        inv.setInvoiceMonth("September 2026");
        inv.setStatus(InvoiceStatus.DRAFT);

        when(invoiceRepository.findByInvoiceMonth("September 2026"))
                .thenReturn(List.of(inv));
        when(ncreDeveloperRepository.findAll()).thenReturn(Collections.emptyList());

        List<Invoice> result = invoiceService.getAllInvoices(null, null, "Admin", null, null);

        assertEquals(1, result.size());
        assertEquals("September 2026", result.get(0).getInvoiceMonth());
        verify(invoiceRepository).findByInvoiceMonth("September 2026");
    }

    @Test
    void testGetAvailableFinalizedMonths_IncludesActiveCycleAndDbMonths() {
        when(jdbcTemplate.queryForList("SELECT bill_month, bill_year, bill_cycle FROM dbadmin.ncre_bill_cycle WHERE is_current = 1 ORDER BY bill_cycle DESC"))
                .thenReturn(List.of(Map.of("bill_month", 9, "bill_year", 2026)));

        when(invoiceRepository.findDistinctInvoiceMonthsByStatus(InvoiceStatus.FINALIZE))
                .thenReturn(List.of("August 2026", "July 2026"));

        List<String> months = invoiceService.getAvailableFinalizedMonths();

        assertTrue(months.contains("September 2026"));
        assertTrue(months.contains("August 2026"));
        assertTrue(months.contains("July 2026"));
        // Sorted newest first
        assertEquals("September 2026", months.get(0));
        assertEquals("August 2026", months.get(1));
        assertEquals("July 2026", months.get(2));
    }
}
