package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.YrMnthDTO;
import com.example.SPSProjectBackend.service.YrMnthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/api/v1/yr-mnth")
public class YrMnthController {

    @Autowired
    private YrMnthService service;

    @GetMapping
    public ResponseEntity<List<YrMnthDTO>> getAll() {
        return ResponseEntity.ok(service.getAllYrMnths());
    }

    @GetMapping("/{billCycle}")
    public ResponseEntity<?> getByBillCycle(@PathVariable Integer billCycle) {
        try {
            Optional<YrMnthDTO> yrMnth = service.getYrMnthByCycle(billCycle);

            if (yrMnth.isPresent()) {
                return ResponseEntity.ok(yrMnth.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}