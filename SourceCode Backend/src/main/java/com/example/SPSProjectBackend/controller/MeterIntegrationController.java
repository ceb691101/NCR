package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.BrandDTO;
import com.example.SPSProjectBackend.dto.MeterAmendmentDTO;
import com.example.SPSProjectBackend.dto.MeterAmendmentRequestDTO;
import com.example.SPSProjectBackend.dto.MtrReasonDTO;
import com.example.SPSProjectBackend.service.MeterIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class MeterIntegrationController {

    @Autowired
    private MeterIntegrationService meterIntegrationService;

    @GetMapping("/meters/customer/{accNbr}")
    public ResponseEntity<?> getMetersByCustomer(@PathVariable String accNbr) {
        try {
            Map<String, Object> meterData = meterIntegrationService.getMetersByAccountNumber(accNbr);
            return ResponseEntity.ok(meterData);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Customer not found");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch meter details");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/meters/customer/{accNbr}/previous-readings")
    public ResponseEntity<?> getPreviousBilledReadings(
            @PathVariable String accNbr,
            @RequestParam Integer mtrSeq,
            @RequestParam(required = false) String mtrNbr,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date effctDate) {
        try {
            Map<String, Integer> readings = meterIntegrationService.getLatestBilledReadings(accNbr, mtrSeq, mtrNbr, effctDate);
            Map<String, BigDecimal> rates = meterIntegrationService.getLatestBilledRates(accNbr, mtrSeq, mtrNbr, effctDate);
            Map<String, Object> response = new HashMap<>();
            response.put("acc_nbr", accNbr);
            response.put("mtr_seq", mtrSeq);
            response.put("mtr_nbr", mtrNbr);
            response.put("effct_date", effctDate);
            response.put("readings", readings);
            response.put("rates", rates);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Validation failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch previous readings");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/meter-reasons")
    public ResponseEntity<?> getMeterReasons() {
        try {
            List<MtrReasonDTO> reasons = meterIntegrationService.getActiveMeterReasons();
            Map<String, Object> response = new HashMap<>();
            response.put("total", reasons.size());
            response.put("reasons", reasons);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch meter reasons");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/meter-brands")
    public ResponseEntity<?> getMeterBrands() {
        try {
            List<BrandDTO> brands = meterIntegrationService.getActiveBrands();
            Map<String, Object> response = new HashMap<>();
            response.put("total", brands.size());
            response.put("brands", brands);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch meter brands");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/meter-amendments")
    public ResponseEntity<?> createMeterAmendment(@RequestBody MeterAmendmentRequestDTO request) {
        try {
            meterIntegrationService.createMeterAmendment(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Meter amendment saved successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Validation failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to save meter amendment");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/meter-amendments/pending")
    public ResponseEntity<?> getPendingMeterAmendments(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {
        try {
            List<MeterAmendmentDTO> amendments = meterIntegrationService.getPendingMeterAmendments(limit, offset);
            long totalCount = meterIntegrationService.getPendingMeterAmendmentsCount();
            
            Map<String, Object> response = new HashMap<>();
            response.put("total", totalCount);
            response.put("count", amendments.size());
            response.put("limit", limit);
            response.put("offset", offset);
            response.put("amendments", amendments);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch pending amendments");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

        @PutMapping("/meter-amendments/approve")
    public ResponseEntity<?> approveMeterAmendment(
            @RequestParam String acc_nbr,
            @RequestParam String amd_type,
            @RequestParam String mtr_nbr,
            @RequestParam String effct_blcy,
            @RequestParam Long entered_dtime,
            @RequestParam(required = false) String user_id) {
        try {
            String userId = user_id != null ? user_id : "SYSTEM";
            meterIntegrationService.approveMeterAmendment(
                acc_nbr,
                amd_type,
                mtr_nbr,
                effct_blcy,
                new Timestamp(entered_dtime),
                userId
            );
            Map<String, String> response = new HashMap<>();
            response.put("message", "Meter amendment approved successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Validation failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to approve meter amendment");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

        @PutMapping("/meter-amendments/reject")
    public ResponseEntity<?> rejectMeterAmendment(
            @RequestParam String acc_nbr,
            @RequestParam String amd_type,
            @RequestParam String mtr_nbr,
            @RequestParam String effct_blcy,
            @RequestParam Long entered_dtime,
            @RequestParam(required = false) String user_id) {
        try {
            String userId = user_id != null ? user_id : "SYSTEM";
            meterIntegrationService.rejectMeterAmendment(
                acc_nbr,
                amd_type,
                mtr_nbr,
                effct_blcy,
                new Timestamp(entered_dtime),
                userId
            );
            Map<String, String> response = new HashMap<>();
            response.put("message", "Meter amendment rejected successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Validation failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reject meter amendment");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
