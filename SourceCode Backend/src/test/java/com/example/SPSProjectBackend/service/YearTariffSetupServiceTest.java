package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.repository.NcreDevTariffRateRepository;
import com.example.SPSProjectBackend.repository.NcreDeveloperRepository;
import com.example.SPSProjectBackend.repository.UserAccSecInfoRepository;
import com.example.SPSProjectBackend.util.SessionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class YearTariffSetupServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private NcreDevTariffRateRepository rateRepository;

    @Mock
    private NcreDeveloperRepository developerRepository;

    @Mock
    private UserAccSecInfoRepository userRepository;

    @Mock
    private SessionUtils sessionUtils;

    @InjectMocks
    private YearTariffSetupService service;

    @Test
    void processesAcTariffChangesAtFebruaryAndSeptemberCycleStartsOnly() {
        when(rateRepository.findAll()).thenReturn(Collections.emptyList());

        service.processBillCycleStarted(2026, 2);
        service.processBillCycleStarted(2026, 9);
        service.processBillCycleStarted(2026, 8);

        verify(rateRepository, times(2)).findAll();
    }

    @Test
    void rollsTariffsOverUsingTheJanuaryCycleYear() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(2025);
        when(rateRepository.findAll()).thenReturn(Collections.emptyList());

        service.processBillCycleStarted(2026, 1);

        verify(rateRepository).findAll();
    }
}