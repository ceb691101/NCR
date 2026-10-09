package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.NetmeterDTO;
import com.example.SPSProjectBackend.service.NetmeterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/netmeters")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class NetmeterController {

    @Autowired
    private NetmeterService netmeterService;

    @GetMapping("/account/{accNbr}")
    public ResponseEntity<?> getNetmeterByAccNbr(@PathVariable String accNbr) {
        try {
            Optional<NetmeterDTO> netmeter = netmeterService.getNetmeterByAccNbr(accNbr);

            if (netmeter.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Netmeter found");
                response.put("netmeter", netmeter.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Netmeter not found");
                error.put("message", "No netmeter found for account: " + accNbr);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve netmeter");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}