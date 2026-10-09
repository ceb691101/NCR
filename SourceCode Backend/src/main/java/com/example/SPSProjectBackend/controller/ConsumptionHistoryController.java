package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.ConsumptionHistoryDTO;
import com.example.SPSProjectBackend.dto.ConsumptionHistoryDTO.*;
import com.example.SPSProjectBackend.service.ConsumptionHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/consumption-history")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ConsumptionHistoryController {

    @Autowired
    private ConsumptionHistoryService consumptionHistoryService;

    /**
     * Get consumption history for a single customer
     */
    @PostMapping(value = "/customer", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getConsumptionHistory(@RequestBody ConsumptionRequest request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            ConsumptionResponse response = consumptionHistoryService.getConsumptionHistory(
                request.getSessionId(), 
                request.getUserId(), 
                request.getAccountNumber(), 
                request.getCycleCount(), 
                request.getAreaCode()
            );
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            
            if (response.getSuccess() && response.getConsumptionHistory() != null) {
                responseMap.put("consumption_history", response.getConsumptionHistory());
            }
            
            responseMap.put("timestamp", response.getTimestamp());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
            
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve consumption history: " + e.getMessage());
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get consumption history for multiple customers
     */
    @PostMapping(value = "/bulk", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getConsumptionHistoryForMultipleAccounts(@RequestBody MultipleAccountsRequest request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            MultipleAccountsResponse response = consumptionHistoryService.getConsumptionHistoryForMultipleAccounts(
                request.getSessionId(), 
                request.getUserId(), 
                request.getAccountNumbers(), 
                request.getCycleCount(), 
                request.getAreaCode()
            );
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            
            if (response.getSuccess()) {
                responseMap.put("total_accounts", response.getTotalAccounts());
                responseMap.put("accounts_with_data", response.getAccountsWithData());
                responseMap.put("accounts_without_data", response.getAccountsWithoutData());
                
                if (response.getConsumptionHistories() != null && !response.getConsumptionHistories().isEmpty()) {
                    responseMap.put("consumption_histories", response.getConsumptionHistories());
                }
            }
            
            responseMap.put("timestamp", response.getTimestamp());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
            
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve consumption history for multiple accounts: " + e.getMessage());
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get consumption history for all customers in an area
     */
    @GetMapping(value = "/area/{areaCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getConsumptionHistoryForArea(
            @PathVariable String areaCode,
            @RequestParam String session_id,
            @RequestParam String user_id,
            @RequestParam(required = false, defaultValue = "12") Integer cycle_count) {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Validate cycle count
            if (cycle_count != 3 && cycle_count != 6 && cycle_count != 12) {
                responseMap.put("success", false);
                responseMap.put("message", "Invalid cycle count. Valid values are: 3, 6, 12");
                return ResponseEntity.badRequest().body(responseMap);
            }

            MultipleAccountsResponse response = consumptionHistoryService.getConsumptionHistoryForArea(
                session_id, 
                user_id, 
                areaCode, 
                cycle_count
            );
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            
            if (response.getSuccess()) {
                responseMap.put("area_code", areaCode);
                responseMap.put("total_accounts", response.getTotalAccounts());
                responseMap.put("accounts_with_data", response.getAccountsWithData());
                responseMap.put("accounts_without_data", response.getAccountsWithoutData());
                
                if (response.getConsumptionHistories() != null && !response.getConsumptionHistories().isEmpty()) {
                    responseMap.put("consumption_histories", response.getConsumptionHistories());
                    
                    // Add area summary statistics
                    Map<String, Object> areaSummary = calculateAreaSummary(response.getConsumptionHistories());
                    responseMap.put("area_summary", areaSummary);
                }
            }
            
            responseMap.put("timestamp", response.getTimestamp());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
            
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve consumption history for area: " + e.getMessage());
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get consumption history with flexible bill cycles
     * This endpoint allows specifying specific bill cycles or a range
     */
    @PostMapping(value = "/customer/flexible", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getFlexibleConsumptionHistory(
            @RequestBody Map<String, Object> request) {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            String sessionId = (String) request.get("session_id");
            String userId = (String) request.get("user_id");
            String accountNumber = (String) request.get("account_number");
            String areaCode = (String) request.get("area_code");
            
            // Validate required parameters
            if (sessionId == null || userId == null || accountNumber == null) {
                responseMap.put("success", false);
                responseMap.put("message", "Session ID, User ID, and Account Number are required");
                return ResponseEntity.badRequest().body(responseMap);
            }
            
            // Default to 12 cycles if not specified
            Integer cycleCount = 12;
            if (request.get("cycle_count") != null) {
                cycleCount = (Integer) request.get("cycle_count");
            }
            
            // Call the service
            ConsumptionResponse response = consumptionHistoryService.getConsumptionHistory(
                sessionId, userId, accountNumber, cycleCount, areaCode
            );
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            
            if (response.getSuccess() && response.getConsumptionHistory() != null) {
                responseMap.put("consumption_history", response.getConsumptionHistory());
            }
            
            responseMap.put("timestamp", response.getTimestamp());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
            
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve consumption history: " + e.getMessage());
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            responseMap.put("status", "healthy");
            responseMap.put("message", "Consumption history service is operational");
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.ok(responseMap);
            
        } catch (Exception e) {
            responseMap.put("status", "unhealthy");
            responseMap.put("message", "Consumption history service has issues: " + e.getMessage());
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(responseMap);
        }
    }

    /**
     * Helper method to calculate area summary statistics
     */
    private Map<String, Object> calculateAreaSummary(java.util.List<ConsumptionSummaryDTO> histories) {
        Map<String, Object> summary = new HashMap<>();
        
        if (histories == null || histories.isEmpty()) {
            return summary;
        }
        
        // Calculate totals across all customers
        java.math.BigDecimal totalOffPeak = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalDay = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalPeak = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalKva = java.math.BigDecimal.ZERO;
        
        int customersWithData = 0;
        
        for (ConsumptionSummaryDTO history : histories) {
            if (history.getBillCycleCount() > 0) {
                customersWithData++;
                totalOffPeak = totalOffPeak.add(history.getTotalOffPeak());
                totalDay = totalDay.add(history.getTotalDay());
                totalPeak = totalPeak.add(history.getTotalPeak());
                totalKva = totalKva.add(history.getTotalKva());
            }
        }
        
        summary.put("customers_with_data", customersWithData);
        summary.put("total_area_off_peak", totalOffPeak);
        summary.put("total_area_day", totalDay);
        summary.put("total_area_peak", totalPeak);
        summary.put("total_area_kva", totalKva);
        
        if (customersWithData > 0) {
            java.math.BigDecimal customerCount = new java.math.BigDecimal(customersWithData);
            summary.put("average_off_peak_per_customer", totalOffPeak.divide(customerCount, 2, java.math.RoundingMode.HALF_UP));
            summary.put("average_day_per_customer", totalDay.divide(customerCount, 2, java.math.RoundingMode.HALF_UP));
            summary.put("average_peak_per_customer", totalPeak.divide(customerCount, 2, java.math.RoundingMode.HALF_UP));
            summary.put("average_kva_per_customer", totalKva.divide(customerCount, 2, java.math.RoundingMode.HALF_UP));
        }
        
        return summary;
    }
}