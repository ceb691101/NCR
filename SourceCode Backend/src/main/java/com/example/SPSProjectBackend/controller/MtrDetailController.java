package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.MtrDetailDTO;
import com.example.SPSProjectBackend.service.MtrDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/mtr-details")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class MtrDetailController {

    @Autowired
    private MtrDetailService mtrDetailService;

    @GetMapping("/inst-id/{instId}")
    public ResponseEntity<?> getMtrDetailByInstId(@PathVariable String instId) {
        try {
            Optional<MtrDetailDTO> mtrDetail = mtrDetailService.getByInstId(instId);

            if (mtrDetail.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "MtrDetail found");
                response.put("mtrDetail", mtrDetail.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "MtrDetail not found");
                error.put("message", "No MtrDetail found for instId: " + instId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve MtrDetail");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}