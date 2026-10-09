package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.TmpAmndDTO;
import com.example.SPSProjectBackend.dto.HsbAreaDTO;
import com.example.SPSProjectBackend.service.TmpAmndService;
import com.example.SPSProjectBackend.service.SecInfoAuthService;
import com.example.SPSProjectBackend.service.HsbLocationService;
import com.example.SPSProjectBackend.util.SessionUtils;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/amendments")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class TmpAmndController {
    @Autowired
    private TmpAmndService tmpAmndService;
    // Added for session validation and access checks (copied from BulkCustomerController)
    @Autowired
    private SecInfoAuthService secInfoAuthService;
    @Autowired
    private HsbLocationService locationService;
    @Autowired
    private SessionUtils sessionUtils;
    // Create new amendment
    @PostMapping
    public ResponseEntity<?> createAmendment(@RequestBody TmpAmndDTO amendmentDTO) {
        try {
            // Validate required fields
            if (amendmentDTO.getAccNbr() == null || amendmentDTO.getAccNbr().trim().isEmpty()) {
                throw new RuntimeException("Account number is required");
            }
            if (amendmentDTO.getAmdType() == null || amendmentDTO.getAmdType().trim().isEmpty()) {
                throw new RuntimeException("Amendment type is required");
            }
            TmpAmndDTO createdAmendment = tmpAmndService.createAmendment(amendmentDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Amendment created successfully");
            response.put("amendment", createdAmendment);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create amendment");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to create amendment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    // UPDATED: Get all pending amendments (status=1), with optional areaCd filter and session validation
    @GetMapping
    public ResponseEntity<?> getAllAmendments(
            @RequestParam(required = false) String areaCd,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            if (areaCd != null && !areaCd.isEmpty()) {
                validateSessionAndAccess(session_id, user_id, null, null, areaCd);
            } else {
                validateSessionAndAccess(session_id, user_id, null, null, null);
            }
            List<TmpAmndDTO> amendments = tmpAmndService.getAllAmendments(areaCd);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Amendments retrieved successfully");
            response.put("amendments", amendments);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve amendments");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    // UPDATED: Fetch all rejected amendments (status = 3), with optional areaCd filter and session validation
    @GetMapping("/rejected")
    public ResponseEntity<?> getRejectedAmendments(
            @RequestParam(required = false) String areaCd,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            if (areaCd != null && !areaCd.isEmpty()) {
                validateSessionAndAccess(session_id, user_id, null, null, areaCd);
            } else {
                validateSessionAndAccess(session_id, user_id, null, null, null);
            }
            List<TmpAmndDTO> rejectedAmendments = tmpAmndService.getRejectedAmendments(areaCd);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Rejected amendments retrieved successfully");
            response.put("count", rejectedAmendments.size());
            response.put("amendments", rejectedAmendments);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve rejected amendments");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    // UPDATED: Fetch all posted amendments (status = 2), with optional areaCd filter and session validation
    @GetMapping("/posted")
    public ResponseEntity<?> getPostedAmendments(
            @RequestParam(required = false) String areaCd,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            if (areaCd != null && !areaCd.isEmpty()) {
                validateSessionAndAccess(session_id, user_id, null, null, areaCd);
            } else {
                validateSessionAndAccess(session_id, user_id, null, null, null);
            }
            List<TmpAmndDTO> postedAmendments = tmpAmndService.getPostedAmendments(areaCd);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Posted amendments retrieved successfully");
            response.put("count", postedAmendments.size());
            response.put("amendments", postedAmendments);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve posted amendments");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    // Get amendments by account number (now returns list)
    @GetMapping("/{accNbr}")
    public ResponseEntity<?> getAmendmentsByAccNbr(@PathVariable String accNbr) {
        try {
            List<TmpAmndDTO> amendments = tmpAmndService.getAmendmentsByAccNbr(accNbr);
            if (amendments.isEmpty()) {
                throw new RuntimeException("No amendments found for account number: " + accNbr);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Amendments found");
            response.put("amendments", amendments);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Amendments not found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve amendments");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    // Update amendment (now with full PK in path)
    @PutMapping("/{accNbr}/{amdType}/{effctBlcy}")
    public ResponseEntity<?> updateAmendment(
            @PathVariable String accNbr,
            @PathVariable String amdType,
            @PathVariable Short effctBlcy,
            @RequestBody TmpAmndDTO amendmentDTO) {
        try {
            TmpAmndDTO updatedAmendment = tmpAmndService.updateAmendment(accNbr, amdType, effctBlcy, amendmentDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Amendment updated successfully");
            response.put("amendment", updatedAmendment);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update amendment");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to update amendment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    // Delete amendment (now with full PK in path)
    @DeleteMapping("/{accNbr}/{amdType}/{effctBlcy}")
    public ResponseEntity<?> deleteAmendment(
            @PathVariable String accNbr,
            @PathVariable String amdType,
            @PathVariable Short effctBlcy) {
        try {
            tmpAmndService.deleteAmendment(accNbr, amdType, effctBlcy);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Amendment deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete amendment");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to delete amendment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    // NEW ENDPOINT: Fetch old value for amendment
    @GetMapping("/old-value")
    public ResponseEntity<?> getOldValue(
            @RequestParam String accNbr,
            @RequestParam String amdType,
            @RequestParam(required = false) String billCycle) {
        try {
            String oldValue = tmpAmndService.getOldValue(accNbr, amdType, billCycle);
            Map<String, Object> response = new HashMap<>();
            response.put("oldValue", oldValue);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch old value");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to fetch old value: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    // Post amendment (now with full PK in path)
    @PutMapping("/{accNbr}/{amdType}/{effctBlcy}/post")
    public ResponseEntity<?> postAmendment(
            @PathVariable String accNbr,
            @PathVariable String amdType,
            @PathVariable Short effctBlcy,
            @RequestParam String session_id,
            @RequestParam String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            tmpAmndService.updateStatus(accNbr, amdType, effctBlcy, "2");
            Map<String, String> response = new HashMap<>();
            response.put("message", "Amendment posted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to post amendment");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to post amendment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    // Reject amendment (now with full PK in path)
    @PutMapping("/{accNbr}/{amdType}/{effctBlcy}/reject")
    public ResponseEntity<?> rejectAmendment(
            @PathVariable String accNbr,
            @PathVariable String amdType,
            @PathVariable Short effctBlcy,
            @RequestParam String session_id,
            @RequestParam String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            tmpAmndService.updateStatus(accNbr, amdType, effctBlcy, "3");
            Map<String, String> response = new HashMap<>();
            response.put("message", "Amendment rejected successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reject amendment");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to reject amendment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    // NEW: Final post to history
    @PutMapping("/{accNbr}/{amdType}/{effctBlcy}/final-post")
    public ResponseEntity<?> finalPostAmendment(
            @PathVariable String accNbr,
            @PathVariable String amdType,
            @PathVariable Short effctBlcy,
            @RequestParam String session_id,
            @RequestParam String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            tmpAmndService.finalPost(accNbr, amdType, effctBlcy);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Amendment archived to history successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to archive amendment");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to archive amendment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    // Helper method to validate session and access (copied from BulkCustomerController)
    private void validateSessionAndAccess(String sessionId, String userId, String targetRegionCode, String targetProvinceCode, String targetAreaCode) {
        if (sessionId != null && userId != null) {
            // Validate session
            SecInfoLoginDTO.SessionValidationRequest validationRequest = new SecInfoLoginDTO.SessionValidationRequest();
            validationRequest.setSessionId(sessionId);
            validationRequest.setUserId(userId);
            SecInfoLoginDTO.SessionValidationResponse validationResponse = secInfoAuthService.validateSession(validationRequest);
            if (!validationResponse.getValid()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session");
            }
            // If area-specific, validate access
            if (targetAreaCode != null) {
                Optional<HsbAreaDTO> areaOpt = locationService.getAreaByCode(targetAreaCode);
                if (areaOpt.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Area not found");
                }
                HsbAreaDTO area = areaOpt.get();
                boolean hasAccess = sessionUtils.hasAreaAccess(sessionId, userId, area.getRegion(), area.getProvCode(), targetAreaCode);
                if (!hasAccess) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this area");
                }
            }
        }
    }
}