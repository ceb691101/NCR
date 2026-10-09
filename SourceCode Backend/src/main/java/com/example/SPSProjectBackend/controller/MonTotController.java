package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.MonTotDTO;
import com.example.SPSProjectBackend.service.MonTotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/mon-tots")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class MonTotController {

    @Autowired
    private MonTotService monTotService;

    @GetMapping("/account/{accNbr}/bill-cycle/{billCycle}")
    public ResponseEntity<?> getMonTotByAccNbrAndBillCycle(@PathVariable String accNbr, @PathVariable String billCycle) {
        try {
            Optional<MonTotDTO> monTot = monTotService.getByAccNbrAndBillCycle(accNbr, billCycle);

            if (monTot.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "MonTot found");
                response.put("monTot", monTot.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "MonTot not found");
                error.put("message", "No MonTot found for account: " + accNbr + " and bill cycle: " + billCycle);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve MonTot");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}