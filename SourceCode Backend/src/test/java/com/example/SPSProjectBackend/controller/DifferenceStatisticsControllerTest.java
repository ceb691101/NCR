package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.DifferenceStatisticsDTO.AccountDifferenceDTO;
import com.example.SPSProjectBackend.dto.DifferenceStatisticsDTO.DifferenceResponse;
import com.example.SPSProjectBackend.service.DifferenceStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DifferenceStatisticsControllerTest {

    @Mock
    private DifferenceStatisticsService differenceStatisticsService;

    @InjectMocks
    private DifferenceStatisticsController differenceStatisticsController;

    private Map<String, Object> validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new HashMap<>();
        validRequest.put("session_id", "test_session_123");
        validRequest.put("user_id", "admin_user");
        validRequest.put("area_code", "A1");
        validRequest.put("bill_cycle", "820");
    }

    @Test
    void testGetAreaDifferences_Success() {
        List<AccountDifferenceDTO> diffs = new ArrayList<>();
        diffs.add(new AccountDifferenceDTO("1234567890", "ABC Hydro", new BigDecimal("125.500")));
        diffs.add(new AccountDifferenceDTO("1234567891", "XYZ Solar", new BigDecimal("-42.250")));

        DifferenceResponse serviceResponse = new DifferenceResponse(
                true, "Difference data retrieved successfully", "A1", "820", diffs, LocalDateTime.now()
        );

        when(differenceStatisticsService.getAreaDifferences("test_session_123", "admin_user", "A1", "820"))
                .thenReturn(serviceResponse);

        ResponseEntity<Map<String, Object>> response = differenceStatisticsController.getAreaDifferences(validRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("A1", response.getBody().get("area_code"));
        assertEquals("820", response.getBody().get("active_bill_cycle"));
        assertEquals(2, ((List<?>) response.getBody().get("differences")).size());
    }

    @Test
    void testGetAreaDifferences_IntegerBillCycleHandledSafely() {
        Map<String, Object> requestWithIntBillCycle = new HashMap<>();
        requestWithIntBillCycle.put("session_id", "test_session_123");
        requestWithIntBillCycle.put("user_id", "admin_user");
        requestWithIntBillCycle.put("area_code", "A1");
        requestWithIntBillCycle.put("bill_cycle", 820); // Integer instead of String!

        DifferenceResponse serviceResponse = new DifferenceResponse(
                true, "Success", "A1", "820", new ArrayList<>(), LocalDateTime.now()
        );

        when(differenceStatisticsService.getAreaDifferences("test_session_123", "admin_user", "A1", "820"))
                .thenReturn(serviceResponse);

        ResponseEntity<Map<String, Object>> response = differenceStatisticsController.getAreaDifferences(requestWithIntBillCycle);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
    }

    @Test
    void testGetAreaDifferences_MissingSessionId_BadRequest() {
        validRequest.remove("session_id");

        ResponseEntity<Map<String, Object>> response = differenceStatisticsController.getAreaDifferences(validRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Session ID is required", response.getBody().get("message"));
    }

    @Test
    void testGetAreaDifferences_MissingUserId_BadRequest() {
        validRequest.remove("user_id");

        ResponseEntity<Map<String, Object>> response = differenceStatisticsController.getAreaDifferences(validRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("User ID is required", response.getBody().get("message"));
    }

    @Test
    void testGetAreaDifferences_MissingAreaCode_AggregatesAllPermittedAreas() {
        // area_code is optional now: omitting it means every area the user may read.
        validRequest.remove("area_code");

        List<AccountDifferenceDTO> diffs = new ArrayList<>();
        diffs.add(new AccountDifferenceDTO("1234567890", "ABC Hydro", new BigDecimal("10.000")));

        DifferenceResponse serviceResponse = new DifferenceResponse(
                true, "Difference data retrieved successfully", "ALL_PERMITTED", "820", diffs,
                LocalDateTime.now()
        );
        serviceResponse.setAreaCodes(List.of("A1", "A2"));

        when(differenceStatisticsService.getDifferencesForAreas("test_session_123", "admin_user", List.of(), "820"))
                .thenReturn(serviceResponse);

        ResponseEntity<Map<String, Object>> response = differenceStatisticsController.getAreaDifferences(validRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("ALL_PERMITTED", response.getBody().get("area_code"));
        assertEquals(List.of("A1", "A2"), response.getBody().get("area_codes"));
        assertEquals(1, ((List<?>) response.getBody().get("differences")).size());
    }

    @Test
    void testGetAreaDifferences_AllToken_AggregatesAllPermittedAreas() {
        validRequest.put("area_code", "ALL");
        validRequest.put("area_codes", List.of("A1", "A2"));

        DifferenceResponse serviceResponse = new DifferenceResponse(
                true, "Difference data retrieved successfully", "ALL_PERMITTED", "820",
                new ArrayList<>(), LocalDateTime.now()
        );
        serviceResponse.setAreaCodes(List.of("A1", "A2"));

        when(differenceStatisticsService.getDifferencesForAreas("test_session_123", "admin_user", List.of("A1", "A2"), "820"))
                .thenReturn(serviceResponse);

        ResponseEntity<Map<String, Object>> response = differenceStatisticsController.getAreaDifferences(validRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of("A1", "A2"), response.getBody().get("area_codes"));
    }
}
