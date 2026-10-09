package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.model.NcreType;
import com.example.SPSProjectBackend.service.NcreTypeService;
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
@RequestMapping("/api/v1/ncre-types")
@RequiredArgsConstructor
public class NcreTypeController {

    private final NcreTypeService ncreTypeService;

    /**
     * GET /api/v1/ncre-types
     *
     * Returns a list of active NCRE types (status = 2) with type_id and type_name.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getNcreTypes() {
        log.info("REST request to get active NCRE types");
        try {
            List<NcreType> types = ncreTypeService.getActiveNcreTypes();
            if (types == null || types.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            List<Map<String, String>> result = types.stream()
                    .map(t -> Map.of(
                            "typeId", t.getTypeId().trim(),
                            "typeName", t.getTypeName().trim()
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to fetch active NCRE types", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
