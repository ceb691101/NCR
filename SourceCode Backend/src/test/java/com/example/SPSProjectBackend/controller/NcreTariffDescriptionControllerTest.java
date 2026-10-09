package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.service.NcreTariffDescriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NcreTariffDescriptionControllerTest {

    @Mock
    private NcreTariffDescriptionService tariffDescriptionService;

    @InjectMocks
    private NcreTariffDescriptionController tariffDescriptionController;

    @Test
    void testGetTariffDescriptions_Success() {
        List<String> mockTariffs = Arrays.asList(
                "Avoided Cost (AC)",
                "FLAT Tariff",
                "TTT (3 years)",
                "TTT (5 years)"
        );
        when(tariffDescriptionService.getTariffDescriptions()).thenReturn(mockTariffs);

        ResponseEntity<List<String>> response = tariffDescriptionController.getTariffDescriptions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(4, response.getBody().size());
        assertEquals("Avoided Cost (AC)", response.getBody().get(0));
    }

    @Test
    void testGetTariffDescriptions_Empty() {
        when(tariffDescriptionService.getTariffDescriptions()).thenReturn(Collections.emptyList());

        ResponseEntity<List<String>> response = tariffDescriptionController.getTariffDescriptions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testGetTariffDescriptions_ServiceException() {
        when(tariffDescriptionService.getTariffDescriptions()).thenThrow(new RuntimeException("DB Connection error"));

        ResponseEntity<List<String>> response = tariffDescriptionController.getTariffDescriptions();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
