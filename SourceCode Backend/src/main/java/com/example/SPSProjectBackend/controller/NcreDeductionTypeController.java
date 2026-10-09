package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.model.NcreDeductionType;
import com.example.SPSProjectBackend.service.NcreDeductionTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment-deduction-types")
@RequiredArgsConstructor
public class NcreDeductionTypeController {

    private final NcreDeductionTypeService deductionTypeService;

    /**
     * GET /api/v1/payment-deduction-types
     *
     * Returns all active NCRE payment deduction types (status = '2').
     */
    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getPaymentDeductionTypes() {
        log.info("REST request to get active NCRE payment deduction types");
        try {
            List<NcreDeductionType> types = deductionTypeService.getActiveDeductionTypes();
            if (types == null || types.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            List<Map<String, String>> result = types.stream()
                    .map(t -> Map.of(
                            "dedTypeNm", t.getDedTypeNm().trim()
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to fetch active payment deduction types", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}