package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.MeterDetailsDTO;
import com.example.SPSProjectBackend.service.MeterDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/meterdetails")
public class MeterDetailsController {

    @Autowired
    private MeterDetailsService meterDetailsService;

    @GetMapping
    public ResponseEntity<?> getAllMeterDetails() {
        try {
            return ResponseEntity.ok(meterDetailsService.getAllMeterDetails());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching meter details: " + e.getMessage());
        }
    }

    @GetMapping("/by-job")
    public ResponseEntity<?> getMeterDetailsByJobNumber(@RequestParam String jobNumber) {
        try {
            // Changed to List
            List<MeterDetailsDTO> dtos = meterDetailsService.getMeterDetailsByJobNumber(jobNumber);
            if (dtos == null || dtos.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching meter details: " + e.getMessage());
        }
    }

    // Alternative endpoint using path variable
    @GetMapping("/{jobNumber}")
    public ResponseEntity<?> getMeterDetailsByJobNumberPath(@PathVariable String jobNumber) {
        try {
            // Changed to List
            List<MeterDetailsDTO> dtos = meterDetailsService.getMeterDetailsByJobNumber(jobNumber);
            if (dtos == null || dtos.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching meter details: " + e.getMessage());
        }
    }
}