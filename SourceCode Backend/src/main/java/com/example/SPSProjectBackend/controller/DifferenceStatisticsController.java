package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.DifferenceStatisticsDTO.DifferenceResponse;
import com.example.SPSProjectBackend.service.DifferenceStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/difference-statistics")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:8097",
        "http://10.128.1.59:8097" }, allowCredentials = "true")
public class DifferenceStatisticsController {

    @Autowired
    private DifferenceStatisticsService differenceStatisticsService;

    /**
     * Get difference statistics for an area and bill cycle
     */
    @PostMapping(value = "/area-differences", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getAreaDifferences(@RequestBody Map<String, Object> request) {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            if (request == null) {
                responseMap.put("success", false);
                responseMap.put("message", "Request body is required");
                responseMap.put("differences", Collections.emptyList());
                responseMap.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.badRequest().body(responseMap);
            }

            String sessionId = request.get("session_id") != null ? request.get("session_id").toString().trim() : null;
            String userId = request.get("user_id") != null ? request.get("user_id").toString().trim() : null;
            String areaCode = request.get("area_code") != null ? request.get("area_code").toString().trim() : null;
            String billCycle = request.get("bill_cycle") != null ? request.get("bill_cycle").toString().trim() : null;

            // Validate required parameters
            if (sessionId == null || sessionId.isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Session ID is required");
                responseMap.put("differences", Collections.emptyList());
                responseMap.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.badRequest().body(responseMap);
            }

            if (userId == null || userId.isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "User ID is required");
                responseMap.put("differences", Collections.emptyList());
                responseMap.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.badRequest().body(responseMap);
            }

            // area_code is optional. When it is absent (or blank / "ALL") the response covers
            // every area the user is permitted to read, which is the default after login.
            DifferenceResponse response;
            if (areaCode == null || areaCode.isEmpty() || "ALL".equalsIgnoreCase(areaCode)) {
                List<String> requestedAreas = extractAreaCodes(request.get("area_codes"));
                response = differenceStatisticsService.getDifferencesForAreas(
                        sessionId, userId, requestedAreas, billCycle);
            } else {
                response = differenceStatisticsService.getAreaDifferences(
                        sessionId, userId, areaCode, billCycle);
            }

            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            responseMap.put("area_code", response.getAreaCode());
            responseMap.put("active_bill_cycle", response.getActiveBillCycle());
            responseMap.put("area_codes",
                    response.getAreaCodes() != null ? response.getAreaCodes() : Collections.emptyList());
            responseMap.put("bill_cycles",
                    response.getBillCycles() != null ? response.getBillCycles() : Collections.emptyList());
            responseMap.put("differences",
                    response.getDifferences() != null ? response.getDifferences() : Collections.emptyList());
            responseMap.put("timestamp", response.getTimestamp() != null ? response.getTimestamp().toString()
                    : LocalDateTime.now().toString());

            if (Boolean.TRUE.equals(response.getSuccess())) {
                return ResponseEntity.ok(responseMap);
            } else {
                String message = String.valueOf(response.getMessage());
                if (message.contains("Access denied")) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseMap);
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }

        } catch (Exception e) {
            System.err.println("DifferenceStatisticsController Exception: " + e.getMessage());
            e.printStackTrace();
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve difference statistics: " + e.getMessage());
            responseMap.put("differences", Collections.emptyList());
            responseMap.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Reads an area_codes payload that may arrive as a List or a comma separated string.
     */
    @SuppressWarnings("unchecked")
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
}
