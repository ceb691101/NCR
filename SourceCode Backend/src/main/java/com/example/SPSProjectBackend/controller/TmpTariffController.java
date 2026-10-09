package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.TariffDTO;
import com.example.SPSProjectBackend.service.TmpTariffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/tmp-tariffs")
public class TmpTariffController {

    @Autowired
    private TmpTariffService tmpTariffService;

    // ================================================================
    // GET ALL TMP TARIFFS
    // ================================================================
    @GetMapping
    public ResponseEntity<List<TariffDTO>> getAllTmpTariffs() {
        try {
            List<TariffDTO> tariffs = tmpTariffService.getAllTmpTariffs();
            return new ResponseEntity<>(tariffs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // GET ACTIVE TMP TARIFFS (to_date = NULL)
    // Used by: Dashboard Tariff New Card
    // ================================================================
    @GetMapping("/active")
    public ResponseEntity<List<TariffDTO>> getActiveTmpTariffs() {
        try {
            List<TariffDTO> tariffs = tmpTariffService.getActiveTmpTariffs();
            return new ResponseEntity<>(tariffs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // GET ENDED TMP TARIFFS (to_date != NULL)
    // Used by: Archive Page
    // ================================================================
    @GetMapping("/ended")
    public ResponseEntity<List<TariffDTO>> getEndedTmpTariffs() {
        try {
            List<TariffDTO> tariffs = tmpTariffService.getEndedTmpTariffs();
            return new ResponseEntity<>(tariffs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // GET TMP TARIFF BY ID
    // ================================================================
    @GetMapping("/{id}")
    public ResponseEntity<TariffDTO> getTmpTariffById(@PathVariable Short id) {
        try {
            Optional<TariffDTO> tariffDTO = tmpTariffService.getTmpTariffById(id);
            return tariffDTO.map(tariff -> ResponseEntity.ok().body(tariff))
                           .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // SEARCH TMP TARIFF BY ID (Query Parameter)
    // ================================================================
    @GetMapping("/search")
    public ResponseEntity<?> searchTmpTariffById(@RequestParam Short tariffId) {
        try {
            Optional<TariffDTO> tariffDTO = tmpTariffService.getTmpTariffById(tariffId);

            if (tariffDTO.isPresent()) {
                return ResponseEntity.ok(tariffDTO.get());
            } else {
                return ResponseEntity.status(404)
                        .body("Tmp Tariff with ID " + tariffId + " not found.");
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // SEARCH TMP TARIFF BY TARIFF NUMBER (Query Parameter)
    // ================================================================
    @GetMapping("/search-by-tariff")
    public ResponseEntity<?> searchTmpTariffByTariff(@RequestParam Short tariff) {
        try {
            Optional<TariffDTO> tariffDTO = tmpTariffService.getTmpTariffByTariff(tariff);

            if (tariffDTO.isPresent()) {
                return ResponseEntity.ok(tariffDTO.get());
            } else {
                return ResponseEntity.status(404)
                        .body("Tmp Tariff with number " + tariff + " not found.");
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // SAVE TMP TARIFF (POST)
    // ================================================================
    @PostMapping("/save")
    public ResponseEntity<TariffDTO> saveTmpTariff(@RequestBody TariffDTO tariffDTO) {
        try {
            TariffDTO savedTariff = tmpTariffService.saveTmpTariff(tariffDTO);
            return new ResponseEntity<>(savedTariff, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // CREATE TMP TARIFF (POST Alternative)
    // ================================================================
    @PostMapping
    public ResponseEntity<TariffDTO> createTmpTariff(@RequestBody TariffDTO tariffDTO) {
        try {
            TariffDTO savedTariff = tmpTariffService.saveTmpTariff(tariffDTO);
            return new ResponseEntity<>(savedTariff, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // UPDATE TMP TARIFF (PATCH)
    // ================================================================
    @PatchMapping("/{tariffId}")
    public ResponseEntity<TariffDTO> updateTmpTariff(
            @PathVariable Short tariffId, 
            @RequestBody TariffDTO updatedTariffDTO) {
        try {
            TariffDTO updated = tmpTariffService.updateTmpTariff(tariffId, updatedTariffDTO);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // UPDATE TMP TARIFF RECORD STATUS (PATCH) - FOR ACTIVE/INACTIVE TOGGLE
    // ================================================================
    @PatchMapping("/{tariffId}/status")
    public ResponseEntity<?> updateTmpTariffRecordStatus(
            @PathVariable Short tariffId,
            @RequestParam Character recordStatus) {
        try {
            System.out.println("=== UPDATE RECORD STATUS ENDPOINT CALLED ===");
            System.out.println("Tariff ID: " + tariffId);
            System.out.println("New Record Status: " + recordStatus);

            // Validate recordStatus is either 'A' or 'I'
            if (recordStatus != 'A' && recordStatus != 'I') {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid record status. Must be 'A' (Active) or 'I' (Inactive)");
            }

            tmpTariffService.updateRecordStatus(tariffId, recordStatus);

            System.out.println("✅ Successfully updated tariff " + tariffId + " to status: " + recordStatus);
            return ResponseEntity.ok()
                    .body("Successfully updated tariff " + tariffId + " to status: " + recordStatus);
        } catch (RuntimeException e) {
            System.err.println("❌ Tariff not found: " + tariffId);
            return new ResponseEntity<>("Tariff not found: " + tariffId, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.err.println("❌ Error updating record status: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // UPDATE TMP TARIFF (PUT Alternative)
    // ================================================================
    @PutMapping("/{tariffId}")
    public ResponseEntity<TariffDTO> updateTmpTariffPut(
            @PathVariable Short tariffId,
            @RequestBody TariffDTO updatedTariffDTO) {
        try {
            TariffDTO updated = tmpTariffService.updateTmpTariff(tariffId, updatedTariffDTO);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // DELETE TMP TARIFF
    // ================================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTmpTariff(@PathVariable Short id) {
        try {
            if (tmpTariffService.getTmpTariffById(id).isPresent()) {
                tmpTariffService.deleteTmpTariff(id);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // END ALL TARIFFS - CRITICAL ENDPOINT!
    // ================================================================
    // This endpoint:
    // 1. Ends all active tariffs (sets to_date = today)
    // 2. Creates new empty tariff records with from_date = today
    // 3. Returns success message
    // ================================================================
    @PostMapping("/end-all")
    public ResponseEntity<?> endAllTariffs() {
        try {
            System.out.println("=== END ALL TARIFFS ENDPOINT CALLED ===");

            // Call the service method that does all the work
            TmpTariffService.EndAllTariffsResult result = tmpTariffService.endAllTariffsAndCreateNew();

            System.out.println("=== END ALL TARIFFS COMPLETED SUCCESSFULLY ===");
            System.out.println("Ended: " + result.getEndedCount() + " tariffs");
            System.out.println("Created: " + result.getCreatedCount() + " new tariffs");

            return ResponseEntity.ok().body(new EndAllTariffsResponse(
                true,
                "Successfully ended " + result.getEndedCount() + " tariff(s) and created " + result.getCreatedCount() + " new tariff(s)",
                result.getEndedCount(),
                result.getCreatedCount()
            ));
        } catch (Exception e) {
            System.err.println("=== ERROR IN END ALL TARIFFS ===");
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EndAllTariffsResponse(
                        false,
                        "Failed to end tariffs: " + e.getMessage(),
                        0,
                        0
                    ));
        }
    }

    // ================================================================
    // END ALL TARIFFS AND CREATE NEW - ALTERNATIVE ENDPOINT
    // ================================================================
    // Same functionality as /end-all, just different endpoint name
    // for frontend compatibility
    // ================================================================
    @PostMapping("/end-all-and-create-new")
    public ResponseEntity<?> endAllTariffsAndCreateNew() {
        try {
            System.out.println("=== END ALL TARIFFS AND CREATE NEW ENDPOINT CALLED ===");

            TmpTariffService.EndAllTariffsResult result = tmpTariffService.endAllTariffsAndCreateNew();

            System.out.println("=== SUCCESS ===");
            System.out.println("Ended: " + result.getEndedCount() + " tariffs");
            System.out.println("Created: " + result.getCreatedCount() + " new tariffs");

            return ResponseEntity.ok().body(new EndAllTariffsResponse(
                true,
                "Successfully ended " + result.getEndedCount() + " tariff(s) and created " + result.getCreatedCount() + " new tariff(s)",
                result.getEndedCount(),
                result.getCreatedCount()
            ));
        } catch (Exception e) {
            System.err.println("=== ERROR ===");
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EndAllTariffsResponse(
                        false,
                        "Failed: " + e.getMessage(),
                        0,
                        0
                    ));
        }
    }

    // ================================================================
    // END SELECTED TARIFFS AND CREATE NEW
    // ================================================================
    // Ends only the specified tariff IDs and creates new records for them
    // Respects Active/Inactive status from frontend
    // ================================================================
    @PostMapping("/end-selected-and-create-new")
    public ResponseEntity<?> endSelectedTariffsAndCreateNew(@RequestBody EndTariffsRequest request) {
        try {
            System.out.println("=== END SELECTED TARIFFS AND CREATE NEW ENDPOINT CALLED ===");
            System.out.println("Tariff IDs to end: " + request.getTariffIds());

            // Validate request body
            if (request == null || request.getTariffIds() == null || request.getTariffIds().isEmpty()) {
                System.err.println("=== ERROR: Invalid request body ===");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new EndAllTariffsResponse(
                            false,
                            "Invalid request: tariffIds cannot be null or empty",
                            0,
                            0
                        ));
            }

            TmpTariffService.EndAllTariffsResult result =
                tmpTariffService.endSpecificTariffsAndCreateNew(request.getTariffIds());

            System.out.println("=== SUCCESS ===");
            System.out.println("Ended: " + result.getEndedCount() + " tariffs");
            System.out.println("Created: " + result.getCreatedCount() + " new tariffs");

            return ResponseEntity.ok().body(new EndAllTariffsResponse(
                true,
                "Successfully ended " + result.getEndedCount() + " tariff(s) and created " + result.getCreatedCount() + " new tariff(s)",
                result.getEndedCount(),
                result.getCreatedCount()
            ));
        } catch (IllegalArgumentException e) {
            System.err.println("=== ERROR: Invalid argument ===");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new EndAllTariffsResponse(
                        false,
                        "Invalid request: " + e.getMessage(),
                        0,
                        0
                    ));
        } catch (Exception e) {
            System.err.println("=== ERROR ===");
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EndAllTariffsResponse(
                        false,
                        "Failed: " + e.getMessage(),
                        0,
                        0
                    ));
        }
    }

    // ================================================================
    // TEST ENDPOINT
    // ================================================================
    @GetMapping("/test")
    public String test() {
        try {
            System.out.println("Tmp Tariff API test endpoint");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Tmp Tariff API is working";
    }

    // ================================================================
    // RESPONSE CLASS FOR END ALL TARIFFS
    // ================================================================
    public static class EndAllTariffsResponse {
        private boolean success;
        private String message;
        private int endedCount;
        private int createdCount;

        public EndAllTariffsResponse(boolean success, String message, int endedCount, int createdCount) {
            this.success = success;
            this.message = message;
            this.endedCount = endedCount;
            this.createdCount = createdCount;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getEndedCount() {
            return endedCount;
        }

        public void setEndedCount(int endedCount) {
            this.endedCount = endedCount;
        }

        public int getCreatedCount() {
            return createdCount;
        }

        public void setCreatedCount(int createdCount) {
            this.createdCount = createdCount;
        }
    }

    // ================================================================
    // REQUEST CLASS FOR END SELECTED TARIFFS
    // ================================================================
    public static class EndTariffsRequest {
        private List<Short> tariffIds;

        public List<Short> getTariffIds() {
            return tariffIds;
        }

        public void setTariffIds(List<Short> tariffIds) {
            this.tariffIds = tariffIds;
        }
    }
}