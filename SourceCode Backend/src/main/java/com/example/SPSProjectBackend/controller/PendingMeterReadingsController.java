package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.PendingMeterReadingsDTO;
import com.example.SPSProjectBackend.dto.PendingMeterReadingsDTO.*;
import com.example.SPSProjectBackend.service.PendingMeterReadingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pending-meter-readings")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class PendingMeterReadingsController {

    @Autowired
    private PendingMeterReadingsService pendingMeterReadingsService;

    /**
     * API: Get all pending meter readings for an area with active bill cycle
     */
    @PostMapping(value = "/area/active", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getPendingMeterReadingsForAreaActive(
            @RequestBody PendingMeterReadingRequest request) {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            PendingMeterReadingResponse response = pendingMeterReadingsService.getPendingMeterReadingsForArea(
                request.getSessionId(), 
                request.getUserId(), 
                request.getAreaCode(), 
                null // No bill cycle provided = use active
            );
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            responseMap.put("pending_readings", response.getPendingReadings());
            responseMap.put("timestamp", response.getTimestamp());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
            
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve pending meter readings: " + e.getMessage());
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * API: Get single pending meter reading for a customer with active bill cycle
     */
    @PostMapping(value = "/customer/active", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getPendingMeterReadingForCustomerActive(
            @RequestBody PendingMeterReadingRequest request) {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            SinglePendingReadingResponse response = pendingMeterReadingsService.getPendingMeterReadingForCustomer(
                request.getSessionId(), 
                request.getUserId(), 
                request.getAccountNumber(), 
                request.getAreaCode(), 
                null, // No bill cycle provided = use active
                request.getFolioNo()
            );
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            
            if (response.getSuccess() && response.getPendingReading() != null) {
                responseMap.put("pending_reading", response.getPendingReading());
            }
            
            responseMap.put("timestamp", response.getTimestamp());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
            
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve pending meter reading: " + e.getMessage());
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * API: Get all pending meter readings for an area with given bill cycle
     */
    @PostMapping(value = "/area", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getPendingMeterReadingsForAreaWithCycle(
            @RequestBody PendingMeterReadingRequest request) {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            PendingMeterReadingResponse response = pendingMeterReadingsService.getPendingMeterReadingsForArea(
                request.getSessionId(), 
                request.getUserId(), 
                request.getAreaCode(), 
                request.getBillCycle()
            );
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            responseMap.put("pending_readings", response.getPendingReadings());
            responseMap.put("timestamp", response.getTimestamp());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
            
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve pending meter readings: " + e.getMessage());
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * API: Get single pending meter reading for a customer with given bill cycle
     */
    @PostMapping(value = "/customer", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getPendingMeterReadingForCustomerWithCycle(
            @RequestBody PendingMeterReadingRequest request) {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            SinglePendingReadingResponse response = pendingMeterReadingsService.getPendingMeterReadingForCustomer(
                request.getSessionId(), 
                request.getUserId(), 
                request.getAccountNumber(), 
                request.getAreaCode(), 
                request.getBillCycle(),
                request.getFolioNo()
            );
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            
            if (response.getSuccess() && response.getPendingReading() != null) {
                responseMap.put("pending_reading", response.getPendingReading());
            }
            
            responseMap.put("timestamp", response.getTimestamp());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
            
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve pending meter reading: " + e.getMessage());
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
            responseMap.put("message", "Pending meter readings service is operational");
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.ok(responseMap);
            
        } catch (Exception e) {
            responseMap.put("status", "unhealthy");
            responseMap.put("message", "Pending meter readings service has issues: " + e.getMessage());
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(responseMap);
        }
    }
}