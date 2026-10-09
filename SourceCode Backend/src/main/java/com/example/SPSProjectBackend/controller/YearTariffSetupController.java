package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.service.YearTariffSetupService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tariff-setup")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
public class YearTariffSetupController {
    private final YearTariffSetupService service;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getRows(@RequestHeader("X-Session-Id") String sessionId) {
        return ResponseEntity.ok(service.getRows(sessionId));
    }

    @PatchMapping("/{folioNo}")
    public ResponseEntity<Void> update(@PathVariable Short folioNo, @RequestBody UpdateRequest request,
                                       @RequestHeader("X-Session-Id") String sessionId) {
        service.update(folioNo, request.getNextTariff(), request.getTariffChanged(), sessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{folioNo}/submit")
    public ResponseEntity<Void> submit(@PathVariable Short folioNo, @RequestHeader("X-Session-Id") String sessionId) {
        service.submit(folioNo, sessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{folioNo}/decision")
    public ResponseEntity<Void> decide(@PathVariable Short folioNo, @RequestParam boolean approved,
                                       @RequestHeader("X-Session-Id") String sessionId) {
        service.decide(folioNo, approved, sessionId);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class UpdateRequest {
        private BigDecimal nextTariff;
        private LocalDate tariffChanged;
    }
}
