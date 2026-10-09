package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.AmndTypeDTO;
import com.example.SPSProjectBackend.service.AmndTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/api/amndtypes")
public class AmndTypeController {

    @Autowired
    private AmndTypeService service;

    @GetMapping
    public ResponseEntity<List<AmndTypeDTO>> getAll() {
        return ResponseEntity.ok(service.getAllAmndTypes());
    }

    @GetMapping("/{amdType}")
    public ResponseEntity<?> getByAmdType(@PathVariable String amdType) {
        try {
            Optional<AmndTypeDTO> amndType = service.getAmndTypeByCode(amdType);

            if (amndType.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Amendment type found");
                response.put("amendmentType", amndType.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Amendment type not found");
                error.put("message", "No amendment type found with code: " + amdType);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve amendment type");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}