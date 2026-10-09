package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.model.NcreGridSubstation;
import com.example.SPSProjectBackend.service.NcreGridSubstationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/grid-substations")
@RequiredArgsConstructor
public class NcreGridSubstationController {

    private final NcreGridSubstationService gridSubstationService;

    /**
     * GET /api/v1/grid-substations
     *
     * Returns all active NCRE grid substations (status = 2).
     */
    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getGridSubstations() {
        log.info("REST request to get active NCRE grid substations");
        try {
            return buildResponse(gridSubstationService.getActiveGridSubstations(null));
        } catch (Exception e) {
            log.error("Failed to fetch active grid substations", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/v1/grid-substations/region/{licenseCode}
     *
     * Returns active NCRE grid substations whose license_code matches the given region license code.
     */
    @GetMapping("/region/{licenseCode}")
    public ResponseEntity<List<Map<String, String>>> getGridSubstationsByRegion(@PathVariable String licenseCode) {
        log.info("REST request to get active NCRE grid substations for license code {}", licenseCode);
        try {
            return buildResponse(gridSubstationService.getActiveGridSubstations(licenseCode));
        } catch (Exception e) {
            log.error("Failed to fetch active grid substations for license code {}", licenseCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<List<Map<String, String>>> buildResponse(List<NcreGridSubstation> substations) {
        if (substations == null || substations.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<Map<String, String>> result = substations.stream()
                .map(s -> Map.of(
                        "gssId", s.getGssId() != null ? s.getGssId().toBigInteger().toString() : "",
                        "licenseCode", s.getLicenseCode() != null ? s.getLicenseCode().trim() : "",
                        "gssCode", s.getGssCode().trim(),
                        "gssName", s.getGssName().trim()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}