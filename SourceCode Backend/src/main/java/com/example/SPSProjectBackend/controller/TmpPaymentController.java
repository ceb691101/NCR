package com.example.SPSProjectBackend.controller;



import com.example.SPSProjectBackend.dto.TmpPaymentDTO;
import com.example.SPSProjectBackend.service.TmpPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tmp-payments")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class TmpPaymentController {
    
    @Autowired
    private TmpPaymentService tmpPaymentService;
    
    /**
     * GET /api/v1/tmp-payments/options
     * Retrieve dropdown options for form fields
     */
    @GetMapping("/options")
    public ResponseEntity<?> getDropdownOptions() {
        try {
            Map<String, List<String>> options = tmpPaymentService.getDropdownOptions();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Dropdown options retrieved successfully");
            response.put("data", options);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve dropdown options: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/v1/tmp-payments
     * Retrieve all temporary payments with pagination
     */
    @GetMapping
    public ResponseEntity<?> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "creditDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            // Map frontend field names to entity field names
            String entitySortField = mapSortField(sortBy);
            Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                        Sort.by(entitySortField).ascending() : 
                        Sort.by(entitySortField).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<TmpPaymentDTO> paymentPage = tmpPaymentService.getAllPayments(pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payments retrieved successfully");
            response.put("payments", paymentPage.getContent());
            response.put("currentPage", paymentPage.getNumber());
            response.put("totalItems", paymentPage.getTotalElements());
            response.put("totalPages", paymentPage.getTotalPages());
            response.put("pageSize", paymentPage.getSize());
            response.put("hasNext", paymentPage.hasNext());
            response.put("hasPrevious", paymentPage.hasPrevious());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * POST /api/v1/tmp-payments
     * Create a new temporary payment
     */
    @PostMapping
    public ResponseEntity<?> createPayment(@RequestBody TmpPaymentDTO paymentDTO) {
        try {
            TmpPaymentDTO createdPayment = tmpPaymentService.createPayment(paymentDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment created successfully");
            response.put("payment", createdPayment);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("errorType", "VALIDATION_ERROR");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to create payment: " + e.getMessage());
            error.put("errorType", "SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/v1/tmp-payments/{agentCode}/{centCode}/{accNbr}/{stubNo}
     * Retrieve a specific payment by composite key
     */
    @GetMapping("/{agentCode}/{centCode}/{accNbr}/{stubNo}")
    public ResponseEntity<?> getPaymentById(@PathVariable String agentCode, 
                                           @PathVariable String centCode,
                                           @PathVariable String accNbr,
                                           @PathVariable Short stubNo) {
        try {
            TmpPaymentDTO payment = tmpPaymentService.getPaymentById(agentCode, centCode, accNbr, stubNo);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("payment", payment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    /**
     * GET /api/v1/tmp-payments/area/{areaCode}
     * Retrieve payments by area code with pagination
     */
    @GetMapping("/area/{areaCode}")
    public ResponseEntity<?> getPaymentsByArea(
            @PathVariable String areaCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "creditDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            String entitySortField = mapSortField(sortBy);
            Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                        Sort.by(entitySortField).ascending() : 
                        Sort.by(entitySortField).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<TmpPaymentDTO> paymentPage = tmpPaymentService.getPaymentsByAreaCode(areaCode, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payments retrieved by area code");
            response.put("payments", paymentPage.getContent());
            response.put("currentPage", paymentPage.getNumber());
            response.put("totalItems", paymentPage.getTotalElements());
            response.put("totalPages", paymentPage.getTotalPages());
            response.put("pageSize", paymentPage.getSize());
            response.put("hasNext", paymentPage.hasNext());
            response.put("hasPrevious", paymentPage.hasPrevious());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/v1/tmp-payments/account/{accountNumber}
     * Retrieve payments by account number with pagination
     */
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<?> getPaymentsByAccount(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "creditDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            String entitySortField = mapSortField(sortBy);
            Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                        Sort.by(entitySortField).ascending() : 
                        Sort.by(entitySortField).descending();
            
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<TmpPaymentDTO> paymentPage = tmpPaymentService.getPaymentsByAccountNumber(accountNumber, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payments retrieved by account number");
            response.put("payments", paymentPage.getContent());
            response.put("currentPage", paymentPage.getNumber());
            response.put("totalItems", paymentPage.getTotalElements());
            response.put("totalPages", paymentPage.getTotalPages());
            response.put("pageSize", paymentPage.getSize());
            response.put("hasNext", paymentPage.hasNext());
            response.put("hasPrevious", paymentPage.hasPrevious());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET /api/v1/tmp-payments/agent/{agentCode}
     * Retrieve payments by agent code
     */
    @GetMapping("/agent/{agentCode}")
    public ResponseEntity<?> getPaymentsByAgent(@PathVariable String agentCode) {
        try {
            List<TmpPaymentDTO> payments = tmpPaymentService.getPaymentsByAgentCode(agentCode);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("payments", payments);
            response.put("count", payments.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve payments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * PUT /api/v1/tmp-payments/{agentCode}/{centCode}/{accNbr}/{stubNo}
     * Update an existing payment
     */
    @PutMapping("/{agentCode}/{centCode}/{accNbr}/{stubNo}")
    public ResponseEntity<?> updatePayment(@PathVariable String agentCode,
                                          @PathVariable String centCode,
                                          @PathVariable String accNbr,
                                          @PathVariable Short stubNo,
                                          @RequestBody TmpPaymentDTO paymentDTO) {
        try {
            TmpPaymentDTO updatedPayment = tmpPaymentService.updatePayment(agentCode, centCode, accNbr, stubNo, paymentDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment updated successfully");
            response.put("payment", updatedPayment);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("errorType", "VALIDATION_ERROR");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to update payment: " + e.getMessage());
            error.put("errorType", "SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    /**
     * DELETE /api/v1/tmp-payments/{agentCode}/{centCode}/{accNbr}/{stubNo}
     * Delete a payment
     */
    @DeleteMapping("/{agentCode}/{centCode}/{accNbr}/{stubNo}")
    public ResponseEntity<?> deletePayment(@PathVariable String agentCode,
                                          @PathVariable String centCode,
                                          @PathVariable String accNbr,
                                          @PathVariable Short stubNo) {
        try {
            tmpPaymentService.deletePayment(agentCode, centCode, accNbr, stubNo);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to delete payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    /**
     * Map frontend field names to entity field names
     */
    private String mapSortField(String frontendField) {
        switch (frontendField) {
            case "creditDate":
                return "creditDate";
            case "actualPayDate":
                return "actualPayDate";
            case "agentCode":
                return "agentCode";
            case "centCode":
                return "centCode";
            case "accNbr":
                return "accNbr";
            case "paidAmount":
            case "paid_amt":
                return "paidAmount";
            default:
                return "creditDate"; // default sort
        }
    }
}