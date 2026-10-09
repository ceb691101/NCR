package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.ReadingStatusDTO;
import com.example.SPSProjectBackend.service.ReadingStatusService;
import com.example.SPSProjectBackend.service.SecInfoAuthService;
import com.example.SPSProjectBackend.service.HsbLocationService;
import com.example.SPSProjectBackend.util.SessionUtils;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/reading-status")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ReadingStatusController {

    @Autowired
    private ReadingStatusService readingStatusService;

    @Autowired
    private SecInfoAuthService secInfoAuthService;

    @Autowired
    private HsbLocationService locationService;

    @Autowired
    private SessionUtils sessionUtils;

    /**
     * Get reading status based on user's access level
     * This is the main endpoint that returns reading status according to user category
     */
    @PostMapping(value = "/user-reading-status", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getUserReadingStatus(@RequestBody Map<String, Object> request) {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            String sessionId = (String) request.get("session_id");
            String userId = (String) request.get("user_id");
            Boolean includeCustomerDetails = (Boolean) request.getOrDefault("include_customer_details", true);
            Boolean includeReadingDetails = (Boolean) request.getOrDefault("include_reading_details", false);

            // Validate input
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

            // Get reading status for user
            ReadingStatusDTO.ReadingStatusResponse response = readingStatusService.getReadingStatusForUser(
                    sessionId, userId, includeCustomerDetails, includeReadingDetails);

            // Convert to response map
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            responseMap.put("user_category", response.getUserCategory());
            responseMap.put("area_reading_status", response.getAreaReadingStatus());
            responseMap.put("summary", response.getSummary());
            responseMap.put("timestamp", response.getTimestamp() != null ? response.getTimestamp().toString() : LocalDateTime.now().toString());

            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                // Check if the error is due to authentication/authorization or data processing
                String message = response.getMessage();
                if (message != null && (message.contains("Session") || message.contains("Unauthorized") || message.contains("Access denied"))) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMap);
                } else {
                    // Data processing error, not authentication error
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
                }
            }

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve reading status: " + e.getMessage());
            responseMap.put("error", "INTERNAL_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get reading status for a specific area
     */
    @GetMapping(value = "/area/{areaCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getAreaReadingStatus(
            @PathVariable String areaCode,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id,
            @RequestParam(required = false, defaultValue = "true") boolean include_customer_details,
            @RequestParam(required = false, defaultValue = "false") boolean include_reading_details) {

        Map<String, Object> responseMap = new HashMap<>();

        try {
            // Validate session and access
            validateSessionAndAccess(session_id, user_id, null, null, areaCode);

            boolean isEE = sessionUtils.isEEUser(session_id, user_id);
            String eeFilter = isEE ? user_id : null;

            List<ReadingStatusDTO.AreaReadingStatusDTO> readingStatus = readingStatusService.getAreaReadingStatus(
                    areaCode, include_customer_details, include_reading_details, eeFilter);

            responseMap.put("success", true);
            responseMap.put("message", "Reading status retrieved successfully for area " + areaCode);
            responseMap.put("area_code", areaCode);
            responseMap.put("reading_status", readingStatus);
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve reading status for area " + areaCode);
            responseMap.put("error", e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get pending readings (customers without readings) for a specific area
     */
    @GetMapping(value = "/area/{areaCode}/pending", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getPendingReadingsForArea(
            @PathVariable String areaCode,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {

        Map<String, Object> responseMap = new HashMap<>();

        try {
            // Validate session and access
            validateSessionAndAccess(session_id, user_id, null, null, areaCode);

            boolean isEE = sessionUtils.isEEUser(session_id, user_id);
            String eeFilter = isEE ? user_id : null;

            ReadingStatusDTO.PendingReadingsDTO pendingReadings = readingStatusService.getPendingReadingsForArea(areaCode, eeFilter);

            responseMap.put("success", true);
            responseMap.put("message", "Pending readings retrieved successfully for area " + areaCode);
            responseMap.put("area_code", areaCode);
            responseMap.put("pending_readings", pendingReadings);
            responseMap.put("pending_count", pendingReadings.getPendingCount());
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve pending readings for area " + areaCode);
            responseMap.put("error", e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get completed readings (customers with readings) for a specific area
     */
    @GetMapping(value = "/area/{areaCode}/completed", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getCompletedReadingsForArea(
            @PathVariable String areaCode,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {

        Map<String, Object> responseMap = new HashMap<>();

        try {
            // Validate session and access
            validateSessionAndAccess(session_id, user_id, null, null, areaCode);

            boolean isEE = sessionUtils.isEEUser(session_id, user_id);
            String eeFilter = isEE ? user_id : null;

            ReadingStatusDTO.CompletedReadingsDTO completedReadings = readingStatusService.getCompletedReadingsForArea(areaCode, eeFilter);

            responseMap.put("success", true);
            responseMap.put("message", "Completed readings retrieved successfully for area " + areaCode);
            responseMap.put("area_code", areaCode);
            responseMap.put("completed_readings", completedReadings);
            responseMap.put("completed_count", completedReadings.getCompletedCount());
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve completed readings for area " + areaCode);
            responseMap.put("error", e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get reading status for a specific province
     */
    @GetMapping(value = "/province/{provinceCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getProvinceReadingStatus(
            @PathVariable String provinceCode,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id,
            @RequestParam(required = false, defaultValue = "true") boolean include_customer_details,
            @RequestParam(required = false, defaultValue = "false") boolean include_reading_details) {

        Map<String, Object> responseMap = new HashMap<>();

        try {
            List<ReadingStatusDTO.AreaReadingStatusDTO> readingStatus = readingStatusService.getProvinceReadingStatus(
                    provinceCode, include_customer_details, include_reading_details);

            // Calculate summary for province
            int totalCustomers = readingStatus.stream().mapToInt(ReadingStatusDTO.AreaReadingStatusDTO::getTotalCustomers).sum();
            int customersWithReadings = readingStatus.stream().mapToInt(ReadingStatusDTO.AreaReadingStatusDTO::getCustomersWithReadings).sum();
            int customersWithoutReadings = readingStatus.stream().mapToInt(ReadingStatusDTO.AreaReadingStatusDTO::getCustomersWithoutReadings).sum();

            responseMap.put("success", true);
            responseMap.put("message", "Reading status retrieved successfully for province " + provinceCode);
            responseMap.put("province_code", provinceCode);
            responseMap.put("reading_status", readingStatus);
            responseMap.put("total_areas", readingStatus.size());
            responseMap.put("total_customers", totalCustomers);
            responseMap.put("customers_with_readings", customersWithReadings);
            responseMap.put("customers_without_readings", customersWithoutReadings);
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve reading status for province " + provinceCode);
            responseMap.put("error", e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get reading status for a specific region
     */
    @GetMapping(value = "/region/{regionCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getRegionReadingStatus(
            @PathVariable String regionCode,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id,
            @RequestParam(required = false, defaultValue = "true") boolean include_customer_details,
            @RequestParam(required = false, defaultValue = "false") boolean include_reading_details) {

        Map<String, Object> responseMap = new HashMap<>();

        try {
            List<ReadingStatusDTO.AreaReadingStatusDTO> readingStatus = readingStatusService.getRegionReadingStatus(
                    regionCode, include_customer_details, include_reading_details);

            // Calculate summary for region
            int totalCustomers = readingStatus.stream().mapToInt(ReadingStatusDTO.AreaReadingStatusDTO::getTotalCustomers).sum();
            int customersWithReadings = readingStatus.stream().mapToInt(ReadingStatusDTO.AreaReadingStatusDTO::getCustomersWithReadings).sum();
            int customersWithoutReadings = readingStatus.stream().mapToInt(ReadingStatusDTO.AreaReadingStatusDTO::getCustomersWithoutReadings).sum();

            responseMap.put("success", true);
            responseMap.put("message", "Reading status retrieved successfully for region " + regionCode);
            responseMap.put("region_code", regionCode);
            responseMap.put("reading_status", readingStatus);
            responseMap.put("total_areas", readingStatus.size());
            responseMap.put("total_customers", totalCustomers);
            responseMap.put("customers_with_readings", customersWithReadings);
            responseMap.put("customers_without_readings", customersWithoutReadings);
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve reading status for region " + regionCode);
            responseMap.put("error", e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get reading status for all areas (Admin only)
     */
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getAllReadingStatus(
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id,
            @RequestParam(required = false, defaultValue = "false") boolean include_customer_details,
            @RequestParam(required = false, defaultValue = "false") boolean include_reading_details) {

        Map<String, Object> responseMap = new HashMap<>();

        try {
            List<ReadingStatusDTO.AreaReadingStatusDTO> readingStatus = readingStatusService.getAllAreasReadingStatus(
                    include_customer_details, include_reading_details);

            // Group by region for better organization
            Map<String, List<ReadingStatusDTO.AreaReadingStatusDTO>> groupedByRegion = readingStatus.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            rs -> rs.getRegionCode() != null ? rs.getRegionCode() : "UNKNOWN"
                    ));

            // Calculate overall summary
            int totalCustomers = readingStatus.stream().mapToInt(ReadingStatusDTO.AreaReadingStatusDTO::getTotalCustomers).sum();
            int customersWithReadings = readingStatus.stream().mapToInt(ReadingStatusDTO.AreaReadingStatusDTO::getCustomersWithReadings).sum();
            int customersWithoutReadings = readingStatus.stream().mapToInt(ReadingStatusDTO.AreaReadingStatusDTO::getCustomersWithoutReadings).sum();

            responseMap.put("success", true);
            responseMap.put("message", "All reading status retrieved successfully");
            responseMap.put("reading_status", readingStatus);
            responseMap.put("reading_status_by_region", groupedByRegion);
            responseMap.put("total_areas", readingStatus.size());
            responseMap.put("total_customers", totalCustomers);
            responseMap.put("customers_with_readings", customersWithReadings);
            responseMap.put("customers_without_readings", customersWithoutReadings);
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve all reading status");
            responseMap.put("error", e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get reading status summary for multiple areas
     */
    @PostMapping(value = "/summary", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getReadingStatusSummary(@RequestBody Map<String, Object> request) {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            List<String> areaCodes = (List<String>) request.get("area_codes");
            Boolean includeDetails = (Boolean) request.getOrDefault("include_details", false);

            if (areaCodes == null || areaCodes.isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Area codes are required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            List<ReadingStatusDTO.AreaReadingStatusDTO> summaryList = new ArrayList<>();

            for (String areaCode : areaCodes) {
                try {
                    List<ReadingStatusDTO.AreaReadingStatusDTO> areaStatus = readingStatusService.getAreaReadingStatus(
                            areaCode, includeDetails, false);
                    summaryList.addAll(areaStatus);
                } catch (Exception e) {
                    // Log error but continue with other areas
                    System.err.println("Failed to get reading status for area " + areaCode + ": " + e.getMessage());
                }
            }

            responseMap.put("success", true);
            responseMap.put("message", "Reading status summary retrieved successfully");
            responseMap.put("summary", summaryList);
            responseMap.put("total_areas", summaryList.size());
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve reading status summary");
            responseMap.put("error", e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Health check endpoint for reading status service
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            // Try to perform a simple operation
            readingStatusService.getAreaReadingStatus("00", false, false); // Non-existent area

            responseMap.put("status", "healthy");
            responseMap.put("message", "Reading status service is operational");
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("status", "unhealthy");
            responseMap.put("message", "Reading status service has issues: " + e.getMessage());
            responseMap.put("error", "SYSTEM_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(responseMap);
        }
    }

    // Helper method to validate session and access
    private void validateSessionAndAccess(String sessionId, String userId, String targetRegionCode, String targetProvinceCode, String targetAreaCode) {
        if (sessionId != null && userId != null) {
            // Validate session
            SecInfoLoginDTO.SessionValidationRequest validationRequest = new SecInfoLoginDTO.SessionValidationRequest();
            validationRequest.setSessionId(sessionId);
            validationRequest.setUserId(userId);
            SecInfoLoginDTO.SessionValidationResponse validationResponse = secInfoAuthService.validateSession(validationRequest);
            if (!validationResponse.getValid()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session");
            }

            // If area-specific, validate access
            if (targetAreaCode != null) {
                Optional<com.example.SPSProjectBackend.dto.HsbAreaDTO> areaOpt = locationService.getAreaByCode(targetAreaCode);
                if (areaOpt.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Area not found");
                }
                com.example.SPSProjectBackend.dto.HsbAreaDTO area = areaOpt.get();
                boolean hasAccess = sessionUtils.hasAreaAccess(sessionId, userId, area.getRegion(), area.getProvCode(), targetAreaCode);
                if (!hasAccess) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this area");
                }
            }
        }
    }
}