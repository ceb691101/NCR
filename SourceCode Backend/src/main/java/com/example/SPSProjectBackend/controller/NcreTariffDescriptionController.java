package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.service.NcreTariffDescriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/ncre-tariff-descriptions")
@RequiredArgsConstructor
public class NcreTariffDescriptionController {

    private final NcreTariffDescriptionService tariffDescriptionService;

    /**
     * GET /api/v1/ncre-tariff-descriptions
     *
     * Returns a list of active NCRE tariff descriptions (status = 2) for the Developer Registration dropdown.
     */
    @GetMapping
    public ResponseEntity<List<String>> getTariffDescriptions() {
        log.info("REST request to get active NCRE tariff descriptions");
        try {
            List<String> descriptions = tariffDescriptionService.getTariffDescriptions();
            return ResponseEntity.ok(descriptions != null ? descriptions : Collections.emptyList());
        } catch (Exception e) {
            log.error("Failed to fetch active tariff descriptions", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
