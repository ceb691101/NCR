package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.model.Invoice;
import com.example.SPSProjectBackend.model.InvoiceStatus;
import com.example.SPSProjectBackend.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceControllerFilterTest {

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private InvoiceController invoiceController;

    @Test
    void testGetAllInvoices_PassesMonthAndStatusToService() {
        Invoice invoice = new Invoice();
        invoice.setId(10L);
        invoice.setInvoiceMonth("August 2026");
        invoice.setStatus(InvoiceStatus.FINALIZE);

        when(invoiceService.getAllInvoices("user1", "session1", "Director", "August 2026", "FINALIZE"))
                .thenReturn(List.of(invoice));

        ResponseEntity<List<Invoice>> response = invoiceController.getAllInvoices(
                "session1", "user1", "Director", "August 2026", "FINALIZE");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals("August 2026", response.getBody().get(0).getInvoiceMonth());
        verify(invoiceService).getAllInvoices("user1", "session1", "Director", "August 2026", "FINALIZE");
    }

    @Test
    void testGetAvailableFinalizedMonths_CallsService() {
        when(invoiceService.getAvailableFinalizedMonths())
                .thenReturn(List.of("September 2026", "August 2026"));

        ResponseEntity<List<String>> response = invoiceController.getAvailableFinalizedMonths();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().size());
        assertEquals("September 2026", response.getBody().get(0));
        assertEquals("August 2026", response.getBody().get(1));
        verify(invoiceService).getAvailableFinalizedMonths();
    }
}
