package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.InvoiceChecklistDTO;
import com.example.SPSProjectBackend.service.InvoiceChecklistService;
import com.example.SPSProjectBackend.service.NcreAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/invoice-checklist")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8097", "http://127.0.0.1:3000", "http://127.0.0.1:8097"}, allowCredentials = "true")
public class InvoiceChecklistController {

    @Autowired
    private InvoiceChecklistService invoiceChecklistService;

    @Autowired
    private NcreAuthorizationService authorizationService;

    /**
     * Resolve authenticated user ID from session context or query parameter. Returns empty if missing/invalid.
     */
    private Optional<String> resolveUserIdFromSession(String headerSessionId, String querySessionId, String queryUserId) {
        String sessionId = headerSessionId != null && !headerSessionId.trim().isEmpty() ? headerSessionId : querySessionId;
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            Optional<String> uid = authorizationService.getUserIdFromSession(sessionId);
            if (uid.isPresent() && !uid.get().trim().isEmpty()) {
                return uid;
            }
        }
        if (queryUserId != null && !queryUserId.trim().isEmpty()) {
            return Optional.of(queryUserId.trim());
        }
        return Optional.empty();
    }

    /**
     * Get active current bill cycle from ncre_bill_cycle where is_current = 1
     */
    @GetMapping(value = "/current-bill-cycle", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCurrentBillCycle(
            @RequestHeader(value = "X-Session-Id", required = false) String headerSessionId,
            @RequestParam(value = "session_id", required = false) String querySessionId,
            @RequestParam(value = "user_id", required = false) String queryUserId) {

        Optional<String> userIdOpt = resolveUserIdFromSession(headerSessionId, querySessionId, queryUserId);
        if (!userIdOpt.isPresent()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Unauthorized. A valid active session is required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        String userId = userIdOpt.get();
        if (!authorizationService.hasAccess(userId, "IM", "INVCHK", "NCR")) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Access denied. User does not have active permission IM/INVCHK.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        }

        Optional<Integer> currentCycleOpt = invoiceChecklistService.getCurrentBillCycle();
        if (!currentCycleOpt.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "No current bill month is available.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("billCycle", currentCycleOpt.get());
        return ResponseEntity.ok(response);
    }

    /**
     * Get invoices for Invoice Checklist by bill cycle
     */
    @GetMapping(value = "/invoices", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getChecklistInvoices(
            @RequestParam("billCycle") Integer billCycle,
            @RequestHeader(value = "X-Session-Id", required = false) String headerSessionId,
            @RequestParam(value = "session_id", required = false) String querySessionId,
            @RequestParam(value = "user_id", required = false) String queryUserId) {

        Optional<String> userIdOpt = resolveUserIdFromSession(headerSessionId, querySessionId, queryUserId);
        if (!userIdOpt.isPresent()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Unauthorized. A valid active session is required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        String userId = userIdOpt.get();
        if (!authorizationService.hasAccess(userId, "IM", "INVCHK", "NCR")) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Access denied. User does not have active permission IM/INVCHK.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        }

        try {
            InvoiceChecklistDTO.InvoiceChecklistResponseDTO response =
                    invoiceChecklistService.getChecklistInvoices(billCycle, userId);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Failed to retrieve invoice checklist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    /**
     * Get checked invoices (is_create = 1) for Invoice Checklist by bill cycle
     */
    @GetMapping(value = "/checked-invoices", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCheckedInvoices(
            @RequestParam("billCycle") Integer billCycle,
            @RequestHeader(value = "X-Session-Id", required = false) String headerSessionId,
            @RequestParam(value = "session_id", required = false) String querySessionId,
            @RequestParam(value = "user_id", required = false) String queryUserId) {

        Optional<String> userIdOpt = resolveUserIdFromSession(headerSessionId, querySessionId, queryUserId);
        if (!userIdOpt.isPresent()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Unauthorized. A valid active session is required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        String userId = userIdOpt.get();
        if (!authorizationService.hasAccess(userId, "IM", "INVCHK", "NCR")) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Access denied. User does not have active permission IM/INVCHK.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        }

        try {
            InvoiceChecklistDTO.InvoiceChecklistResponseDTO response =
                    invoiceChecklistService.getCheckedInvoices(billCycle, userId);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Failed to retrieve checked invoice checklist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    /**
     * Save selected invoices to ncre_invoice_create
     */
    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addChecklistInvoices(
            @RequestBody InvoiceChecklistDTO.AddChecklistRequestDTO request,
            @RequestHeader(value = "X-Session-Id", required = false) String headerSessionId,
            @RequestParam(value = "session_id", required = false) String querySessionId,
            @RequestParam(value = "user_id", required = false) String queryUserId) {

        String requestUserId = request != null && request.getSelectedInvoices() != null ? queryUserId : queryUserId;
        Optional<String> userIdOpt = resolveUserIdFromSession(headerSessionId, querySessionId, requestUserId);
        if (!userIdOpt.isPresent()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Unauthorized. A valid active session is required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        String userId = userIdOpt.get();
        if (!authorizationService.hasAccess(userId, "IM", "INVCHK", "NCR")) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Access denied. User does not have active permission IM/INVCHK.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        }

        try {
            Map<String, Object> response = invoiceChecklistService.addChecklistInvoices(
                    request.getBillCycle(), request.getSelectedInvoices(), userId);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Failed to add invoices to checklist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    /**
     * Get Invoice Checklist Validation metrics
     */
    @GetMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> validateChecklist(
            @RequestParam("billCycle") Integer billCycle,
            @RequestHeader(value = "X-Session-Id", required = false) String headerSessionId,
            @RequestParam(value = "session_id", required = false) String querySessionId,
            @RequestParam(value = "user_id", required = false) String queryUserId) {

        Optional<String> userIdOpt = resolveUserIdFromSession(headerSessionId, querySessionId, queryUserId);
        if (!userIdOpt.isPresent()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Unauthorized. A valid active session is required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        String userId = userIdOpt.get();
        if (!authorizationService.hasAccess(userId, "IM", "INVCHK", "NCR")) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Access denied. User does not have active permission IM/INVCHK.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        }

        try {
            InvoiceChecklistDTO.ValidationResultDTO validation =
                    invoiceChecklistService.validateChecklist(billCycle, userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", validation);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Failed to validate invoice checklist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }
}
