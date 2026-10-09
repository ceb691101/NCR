package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.InstInfoDTO;
import com.example.SPSProjectBackend.service.InstInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/inst-info")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class InstInfoController {

    @Autowired
    private InstInfoService instInfoService;

    @GetMapping("/inst-id/{instId}")
    public ResponseEntity<?> getInstInfoByInstId(@PathVariable String instId) {
        try {
            Optional<InstInfoDTO> instInfo = instInfoService.getByInstId(instId);

            if (instInfo.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "InstInfo found");
                response.put("instInfo", instInfo.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "InstInfo not found");
                error.put("message", "No InstInfo found for instId: " + instId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve InstInfo");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}