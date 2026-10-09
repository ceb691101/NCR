// FILE: src/main/java/com/example/SPSProjectBackend/controller/MeterReadingUpdateController.java
package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.MeterReadingUpdateDTO;
import com.example.SPSProjectBackend.service.MeterReadingUpdateService;
import com.example.SPSProjectBackend.service.MeterReadingUpdateService.UpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/meter-reading-update")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class MeterReadingUpdateController {

    @Autowired
    private MeterReadingUpdateService meterReadingUpdateService;

    /**
     * Update meter readings and charges
     */
    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updateMeterReadings(@RequestBody MeterReadingUpdateDTO updateDTO) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            UpdateResponse response = meterReadingUpdateService.updateMeterReadings(updateDTO);
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            responseMap.put("timestamp", response.getTimestamp());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
            
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to update meter readings: " + e.getMessage());
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
            responseMap.put("message", "Meter reading update service is operational");
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.ok(responseMap);
            
        } catch (Exception e) {
            responseMap.put("status", "unhealthy");
            responseMap.put("message", "Meter reading update service has issues: " + e.getMessage());
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(responseMap);
        }
    }
}