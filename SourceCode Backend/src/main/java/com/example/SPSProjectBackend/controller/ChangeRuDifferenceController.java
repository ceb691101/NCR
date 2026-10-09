package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.ChangeRuRequestDTO;
import com.example.SPSProjectBackend.dto.DeveloperRuDetailsDTO;
import com.example.SPSProjectBackend.service.ChangeRuDifferenceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/change-ru")
@CrossOrigin(
        origins = {"http://localhost:3000", "http://localhost:8097", "http://127.0.0.1:3000", "http://127.0.0.1:8097"},
        allowCredentials = "true",
        allowedHeaders = "*"
)
public class ChangeRuDifferenceController {

    @Autowired
    private ChangeRuDifferenceService changeRuDifferenceService;

    @GetMapping("/search")
    public ResponseEntity<?> searchDevelopers(@RequestParam String type, @RequestParam String query) {
        try {
            List<DeveloperRuDetailsDTO> results = changeRuDifferenceService.searchDevelopers(type, query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Search failed for type='{}', query='{}'", type, query, e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Search failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{folioNo}")
    public ResponseEntity<?> getDeveloperByFolio(@PathVariable Short folioNo) {
        try {
            DeveloperRuDetailsDTO details = changeRuDifferenceService.getDeveloperByFolio(folioNo);
            return ResponseEntity.ok(details);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to retrieve developer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateRuDifference(@Valid @RequestBody ChangeRuRequestDTO request, BindingResult br) {
        if (br.hasErrors()) {
            Map<String, String> errors = br.getFieldErrors().stream()
                    .collect(Collectors.toMap(fe -> fe.getField(), fe -> fe.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        try {
            changeRuDifferenceService.changeRuDifference(request);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "RU Difference updated successfully for Folio " + request.getFolioNo() + ".");
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Failed to update RU Difference: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }
}
