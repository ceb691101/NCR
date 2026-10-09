// PackChangesController.java
package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.PackChangesDTO;
import com.example.SPSProjectBackend.dto.PackChangesDTO.*;
import com.example.SPSProjectBackend.service.PackChangesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pack-changes")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class PackChangesController {

    @Autowired
    private PackChangesService packChangesService;

    /**
     * Update pack changes for a customer
     */
    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updatePackChanges(@RequestBody PackChangesRequest request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            PackChangesResponse response = packChangesService.updatePackChanges(
                request.getSessionId(), 
                request.getUserId(), 
                request.getAccountNumber(), 
                request.getAreaCode(), 
                request.getBillCycle(),
                request.getPackChanges()
            );
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            
            if (response.getSuccess() && response.getUpdatedPackChanges() != null) {
                responseMap.put("updated_pack_changes", response.getUpdatedPackChanges());
            }
            
            responseMap.put("timestamp", response.getTimestamp());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
            
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to update pack changes: " + e.getMessage());
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * View pack changes for a customer
     */
    @PostMapping(value = "/view", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> viewPackChanges(@RequestBody PackChangesViewRequest request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            PackChangesViewResponse response = packChangesService.viewPackChanges(
                request.getSessionId(), 
                request.getUserId(), 
                request.getAccountNumber(), 
                request.getAreaCode(), 
                request.getBillCycle()
            );
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            
            if (response.getSuccess() && response.getPackChangesInfo() != null) {
                responseMap.put("pack_changes_info", response.getPackChangesInfo());
            }
            
            responseMap.put("timestamp", response.getTimestamp());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
            
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve pack changes information: " + e.getMessage());
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Update pack changes for multiple customers in bulk
     */
    @PutMapping(value = "/bulk-update", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updateBulkPackChanges(@RequestBody BulkPackChangesRequest request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            BulkPackChangesResponse response = packChangesService.updateBulkPackChanges(
                request.getSessionId(), 
                request.getUserId(), 
                request.getAreaCode(), 
                request.getBillCycle(),
                request.getPackChangesList()
            );
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            responseMap.put("total_processed", response.getTotalProcessed());
            responseMap.put("successful_updates", response.getSuccessfulUpdates());
            responseMap.put("failed_updates", response.getFailedUpdates());
            
            if (response.getFailedAccounts() != null && !response.getFailedAccounts().isEmpty()) {
                responseMap.put("failed_accounts", response.getFailedAccounts());
            }
            
            responseMap.put("timestamp", response.getTimestamp());
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
            }
            
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to process bulk pack changes: " + e.getMessage());
            responseMap.put("timestamp", new java.util.Date().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }
}