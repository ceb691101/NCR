// FILE: src/main/java/com/example/SPSProjectBackend/controller/InsertNewReadingController.java
package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.InsertNewReadingDTO;
import com.example.SPSProjectBackend.dto.InsertNewReadingDTO.*;
import com.example.SPSProjectBackend.service.InsertNewReadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/insert-new-readings")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class InsertNewReadingController {

    @Autowired
    private InsertNewReadingService insertNewReadingService;

    /**
     * API: Insert new meter readings for a customer
     */
    @PostMapping(value = "/insert", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> insertNewMeterReadings(@RequestBody InsertNewReadingRequest request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            InsertNewReadingResponse response = insertNewReadingService.insertNewMeterReadings(request);
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            responseMap.put("inserted_count", response.getInsertedCount());
            responseMap.put("meter_readings_inserted", response.getMeterReadingsInserted());
            responseMap.put("timestamp", response.getTimestamp());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
            
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to insert new meter readings: " + e.getMessage());
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
            responseMap.put("message", "Insert new readings service is operational");
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.ok(responseMap);
            
        } catch (Exception e) {
            responseMap.put("status", "unhealthy");
            responseMap.put("message", "Insert new readings service has issues: " + e.getMessage());
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(responseMap);
        }
    }
}