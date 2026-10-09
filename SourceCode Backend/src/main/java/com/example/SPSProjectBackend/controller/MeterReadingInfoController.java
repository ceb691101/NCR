package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.MeterReadingInfoDTO;
import com.example.SPSProjectBackend.dto.MeterReadingInfoDTO.*;
import com.example.SPSProjectBackend.service.MeterReadingInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/meter-reading-info")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class MeterReadingInfoController {

    @Autowired
    private MeterReadingInfoService meterReadingInfoService;

    /**
     * Get meter reading information for a single customer
     */
    @PostMapping(value = "/customer", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getMeterReadingInfo(@RequestBody MeterReadingRequest request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            MeterReadingInfoResponse response = meterReadingInfoService.getMeterReadingInfo(
                request.getSessionId(), 
                request.getUserId(), 
                request.getAccountNumber(), 
                request.getAreaCode(), 
                request.getBillCycle()
            );
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            responseMap.put("meter_reading_info", response.getMeterReadingInfo());
            responseMap.put("timestamp", response.getTimestamp());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
            
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve meter reading information: " + e.getMessage());
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get meter reading information for multiple customers
     */
    @PostMapping(value = "/bulk", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getBulkMeterReadingInfo(@RequestBody BulkMeterReadingRequest request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            BulkMeterReadingResponse response = meterReadingInfoService.getBulkMeterReadingInfo(
                request.getSessionId(), 
                request.getUserId(), 
                request.getAreaCode(), 
                request.getAccountNumbers(), 
                request.getBillCycle()
            );
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            responseMap.put("area_code", response.getAreaCode());
            responseMap.put("active_bill_cycle", response.getActiveBillCycle());
            responseMap.put("total_customers", response.getTotalCustomers());
            responseMap.put("customers_with_readings", response.getCustomersWithReadings());
            responseMap.put("customers_without_readings", response.getCustomersWithoutReadings());
            responseMap.put("meter_readings", response.getMeterReadings());
            responseMap.put("timestamp", response.getTimestamp());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
            
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve bulk meter reading information: " + e.getMessage());
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get meter reading information for all customers in an area
     */
    @GetMapping(value = "/area/{areaCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getMeterReadingInfoForArea(
            @PathVariable String areaCode,
            @RequestParam String session_id,
            @RequestParam String user_id,
            @RequestParam(required = false) String bill_cycle) {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            BulkMeterReadingResponse response = meterReadingInfoService.getBulkMeterReadingInfo(
                session_id, 
                user_id, 
                areaCode, 
                null, // null accountNumbers means get all customers in area
                bill_cycle
            );
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            responseMap.put("area_code", response.getAreaCode());
            responseMap.put("active_bill_cycle", response.getActiveBillCycle());
            responseMap.put("total_customers", response.getTotalCustomers());
            responseMap.put("customers_with_readings", response.getCustomersWithReadings());
            responseMap.put("customers_without_readings", response.getCustomersWithoutReadings());
            responseMap.put("meter_readings", response.getMeterReadings());
            responseMap.put("timestamp", response.getTimestamp());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
            
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve meter reading information for area: " + e.getMessage());
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
            responseMap.put("message", "Meter reading info service is operational");
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.ok(responseMap);
            
        } catch (Exception e) {
            responseMap.put("status", "unhealthy");
            responseMap.put("message", "Meter reading info service has issues: " + e.getMessage());
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(responseMap);
        }
    }


    /**
     * Edit meter readings for a customer
    */
    @PutMapping(value = "/customer/edit", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> editMeterReadings(@RequestBody MeterReadingEditRequest request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            MeterReadingEditResponse response = meterReadingInfoService.editMeterReadings(
                request.getSessionId(), 
                request.getUserId(), 
                request.getAccountNumber(), 
                request.getAreaCode(), 
                request.getBillCycle(),
                request.getReadingDate(),
                request.getMeterReadings()
            );
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            
            if (response.getSuccess() && response.getUpdatedMeterReadingInfo() != null) {
                responseMap.put("updated_meter_reading_info", response.getUpdatedMeterReadingInfo());
            }
            
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
}