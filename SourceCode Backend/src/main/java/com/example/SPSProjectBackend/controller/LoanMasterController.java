package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.LoanMasterDTO;
import com.example.SPSProjectBackend.service.LoanMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/loans")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class LoanMasterController {

    @Autowired
    private LoanMasterService loanMasterService;

    @GetMapping("/account/{accNbr}")
    public ResponseEntity<?> getLoanByAccNbr(@PathVariable String accNbr) {
        try {
            Optional<LoanMasterDTO> loan = loanMasterService.getLoanByAccNbr(accNbr);

            if (loan.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Loan found");
                response.put("loan", loan.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Loan not found");
                error.put("message", "No loan found for account: " + accNbr);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve loan");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}