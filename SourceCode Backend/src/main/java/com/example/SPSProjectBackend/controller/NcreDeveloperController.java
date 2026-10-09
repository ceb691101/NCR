package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.NcreDeveloperDTO;
import com.example.SPSProjectBackend.model.NcreDeveloper;
import com.example.SPSProjectBackend.service.NcreDeveloperService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/developers")
public class NcreDeveloperController {

    private static final Logger log = LoggerFactory.getLogger(NcreDeveloperController.class);

    @Autowired
    private NcreDeveloperService ncreDeveloperService;

    @Autowired
    private com.example.SPSProjectBackend.service.DeveloperRegistrationService developerRegistrationService;

    @PostMapping("/active-count")
    public ResponseEntity<?> getActiveDeveloperCount(@RequestBody Map<String, Object> request) {
        String sessionId = (String) request.get("session_id");
        String userId = (String) request.get("user_id");
        String selectedAreaCode = (String) request.get("selected_area_code");
        if (sessionId == null || sessionId.trim().isEmpty() || userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Session ID and user ID are required"));
        }

        try {
            long count = ncreDeveloperService.getActiveDeveloperCount(sessionId, userId, selectedAreaCode);
            return ResponseEntity.ok(Map.of("success", true, "active_count", count));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to retrieve active developer count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to retrieve active developer count"));
        }
    }

    @PostMapping("/register-developer")
    public ResponseEntity<?> createDeveloper(@Valid @RequestBody NcreDeveloperDTO dto, BindingResult br) {
        if (br.hasErrors()) {
            Map<String, String> errors = br.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            fe -> fe.getField(),
                            fe -> fe.getDefaultMessage()
                    ));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        try {
            NcreDeveloper saved = ncreDeveloperService.saveDeveloper(dto);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("acc_nbr", saved.getAccNbr());
            resp.put("message", "Developer saved successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (com.example.SPSProjectBackend.exception.DeveloperAlreadyExistsException dae) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Developer with accountNumber already exists: " + dae.getAccNbr());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
        } catch (IllegalArgumentException iae) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", iae.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Failed to save developer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDeveloper(@RequestParam String field, @RequestParam String value) {
        try {
            com.example.SPSProjectBackend.dto.DeveloperRegistrationRequest payload = developerRegistrationService.getDeveloperDetailsBySearch(field, value);
            if (payload == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Developer not found"));
            }
            return ResponseEntity.ok(payload);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Search failed: " + e.getMessage()));
        }
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(
            @RequestParam(required = false, defaultValue = "folio_no") String field,
            @RequestParam(required = false, defaultValue = "") String query) {
        try {
            java.util.List<Map<String, Object>> suggestions = developerRegistrationService.getDeveloperSuggestions(field, query);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Autocomplete failed: " + e.getMessage()));
        }
    }

    @GetMapping("/{accNbr}")
    public ResponseEntity<?> getDeveloperByAccNbr(@PathVariable String accNbr) {
        try {
            com.example.SPSProjectBackend.dto.DeveloperRegistrationRequest payload =
                    developerRegistrationService.getDeveloperDetailsBySearch("acc_nbr", accNbr);
            if (payload == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Developer not found"));
            }
            return ResponseEntity.ok(payload);
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", iae.getMessage()));
        } catch (Exception e) {
            log.error("Failed to fetch developer by accNbr: " + accNbr, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to fetch developer: " + e.getMessage()));
        }
    }

    @PutMapping("/{accNbr}")
    public ResponseEntity<?> updateDeveloper(@PathVariable String accNbr, @Valid @RequestBody NcreDeveloperDTO dto, BindingResult br) {
        if (br.hasErrors()) {
            Map<String, String> errors = br.getFieldErrors().stream()
                    .collect(Collectors.toMap(fe -> fe.getField(), fe -> fe.getDefaultMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        }

        try {
            NcreDeveloper updated = ncreDeveloperService.updateDeveloper(accNbr, dto);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("acc_nbr", updated.getAccNbr());
            resp.put("message", "Developer updated successfully");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Failed to update developer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }
}

