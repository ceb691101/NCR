package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.TariffDTO;
import com.example.SPSProjectBackend.service.TariffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/tariffs")
public class TariffController {

    @Autowired
    private TariffService tariffService;

    // ================================================================
    // GET ALL ACTIVE TARIFFS (to_date = NULL)
    // ================================================================
    @GetMapping
    public ResponseEntity<List<TariffDTO>> getAllTariffs() {
        try {
            List<TariffDTO> tariffs = tariffService.getAllTariffs();
            return new ResponseEntity<>(tariffs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // GET ONLY ACTIVE TARIFFS (to_date = NULL)
    // Used by Live Tariffs page to show only current tariffs
    // ================================================================
    @GetMapping("/active")
    public ResponseEntity<List<TariffDTO>> getActiveTariffs() {
        try {
            List<TariffDTO> tariffs = tariffService.getAllActiveTariffs();
            return new ResponseEntity<>(tariffs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // GET ALL ENDED TARIFFS (to_date != NULL)
    // Used by Tariff History page to show ended live tariffs
    // ================================================================
    @GetMapping("/ended")
    public ResponseEntity<List<TariffDTO>> getAllEndedTariffs() {
        try {
            List<TariffDTO> tariffs = tariffService.getAllEndedTariffs();
            return new ResponseEntity<>(tariffs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ================================================================
    // SAVE NEW TARIFF
    // ================================================================
    @PostMapping("/save")
    public ResponseEntity<TariffDTO> createTariff(@RequestBody TariffDTO tariffDTO) {
        try {
            TariffDTO savedTariff = tariffService.saveTariff(tariffDTO);
            return new ResponseEntity<>(savedTariff, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // ================================================================
    // UPDATE TARIFF
    // ================================================================
    @PatchMapping("/{tariffId}")
    public ResponseEntity<TariffDTO> updateTariff(
            @PathVariable Short tariffId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestBody TariffDTO tariffDTO) {
        try {
            TariffDTO updated = tariffService.updateTariff(tariffId, fromDate, tariffDTO);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // ================================================================
    // END ALL TARIFFS AND CREATE NEW ONES
    // ================================================================
    @PostMapping("/end-all-and-create-new")
    public ResponseEntity<?> endAllTariffsAndCreateNew() {
        try {
            TariffService.EndAllTariffsResult result = tariffService.endAllTariffsAndCreateNew();

            return ResponseEntity.ok(new EndAllResponse(
                true,
                "Successfully ended " + result.getEndedCount() + " tariff(s) and created " + result.getCreatedCount() + " new tariff(s)",
                result.getEndedCount(),
                result.getCreatedCount()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EndAllResponse(false, "Error: " + e.getMessage(), 0, 0));
        }
    }

    // ================================================================
    // END SELECTED TARIFFS AND CREATE NEW ONES (FOR ACTIVE/INACTIVE STATUS)
    // ================================================================
    @PostMapping("/end-selected-and-create-new")
    public ResponseEntity<?> endSelectedTariffsAndCreateNew(@RequestBody EndTariffsRequest request) {
        try {
            System.out.println("=== END SELECTED TARIFFS ENDPOINT CALLED ===");
            System.out.println("Tariff IDs to end: " + request.getTariffIds());

            // Validate request body
            if (request.getTariffIds() == null || request.getTariffIds().isEmpty()) {
                System.out.println("=== VALIDATION ERROR: Empty tariff IDs list ===");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new EndAllResponse(false, "Tariff IDs list cannot be null or empty", 0, 0));
            }

            // Call service method
            TariffService.EndAllTariffsResult result = tariffService.endSpecificTariffsAndCreateNew(request.getTariffIds());

            System.out.println("=== SUCCESS ===");
            System.out.println("Ended: " + result.getEndedCount());
            System.out.println("Created: " + result.getCreatedCount());

            return ResponseEntity.ok(new EndAllResponse(
                true,
                "Successfully ended " + result.getEndedCount() + " tariff(s) and created " + result.getCreatedCount() + " new tariff(s)",
                result.getEndedCount(),
                result.getCreatedCount()
            ));
        } catch (IllegalArgumentException e) {
            System.err.println("=== VALIDATION ERROR ===");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new EndAllResponse(false, "Validation error: " + e.getMessage(), 0, 0));
        } catch (Exception e) {
            System.err.println("=== INTERNAL ERROR ===");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EndAllResponse(false, "Error: " + e.getMessage(), 0, 0));
        }
    }

    // Request class for ending selected tariffs
    public static class EndTariffsRequest {
        private List<Short> tariffIds;

        public List<Short> getTariffIds() {
            return tariffIds;
        }

        public void setTariffIds(List<Short> tariffIds) {
            this.tariffIds = tariffIds;
        }
    }

    // ================================================================
// REACTIVATE ALL TARIFFS
// ================================================================
@PostMapping("/reactivate-all")
public ResponseEntity<ReactivateAllTariffsResponse> reactivateAllTariffs() {
    try {
        System.out.println("=== REACTIVATE ALL TARIFFS ENDPOINT CALLED ===");
        
        TariffService.ReactivateAllTariffsResult result = tariffService.reactivateAllTariffs();
        
        System.out.println("=== SUCCESS ===");
        System.out.println("Reactivated: " + result.getReactivatedCount());
        System.out.println("Deleted: " + result.getDeletedCount());
        
        return ResponseEntity.ok(new ReactivateAllTariffsResponse(
            true,
            "All tariffs reactivated successfully",
            result.getReactivatedCount(),
            result.getDeletedCount()
        ));
    } catch (Exception e) {
        System.err.println("=== ERROR ===");
        e.printStackTrace();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ReactivateAllTariffsResponse(
                    false,
                    "Failed: " + e.getMessage(),
                    0,
                    0
                ));
    }
}

// Response class
public static class ReactivateAllTariffsResponse {
    private boolean success;
    private String message;
    private int reactivatedCount;
    private int deletedCount;

    public ReactivateAllTariffsResponse(boolean success, String message, int reactivatedCount, int deletedCount) {
        this.success = success;
        this.message = message;
        this.reactivatedCount = reactivatedCount;
        this.deletedCount = deletedCount;
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public int getReactivatedCount() { return reactivatedCount; }
    public int getDeletedCount() { return deletedCount; }

    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setReactivatedCount(int reactivatedCount) { this.reactivatedCount = reactivatedCount; }
    public void setDeletedCount(int deletedCount) { this.deletedCount = deletedCount; }
}

// ================================================================
// TRANSFER TO TMP_TARIFF (TARIFF SETUP)
// ================================================================
@PostMapping("/transfer-to-tmp")
public ResponseEntity<TransferToTmpResponse> transferToTmpTariff() {
    try {
        System.out.println("=== TRANSFER TO TMP_TARIFF ENDPOINT CALLED ===");

        int transferredCount = tariffService.transferToTmpTariff();

        System.out.println("=== SUCCESS ===");
        System.out.println("Transferred: " + transferredCount);

        return ResponseEntity.ok(new TransferToTmpResponse(
            true,
            "Successfully transferred " + transferredCount + " tariff(s) to Tariff Setup",
            transferredCount
        ));
    } catch (Exception e) {
        System.err.println("=== ERROR ===");
        e.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new TransferToTmpResponse(
                    false,
                    "Transfer failed: " + e.getMessage(),
                    0
                ));
    }
}

// Response class for transfer
public static class TransferToTmpResponse {
    private boolean success;
    private String message;
    private int transferredCount;

    public TransferToTmpResponse(boolean success, String message, int transferredCount) {
        this.success = success;
        this.message = message;
        this.transferredCount = transferredCount;
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public int getTransferredCount() { return transferredCount; }

    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setTransferredCount(int transferredCount) { this.transferredCount = transferredCount; }
}




    // Response class
    public static class EndAllResponse {
        private boolean success;
        private String message;
        private int endedCount;
        private int createdCount;

        public EndAllResponse(boolean success, String message, int endedCount, int createdCount) {
            this.success = success;
            this.message = message;
            this.endedCount = endedCount;
            this.createdCount = createdCount;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getEndedCount() { return endedCount; }
        public int getCreatedCount() { return createdCount; }
    }

    @GetMapping("/test")
    public String test() {
        return "Tariff API is working";
    }
}