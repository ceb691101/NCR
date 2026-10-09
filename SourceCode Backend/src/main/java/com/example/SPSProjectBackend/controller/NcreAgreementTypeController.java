package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.model.NcreAgreementType;
import com.example.SPSProjectBackend.service.NcreAgreementTypeService;
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
@RequestMapping("/api/v1/ncre-agreement-types")
@RequiredArgsConstructor
public class NcreAgreementTypeController {

    private final NcreAgreementTypeService agreementTypeService;

    /**
     * GET /api/v1/ncre-agreement-types
     *
     * Returns a list of active NCRE agreement types (status = 2) with agg_type_id and agg_type_nm.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getAgreementTypes() {
        log.info("REST request to get active NCRE agreement types");
        try {
            List<NcreAgreementType> types = agreementTypeService.getActiveAgreementTypes();
            if (types == null || types.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            List<Map<String, String>> result = types.stream()
                    .map(t -> Map.of(
                            "aggTypeId", t.getAggTypeId().trim(),
                            "aggTypeName", t.getAggTypeName().trim()
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to fetch active NCRE agreement types", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}