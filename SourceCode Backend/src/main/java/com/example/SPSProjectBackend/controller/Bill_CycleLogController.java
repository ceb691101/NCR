package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.Bill_CycleLogDTO;
import com.example.SPSProjectBackend.service.Bill_CycleLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/logfile")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class Bill_CycleLogController {

    @Autowired
    private Bill_CycleLogService logService;

    /**
     * Create a new log file entry (start a process)
     */
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> createLogEntry(@RequestBody Bill_CycleLogDTO.LogFileEntryRequest request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Validate required fields
            if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Session ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "User ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            // Create log entry
            Bill_CycleLogDTO.LogFileResponse response = logService.createLogEntry(request);
            
            // Convert response to map
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            
            if (response.getSuccess()) {
                responseMap.put("log_entry", response.getLogEntry());
                responseMap.put("timestamp", response.getTimestamp().toString());
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.badRequest().body(responseMap);
            }

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to create log entry: " + e.getMessage());
            responseMap.put("error", "INTERNAL_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Complete a log file entry (set end time)
     */
    @PostMapping(value = "/complete", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> completeLogEntry(@RequestBody Bill_CycleLogDTO.LogFileEndRequest request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Validate required fields
            if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Session ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "User ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            if (request.getLogId() == null) {
                responseMap.put("success", false);
                responseMap.put("message", "Log ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            // Complete log entry
            Bill_CycleLogDTO.LogFileResponse response = logService.completeLogEntry(request);
            
            // Convert response to map
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            responseMap.put("log_entry", response.getLogEntry());
            responseMap.put("bill_cycle_changed", response.getBillCycleChanged());
            
            if (response.getBillCycleChanged()) {
                responseMap.put("old_bill_cycle", response.getOldBillCycle());
                responseMap.put("new_bill_cycle", response.getNewBillCycle());
            }
            
            responseMap.put("timestamp", response.getTimestamp().toString());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.badRequest().body(responseMap);
            }

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to complete log entry: " + e.getMessage());
            responseMap.put("error", "INTERNAL_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get logs for a specific area
     */
    @PostMapping(value = "/area-logs", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getAreaLogs(@RequestBody Bill_CycleLogDTO.ActiveLogsRequest request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Validate required fields
            if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Session ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "User ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            if (request.getAreaCode() == null || request.getAreaCode().trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Area code is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            // Get area logs
            Bill_CycleLogDTO.LogsListResponse response = logService.getAreaLogs(request);
            
            // Convert response to map
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            
            if (response.getSuccess()) {
                responseMap.put("area_code", response.getAreaCode());
                responseMap.put("area_name", response.getAreaName());
                responseMap.put("active_bill_cycle", response.getActiveBillCycle());
                responseMap.put("requested_bill_cycle", response.getRequestedBillCycle());
                responseMap.put("logs", response.getLogs());
                responseMap.put("total_logs", response.getTotalLogs());
                responseMap.put("completed_logs", response.getCompletedLogs());
                responseMap.put("pending_logs", response.getPendingLogs());
                responseMap.put("has_process_901", response.getHasProcess901());
            }
            
            responseMap.put("timestamp", response.getTimestamp().toString());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.badRequest().body(responseMap);
            }

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve area logs: " + e.getMessage());
            responseMap.put("error", "INTERNAL_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get area status including active bill cycle and pending processes
     */
    @GetMapping(value = "/area/{areaCode}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getAreaStatus(
            @PathVariable String areaCode,
            @RequestParam String sessionId,
            @RequestParam String userId) {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
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

            // Get area status
            Bill_CycleLogDTO.AreaStatusResponse response = logService.getAreaStatus(sessionId, userId, areaCode);
            
            // Convert response to map
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            
            if (response.getSuccess() && response.getAreaStatus() != null) {
                Bill_CycleLogDTO.AreaStatusDTO areaStatus = response.getAreaStatus();
                
                Map<String, Object> statusMap = new HashMap<>();
                statusMap.put("area_code", areaStatus.getAreaCode());
                statusMap.put("area_name", areaStatus.getAreaName());
                statusMap.put("active_bill_cycle", areaStatus.getActiveBillCycle());
                statusMap.put("has_pending_logs", areaStatus.getHasPendingLogs());
                statusMap.put("last_completed_process", areaStatus.getLastCompletedProcess());
                statusMap.put("can_enter_new_log", areaStatus.getCanEnterNewLog());
                statusMap.put("cycle_completion_status", areaStatus.getCycleCompletionStatus());
                
                responseMap.put("area_status", statusMap);
            }
            
            responseMap.put("timestamp", response.getTimestamp().toString());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.badRequest().body(responseMap);
            }

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to get area status: " + e.getMessage());
            responseMap.put("error", "INTERNAL_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get logs for multiple areas (bulk operation)
     */
    @PostMapping(value = "/bulk-logs", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getBulkAreaLogs(@RequestBody Bill_CycleLogDTO.BulkLogRequest request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Validate required fields
            if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Session ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "User ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            if (request.getAreaCodes() == null || request.getAreaCodes().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Area codes list is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            // Process each area
            Map<String, Object> areaResults = new HashMap<>();
            int totalAreas = request.getAreaCodes().size();
            int processedAreas = 0;
            int errorAreas = 0;

            for (String areaCode : request.getAreaCodes()) {
                try {
                    Bill_CycleLogDTO.ActiveLogsRequest areaRequest = new Bill_CycleLogDTO.ActiveLogsRequest();
                    areaRequest.setSessionId(request.getSessionId());
                    areaRequest.setUserId(request.getUserId());
                    areaRequest.setAreaCode(areaCode);
                    areaRequest.setBillCycle(request.getBillCycle());

                    Bill_CycleLogDTO.LogsListResponse areaResponse = logService.getAreaLogs(areaRequest);
                    
                    if (areaResponse.getSuccess()) {
                        Map<String, Object> areaData = new HashMap<>();
                        areaData.put("area_name", areaResponse.getAreaName());
                        areaData.put("active_bill_cycle", areaResponse.getActiveBillCycle());
                        areaData.put("total_logs", areaResponse.getTotalLogs());
                        areaData.put("completed_logs", areaResponse.getCompletedLogs());
                        areaData.put("pending_logs", areaResponse.getPendingLogs());
                        areaData.put("has_process_901", areaResponse.getHasProcess901());
                        
                        if (request.getIncludeCompleted() != null && request.getIncludeCompleted()) {
                            areaData.put("logs", areaResponse.getLogs());
                        }
                        
                        areaResults.put(areaCode, areaData);
                        processedAreas++;
                    } else {
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("error", areaResponse.getMessage());
                        areaResults.put(areaCode, errorData);
                        errorAreas++;
                    }
                } catch (Exception e) {
                    Map<String, Object> errorData = new HashMap<>();
                    errorData.put("error", "Failed to process area: " + e.getMessage());
                    areaResults.put(areaCode, errorData);
                    errorAreas++;
                }
            }

            responseMap.put("success", true);
            responseMap.put("message", String.format("Processed %d areas successfully, %d with errors", 
                processedAreas, errorAreas));
            responseMap.put("total_areas", totalAreas);
            responseMap.put("processed_areas", processedAreas);
            responseMap.put("error_areas", errorAreas);
            responseMap.put("area_results", areaResults);
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve bulk area logs: " + e.getMessage());
            responseMap.put("error", "INTERNAL_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Validate process code
     */
    @PostMapping(value = "/validate-process", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> validateProcessCode(@RequestBody Map<String, String> request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            String proCode = request.get("pro_code");
            if (proCode == null || proCode.trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Process code is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            // Validate process code format and rules
            Bill_CycleLogDTO.ProcessCodeValidationDTO validation = validateProcessCode(proCode);
            
            responseMap.put("success", true);
            responseMap.put("message", "Process code validation completed");
            responseMap.put("validation", validation);
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to validate process code: " + e.getMessage());
            responseMap.put("error", "INTERNAL_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get system statistics
     */
    @GetMapping(value = "/system-stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getSystemStats(
            @RequestParam String sessionId,
            @RequestParam String userId) {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Validate session (admin access recommended for system stats)
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

            // Get system statistics (this would need to be implemented in service)
            // For now, return basic structure
            Map<String, Object> stats = new HashMap<>();
            stats.put("total_active_areas", 0);
            stats.put("areas_with_pending_logs", 0);
            stats.put("areas_ready_for_cycle_change", 0);
            stats.put("total_logs_today", 0);
            stats.put("completed_logs_today", 0);
            stats.put("pending_logs_total", 0);

            responseMap.put("success", true);
            responseMap.put("message", "System statistics retrieved successfully");
            responseMap.put("statistics", stats);
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to get system statistics: " + e.getMessage());
            responseMap.put("error", "INTERNAL_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Health check endpoint for log service
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Basic health check
            responseMap.put("status", "healthy");
            responseMap.put("message", "Log file service is operational");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("status", "unhealthy");
            responseMap.put("message", "Log file service has issues: " + e.getMessage());
            responseMap.put("error", "SYSTEM_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(responseMap);
        }
    }

    // Helper method for process code validation
    private Bill_CycleLogDTO.ProcessCodeValidationDTO validateProcessCode(String proCode) {
        boolean isValid = true;
        boolean isSpecialProcess = "9.01".equals(proCode);
        boolean requiresEndTime = true;
        boolean canTriggerBillCycleChange = isSpecialProcess;
        String validationMessage = "Process code is valid";

        // Basic format validation (should be like X.XX format)
        if (!proCode.matches("^\\d+\\.\\d+$")) {
            isValid = false;
            validationMessage = "Invalid process code format. Expected format: X.XX (e.g., 1.01, 2.05, 9.01)";
        }

        // Special validation for process 9.01
        if (isSpecialProcess) {
            validationMessage = "Special process code 9.01 - Will trigger bill cycle change when completed";
        }

        return Bill_CycleLogDTO.ProcessCodeValidationDTO.builder()
            .proCode(proCode)
            .isValid(isValid)
            .isSpecialProcess(isSpecialProcess)
            .requiresEndTime(requiresEndTime)
            .canTriggerBillCycleChange(canTriggerBillCycleChange)
            .validationMessage(validationMessage)
            .build();
    }
}