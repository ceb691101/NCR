package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.repository.NcreTariffDescriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NcreTariffDescriptionServiceTest {

    @Mock
    private NcreTariffDescriptionRepository tariffDescriptionRepository;

    @InjectMocks
    private NcreTariffDescriptionService tariffDescriptionService;

    @Test
    void testGetTariffDescriptions_Success() {
        List<String> mockData = Arrays.asList(
                "Avoided Cost (AC)",
                "TTT (3 years)",
                "TTT (5 years)",
                "FLAT Tariff",
                "  15+ (2019)  ",
                "15+ New (2022)"
        );
        when(tariffDescriptionRepository.findActiveTariffDescriptions()).thenReturn(mockData);

        List<String> result = tariffDescriptionService.getTariffDescriptions();

        assertNotNull(result);
        assertEquals(6, result.size());
        assertTrue(result.contains("15+ (2019)")); // trimmed
        assertTrue(result.contains("Avoided Cost (AC)"));
        assertTrue(result.contains("TTT (3 years)"));
        verify(tariffDescriptionRepository, times(1)).findActiveTariffDescriptions();
    }

    @Test
    void testGetTariffDescriptions_FiltersNullAndEmptyAndDuplicates() {
        List<String> mockData = Arrays.asList(
                "TTT (3 years)",
                null,
                "",
                "   ",
                "TTT (3 years)",
                "FLAT Tariff"
        );
        when(tariffDescriptionRepository.findActiveTariffDescriptions()).thenReturn(mockData);

        List<String> result = tariffDescriptionService.getTariffDescriptions();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("FLAT Tariff", result.get(0));
        assertEquals("TTT (3 years)", result.get(1));
    }

    @Test
    void testGetTariffDescriptions_EmptyWhenNoActiveTariffs() {
        when(tariffDescriptionRepository.findActiveTariffDescriptions()).thenReturn(Collections.emptyList());

        List<String> result = tariffDescriptionService.getTariffDescriptions();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTariffDescriptions_NullRepoResponse() {
        when(tariffDescriptionRepository.findActiveTariffDescriptions()).thenReturn(null);

        List<String> result = tariffDescriptionService.getTariffDescriptions();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
