package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.model.NcreBillCycle;
import com.example.SPSProjectBackend.model.YrMnth;
import com.example.SPSProjectBackend.repository.NcreBillCycleRepository;
import com.example.SPSProjectBackend.repository.YrMnthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceResolveInvoiceMonthTest {

    @Mock
    private NcreBillCycleRepository ncreBillCycleRepository;

    @Mock
    private YrMnthRepository yrMnthRepository;

    @Mock
    private InvoiceCalculationService invoiceCalculationService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private BillCycleService billCycleService;

    @InjectMocks
    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testResolveInvoiceMonth_FromActiveBillCycle_September2026() {
        when(jdbcTemplate.queryForList("SELECT bill_month, bill_year, bill_cycle FROM dbadmin.ncre_bill_cycle WHERE is_current = 1 ORDER BY bill_cycle DESC"))
                .thenReturn(List.of(Map.of("bill_month", 9, "bill_year", 2026)));

        // Even when an area-specific reading cycle like 4 is passed, active cycle yields September 2026
        YearMonth ym = invoiceService.resolveInvoiceMonth(4);
        assertEquals(YearMonth.of(2026, 9), ym);
    }

    @Test
    void testResolveInvoiceMonth_FromActiveBillCycle_NullBillCycle() {
        when(jdbcTemplate.queryForList("SELECT bill_month, bill_year, bill_cycle FROM dbadmin.ncre_bill_cycle WHERE is_current = 1 ORDER BY bill_cycle DESC"))
                .thenReturn(List.of(Map.of("bill_month", 9, "bill_year", 2026)));

        YearMonth ym = invoiceService.resolveInvoiceMonth(null);
        assertEquals(YearMonth.of(2026, 9), ym);
    }

    @Test
    void testResolveInvoiceMonth_FromActiveBillCycleRepository_WhenJdbcEmpty() {
        when(jdbcTemplate.queryForList("SELECT bill_month, bill_year, bill_cycle FROM dbadmin.ncre_bill_cycle WHERE is_current = 1 ORDER BY bill_cycle DESC"))
                .thenReturn(Collections.emptyList());
        when(jdbcTemplate.queryForList("SELECT bill_month, bill_year, bill_cycle FROM ncre_bill_cycle WHERE is_current = 1 ORDER BY bill_cycle DESC"))
                .thenReturn(Collections.emptyList());

        NcreBillCycle activeCycle = NcreBillCycle.builder()
                .billCycle((short) 821)
                .billYear((short) 2026)
                .billMonth((short) 9)
                .isCurrent((short) 1)
                .build();
        when(ncreBillCycleRepository.findCurrentBillCycle()).thenReturn(Optional.of(activeCycle));

        YearMonth ym = invoiceService.resolveInvoiceMonth(null);
        assertEquals(YearMonth.of(2026, 9), ym);
    }

    @Test
    void testResolveInvoiceMonth_FromNcreBillCycle_August2026() {
        NcreBillCycle cycle = NcreBillCycle.builder()
                .billCycle((short) 820)
                .billYear((short) 2026)
                .billMonth((short) 8)
                .build();

        when(ncreBillCycleRepository.findById((short) 820)).thenReturn(Optional.of(cycle));

        YearMonth ym = invoiceService.resolveInvoiceMonth(820);
        assertEquals(YearMonth.of(2026, 8), ym);
    }

    @Test
    void testResolveInvoiceMonth_FromNcreBillCycle_September2026() {
        NcreBillCycle cycle = NcreBillCycle.builder()
                .billCycle((short) 821)
                .billYear((short) 2026)
                .billMonth((short) 9)
                .build();

        when(ncreBillCycleRepository.findById((short) 821)).thenReturn(Optional.of(cycle));

        YearMonth ym = invoiceService.resolveInvoiceMonth(821);
        assertEquals(YearMonth.of(2026, 9), ym);
    }

    @Test
    void testResolveInvoiceMonth_FromYrMnth_WhenNcreBillCycleNotFound() {
        when(ncreBillCycleRepository.findById((short) 819)).thenReturn(Optional.empty());

        YrMnth yrMnth = new YrMnth();
        yrMnth.setBillCycle(819);
        yrMnth.setBillMnth("2026 JUL");
        when(yrMnthRepository.findById(819)).thenReturn(Optional.of(yrMnth));

        YearMonth ym = invoiceService.resolveInvoiceMonth(819);
        assertEquals(YearMonth.of(2026, 7), ym);
    }

    @Test
    void testResolveInvoiceMonth_FromNumericYyyyMm() {
        // e.g. 202608
        YearMonth ym = invoiceService.resolveInvoiceMonth(202608);
        assertEquals(YearMonth.of(2026, 8), ym);
    }

    @Test
    void testResolveInvoiceMonth_FallbackToDetermineInvoiceMonth() {
        when(ncreBillCycleRepository.findById((short) 999)).thenReturn(Optional.empty());
        when(yrMnthRepository.findById(999)).thenReturn(Optional.empty());
        when(invoiceCalculationService.determineInvoiceMonth()).thenReturn(YearMonth.of(2026, 1));

        YearMonth ym = invoiceService.resolveInvoiceMonth(999);
        assertEquals(YearMonth.of(2026, 1), ym);
    }

    @Test
    void testResolveInvoiceMonth_NullBillCycleFallback() {
        when(invoiceCalculationService.determineInvoiceMonth()).thenReturn(YearMonth.of(2026, 5));

        YearMonth ym = invoiceService.resolveInvoiceMonth(null);
        assertEquals(YearMonth.of(2026, 5), ym);
    }
}
