package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.DeveloperRegistrationRequest;
import com.example.SPSProjectBackend.model.NcreDeveloper;
import com.example.SPSProjectBackend.service.DeveloperRegistrationService;
import com.example.SPSProjectBackend.exception.DeveloperAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/developers")
@RequiredArgsConstructor
public class DeveloperRegistrationController {

    private final DeveloperRegistrationService registrationService;

    @PostMapping("/register")
    public ResponseEntity<?> registerDeveloper(
            @RequestPart("data") DeveloperRegistrationRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        
        log.info("REST request to register developer: {}", request.getDeveloperName());
        
        try {
            // Pass files to service to upload and map them
            NcreDeveloper savedDeveloper = registrationService.registerDeveloper(request, files);
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedDeveloper);
                    
        } catch (DeveloperAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Registration failed: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Registration failed: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid developer registration request: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("REST request failed for developer registration: {}", request.getDeveloperName(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateDeveloper(
            @RequestPart("data") DeveloperRegistrationRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        
        log.info("REST request to update developer: {}", request.getDeveloperName());
        
        try {
            NcreDeveloper updatedDeveloper = registrationService.updateDeveloper(request, files);
            
            return ResponseEntity
                    .ok(updatedDeveloper);
                    
        } catch (IllegalArgumentException e) {
            log.warn("Invalid developer update request: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Update failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("REST request failed for developer update: {}", request.getDeveloperName(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Update failed: " + e.getMessage());
        }
    }
}
