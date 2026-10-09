package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.ErrorStatisticsDTO;
import com.example.SPSProjectBackend.dto.ErrorStatisticsDTO.*;
import com.example.SPSProjectBackend.service.ErrorStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/error-statistics")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ErrorStatisticsController {

    @Autowired
    private ErrorStatisticsService errorStatisticsService;

    /**
     * Get error statistics for an area
     */
    @PostMapping(value = "/area-statistics", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getErrorStatistics(@RequestBody Map<String, Object> request) {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            String sessionId = (String) request.get("session_id");
            String userId = (String) request.get("user_id");
            String areaCode = (String) request.get("area_code");
            String billCycle = (String) request.get("bill_cycle");

            // Validate required parameters
            if (sessionId == null || sessionId.trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Session ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            if (userId == null || userId.trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "User ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            // area_code is optional. Blank / "ALL" aggregates every permitted area, which
            // is the default state after login.
            ErrorStatsResponse response;
            if (areaCode == null || areaCode.trim().isEmpty() || "ALL".equalsIgnoreCase(areaCode.trim())) {
                response = errorStatisticsService.getErrorStatisticsForAreas(
                        sessionId, userId, extractAreaCodes(request.get("area_codes")), billCycle);
            } else {
                response = errorStatisticsService.getErrorStatistics(
                        sessionId, userId, areaCode, billCycle);
            }

            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            responseMap.put("error_statistics", response.getErrorStatistics());
            responseMap.put("timestamp", response.getTimestamp() != null ? response.getTimestamp().toString()
                    : LocalDateTime.now().toString());

            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve error statistics: " + e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Error details for every area in scope, used when no single area is selected.
 * Each account carries its own area and bill cycle.
 */
    @PostMapping(value = "/error-details-all", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getErrorDetailsAllAreas(@RequestBody Map<String, Object> request) {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            String sessionId = request.get("session_id") != null
                    ? request.get("session_id").toString().trim() : null;
            String userId = request.get("user_id") != null
                    ? request.get("user_id").toString().trim() : null;
            String billCycle = request.get("bill_cycle") != null
                    ? request.get("bill_cycle").toString().trim() : null;

            Integer errorCode = null;
            Object rawErrorCode = request.get("error_code");
            if (rawErrorCode instanceof Number number) {
                errorCode = number.intValue();
            } else if (rawErrorCode != null && !rawErrorCode.toString().trim().isEmpty()) {
                errorCode = Integer.parseInt(rawErrorCode.toString().trim());
            }

            if (sessionId == null || sessionId.isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Session ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }
            if (userId == null || userId.isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "User ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }
            if (errorCode == null) {
                responseMap.put("success", false);
                responseMap.put("message", "Error code is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            ErrorDetailsResponse response = errorStatisticsService.getErrorDetailsForAreas(
                    sessionId, userId, errorCode, billCycle);

            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            responseMap.put("error_code", response.getErrorCode());
            responseMap.put("error_name", response.getErrorName());
            responseMap.put("accounts_with_error",
                    response.getAccountsWithError() != null ? response.getAccountsWithError() : List.of());
            responseMap.put("total_accounts", response.getTotalAccounts());
            responseMap.put("timestamp", response.getTimestamp() != null
                    ? response.getTimestamp().toString() : LocalDateTime.now().toString());

            if (Boolean.TRUE.equals(response.getSuccess())) {
                return ResponseEntity.ok(responseMap);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve error details: " + e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Reads an area_codes payload that may arrive as a List or a comma separated string.
     */
    private List<String> extractAreaCodes(Object raw) {
        List<String> codes = new ArrayList<>();
        if (raw == null) {
            return codes;
        }
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                if (item != null && !item.toString().trim().isEmpty()) {
                    codes.add(item.toString().trim());
                }
            }
        } else if (raw instanceof String text) {
            for (String part : text.split(",")) {
                if (!part.trim().isEmpty()) {
                    codes.add(part.trim());
                }
            }
        }
        return codes;
    }

    /**
     * Get detailed accounts for a specific error code
     */
    @PostMapping(value = "/error-details", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getErrorDetails(@RequestBody Map<String, Object> request) {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            String sessionId = (String) request.get("session_id");
            String userId = (String) request.get("user_id");
            String areaCode = (String) request.get("area_code");
            String billCycle = (String) request.get("bill_cycle");
            Integer errorCode = (Integer) request.get("error_code");

            // Validate required parameters
            if (sessionId == null || sessionId.trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Session ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            if (userId == null || userId.trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "User ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            if (areaCode == null || areaCode.trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Area code is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            if (errorCode == null) {
                responseMap.put("success", false);
                responseMap.put("message", "Error code is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            ErrorDetailsResponse response = errorStatisticsService.getErrorDetails(
                    sessionId, userId, areaCode, billCycle, errorCode);

            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            responseMap.put("error_code", response.getErrorCode());
            responseMap.put("error_name", response.getErrorName());
            responseMap.put("accounts_with_error", response.getAccountsWithError());
            responseMap.put("total_accounts", response.getTotalAccounts());
            responseMap.put("timestamp", response.getTimestamp() != null ? response.getTimestamp().toString()
                    : LocalDateTime.now().toString());

            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve error details: " + e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get error statistics summary for multiple areas (for regional/provincial
     * users)
     */
    @PostMapping(value = "/bulk-statistics", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getBulkErrorStatistics(@RequestBody Map<String, Object> request) {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            String sessionId = (String) request.get("session_id");
            String userId = (String) request.get("user_id");
            @SuppressWarnings("unchecked")
            java.util.List<String> areaCodes = (java.util.List<String>) request.get("area_codes");

            // Validate required parameters
            if (sessionId == null || sessionId.trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Session ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            if (userId == null || userId.trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "User ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            if (areaCodes == null || areaCodes.isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Area codes are required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            // Get statistics for each area
            java.util.List<ErrorStatisticsData> allStatistics = new java.util.ArrayList<>();
            int totalAreasProcessed = 0;

            for (String areaCode : areaCodes) {
                try {
                    ErrorStatsResponse response = errorStatisticsService.getErrorStatistics(
                            sessionId, userId, areaCode, null);

                    if (response.getSuccess() && response.getErrorStatistics() != null) {
                        allStatistics.add(response.getErrorStatistics());
                        totalAreasProcessed++;
                    }
                } catch (Exception e) {
                    // Log error but continue with other areas
                    System.err.println("Failed to get error statistics for area " + areaCode + ": " + e.getMessage());
                }
            }

            responseMap.put("success", true);
            responseMap.put("message", "Bulk error statistics retrieved successfully");
            responseMap.put("area_statistics", allStatistics);
            responseMap.put("total_areas_processed", totalAreasProcessed);
            responseMap.put("total_areas_requested", areaCodes.size());
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve bulk error statistics: " + e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());
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
            responseMap.put("message", "Error statistics service is operational");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("status", "unhealthy");
            responseMap.put("message", "Error statistics service has issues: " + e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(responseMap);
        }
    }

    /**
     * Get error code mapping (for frontend reference)
     */
    @GetMapping(value = "/error-codes", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getErrorCodes() {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            Map<String, String> errorCodes = new HashMap<>();
            errorCodes.put("1", "High Consumption");
            errorCodes.put("2", "Low Consumption");
            errorCodes.put("3", "Reading Error");
            errorCodes.put("4", "Charge Error");
            errorCodes.put("5", "Negative Error");
            errorCodes.put("6", "Total Charge Error");
            errorCodes.put("7", "Zero Consumption");

            responseMap.put("success", true);
            responseMap.put("message", "Error codes retrieved successfully");
            responseMap.put("error_codes", errorCodes);
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve error codes: " + e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get detailed accounts with complete meter reading information for a specific
     * error code
     */
    @PostMapping(value = "/error-details-with-readings", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getErrorDetailsWithMeterReadings(
            @RequestBody Map<String, Object> request) {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            String sessionId = (String) request.get("session_id");
            String userId = (String) request.get("user_id");
            String areaCode = (String) request.get("area_code");
            String billCycle = (String) request.get("bill_cycle");
            Integer errorCode = (Integer) request.get("error_code");

            // Validate required parameters
            if (sessionId == null || sessionId.trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Session ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            if (userId == null || userId.trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "User ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            if (areaCode == null || areaCode.trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Area code is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            if (errorCode == null) {
                responseMap.put("success", false);
                responseMap.put("message", "Error code is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            ErrorDetailsWithReadingsResponse response = errorStatisticsService.getErrorDetailsWithMeterReadings(
                    sessionId, userId, areaCode, billCycle, errorCode);

            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            responseMap.put("error_code", response.getErrorCode());
            responseMap.put("error_name", response.getErrorName());
            responseMap.put("area_code", response.getAreaCode());
            responseMap.put("area_name", response.getAreaName());
            responseMap.put("active_bill_cycle", response.getActiveBillCycle());
            responseMap.put("accounts_with_error", response.getAccountsWithError());
            responseMap.put("total_accounts", response.getTotalAccounts());
            responseMap.put("timestamp", response.getTimestamp() != null ? response.getTimestamp().toString()
                    : LocalDateTime.now().toString());

            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve error details with meter readings: " + e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }
}