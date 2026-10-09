package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.service.LiveTariffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/live-tariffs")
public class LiveTariffController {

    @Autowired
    private LiveTariffService liveTariffService;

    // Handle OPTIONS requests explicitly (CORS preflight)
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleAllOptions() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<?> getLiveTariffList() {
        try {
            List<Map<String, Object>> data = liveTariffService.getLiveTariffList();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            System.err.println("[LiveTariffController] Error loading live tariffs:");
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Failed to load live tariffs: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }
}