package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.BillCycleDTO;
import com.example.SPSProjectBackend.service.BillCycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/billcycles")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class BillCycleController {

    @Autowired
    private BillCycleService billCycleService;

    @GetMapping(value = {"/current-open", "/current", "/current-cycle"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getCurrentOpenBillCycle() {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            Optional<BillCycleDTO.CurrentBillCycleDTO> currentCycle = billCycleService.getCurrentOpenBillCycle();
            if (currentCycle.isEmpty()) {
                Optional<Integer> cycleNum = billCycleService.getCurrentBillCycleNumber();
                if (cycleNum.isPresent()) {
                    BillCycleDTO.CurrentBillCycleDTO fallback = new BillCycleDTO.CurrentBillCycleDTO();
                    fallback.setBillCycle(cycleNum.get());
                    fallback.setIsCurrent(1);
                    fallback.setCycleStatus("Open");
                    responseMap.put("success", true);
                    responseMap.put("current_bill_cycle", fallback);
                    responseMap.put("bill_cycle", cycleNum.get());
                    return ResponseEntity.ok(responseMap);
                }
                responseMap.put("success", false);
                responseMap.put("message", "No current bill cycle found where is_current = 1 in dbadmin.ncre_bill_cycle");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMap);
            }

            responseMap.put("success", true);
            responseMap.put("current_bill_cycle", currentCycle.get());
            responseMap.put("bill_cycle", currentCycle.get().getBillCycle());
            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve the current bill cycle: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Invoice creation summary for a bill cycle (active developers, invoices created,
     * invoices pending and progress). Falls back to the current open bill cycle when
     * no bill cycle is supplied.
     */
    @GetMapping(value = "/invoice-creation-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getInvoiceCreationSummary(
            @RequestParam(required = false) Integer billCycle) {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            BillCycleDTO.InvoiceCreationSummaryDTO summary =
                    billCycleService.getInvoiceCreationSummary(billCycle);

            responseMap.put("success", true);
            responseMap.put("summary", summary);
            return ResponseEntity.ok(responseMap);
        } catch (IllegalStateException e) {
            responseMap.put("success", false);
            responseMap.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMap);
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve the invoice creation summary");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    @GetMapping(value = "/invoice-create-entries", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getInvoiceCreateEntries(
            @RequestParam(required = false) Integer billCycle) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            List<BillCycleDTO.InvoiceCreateEntryDTO> entries =
                    billCycleService.getInvoiceCreateEntries(billCycle);
            responseMap.put("success", true);
            responseMap.put("entries", entries);
            return ResponseEntity.ok(responseMap);
        } catch (IllegalStateException e) {
            responseMap.put("success", false);
            responseMap.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMap);
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve invoice register entries");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    @PostMapping(value = "/reconcile-current-cycle", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> reconcileCurrentCycle(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @RequestParam(required = false) Integer billCycle) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            BillCycleDTO.ActiveDeveloperReconciliationDTO result =
                    billCycleService.reconcileActiveDevelopers(sessionId, billCycle);
            responseMap.put("success", true);
            responseMap.put("reconciliation", result);
            return ResponseEntity.ok(responseMap);
        } catch (ResponseStatusException e) {
            responseMap.put("success", false);
            responseMap.put("message", e.getReason() != null ? e.getReason() : "Unable to reconcile active developers");
            return ResponseEntity.status(e.getStatusCode()).body(responseMap);
        } catch (Exception e) {
            System.err.println("[BillCycleController] reconcile-current-cycle failed: " + e);
            e.printStackTrace();
            responseMap.put("success", false);
            responseMap.put("message", "Unable to check for newly active developers.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * End the current bill cycle: closes it and opens the next one in a single
     * transaction. The acting user is taken from the session, never from the request.
     */
    @PostMapping(value = "/end-cycle", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> endCurrentBillCycle(
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @RequestBody(required = false) BillCycleDTO.EndBillCycleRequestDTO request) {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            BillCycleDTO.ActiveDeveloperReconciliationDTO reconciliation =
                billCycleService.reconcileActiveDevelopers(sessionId, null);
            if (!reconciliation.getAddedFolios().isEmpty()) {
            responseMap.put("success", false);
            responseMap.put("message", "New active developer folios were added to this bill cycle: "
                + reconciliation.getAddedFolios()
                + ". Their invoices are now pending, so this bill cycle cannot be ended yet.");
            responseMap.put("added_folios", reconciliation.getAddedFolios());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(responseMap);
            }

                BillCycleDTO.EndBillCycleResultDTO result = billCycleService.endCurrentBillCycle(
                    sessionId,
                    request == null ? null : request.getRemarks());

            responseMap.put("success", true);
            responseMap.put("message", "Bill cycle ended successfully");
            responseMap.put("result", result);
            return ResponseEntity.ok(responseMap);
        } catch (ResponseStatusException e) {
            responseMap.put("success", false);
            responseMap.put("message", e.getReason() != null ? e.getReason() : "Unable to end the bill cycle");
            return ResponseEntity.status(e.getStatusCode()).body(responseMap);
        } catch (Exception e) {
            // The response stays user friendly, but the real cause must be logged:
            // without this the failure is an unexplained 500.
            System.err.println("[BillCycleController] end-cycle failed: " + e);
            e.printStackTrace();

            responseMap.put("success", false);
            responseMap.put("message", "Failed to end the bill cycle. No changes were made.");
            responseMap.put("error", "END_CYCLE_FAILED");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Active developers that have not yet created an invoice for a bill cycle.
     * Returns friendly developer details only.
     */
    @GetMapping(value = "/pending-developers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getPendingDevelopers(
            @RequestParam(required = false) Integer billCycle) {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            List<BillCycleDTO.PendingDeveloperDTO> developers = billCycleService.getPendingDevelopers(billCycle);

            responseMap.put("success", true);
            responseMap.put("pending_developers", developers);
            responseMap.put("total_pending", developers.size());
            return ResponseEntity.ok(responseMap);
        } catch (IllegalStateException e) {
            responseMap.put("success", false);
            responseMap.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMap);
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve pending developers");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get bill cycles based on user's access level
     * This is the main endpoint that returns bill cycles according to user category
     */
    @PostMapping(value = "/user-billcycles", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getUserBillCycles(@RequestBody Map<String, String> request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            String sessionId = request.get("session_id");
            String userId = request.get("user_id");
            
            // Validate input
            if (sessionId == null || sessionId.trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Session ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }
            
            if (userId == null || userId.trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "User ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            // Get bill cycles for user
            BillCycleDTO.BillCycleResponse response = billCycleService.getBillCyclesForUser(sessionId, userId);
            
            // Convert to response map
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            responseMap.put("user_category", response.getUserCategory());
            responseMap.put("bill_cycles", response.getBillCycles());
            responseMap.put("timestamp", response.getTimestamp() != null ? response.getTimestamp().toString() : LocalDateTime.now().toString());
            
            if (response.getSuccess()) {
                // Add summary information
                int totalAreas = response.getBillCycles().size();
                long areasWithCycles = response.getBillCycles().stream()
                        .filter(BillCycleDTO.AreaBillCycleDTO::getHasBillCycle)
                        .count();
                
                responseMap.put("total_areas", totalAreas);
                responseMap.put("areas_with_cycles", areasWithCycles);
                responseMap.put("areas_without_cycles", totalAreas - areasWithCycles);
                
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMap);
            }

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve bill cycles: " + e.getMessage());
            responseMap.put("error", "INTERNAL_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get bill cycles for a specific area
     */
    @GetMapping(value = "/area/{areaCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getAreaBillCycles(
            @PathVariable String areaCode,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String userId) {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Check access if session info provided
            if (sessionId != null && userId != null) {
                boolean hasAccess = billCycleService.hasAccessToArea(sessionId, userId, areaCode);
                if (!hasAccess) {
                    responseMap.put("success", false);
                    responseMap.put("message", "Access denied to area " + areaCode);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseMap);
                }
            }

            List<BillCycleDTO.AreaBillCycleDTO> billCycles = billCycleService.getAreaBillCycles(areaCode);
            
            responseMap.put("success", true);
            responseMap.put("message", "Bill cycles retrieved successfully for area " + areaCode);
            responseMap.put("area_code", areaCode);
            responseMap.put("bill_cycles", billCycles);
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve bill cycles for area " + areaCode);
            responseMap.put("error", e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get bill cycles for a specific province
     */
    @GetMapping(value = "/province/{provinceCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getProvinceBillCycles(
            @PathVariable String provinceCode,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String userId) {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            List<BillCycleDTO.AreaBillCycleDTO> billCycles = billCycleService.getProvinceBillCycles(provinceCode);
            
            responseMap.put("success", true);
            responseMap.put("message", "Bill cycles retrieved successfully for province " + provinceCode);
            responseMap.put("province_code", provinceCode);
            responseMap.put("bill_cycles", billCycles);
            responseMap.put("total_areas", billCycles.size());
            responseMap.put("areas_with_cycles", billCycles.stream().filter(BillCycleDTO.AreaBillCycleDTO::getHasBillCycle).count());
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve bill cycles for province " + provinceCode);
            responseMap.put("error", e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get bill cycles for a specific region
     */
    @GetMapping(value = "/region/{regionCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getRegionBillCycles(
            @PathVariable String regionCode,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String userId) {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            List<BillCycleDTO.AreaBillCycleDTO> billCycles = billCycleService.getRegionBillCycles(regionCode);
            
            responseMap.put("success", true);
            responseMap.put("message", "Bill cycles retrieved successfully for region " + regionCode);
            responseMap.put("region_code", regionCode);
            responseMap.put("bill_cycles", billCycles);
            responseMap.put("total_areas", billCycles.size());
            responseMap.put("areas_with_cycles", billCycles.stream().filter(BillCycleDTO.AreaBillCycleDTO::getHasBillCycle).count());
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve bill cycles for region " + regionCode);
            responseMap.put("error", e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get all bill cycles (Admin only)
     */
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getAllBillCycles(
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String userId) {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            List<BillCycleDTO.AreaBillCycleDTO> billCycles = billCycleService.getAllAreasBillCycles();
            
            // Group by region for better organization
            Map<String, List<BillCycleDTO.AreaBillCycleDTO>> groupedByRegion = billCycles.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                        bc -> bc.getRegionCode() != null ? bc.getRegionCode() : "UNKNOWN"
                    ));
            
            responseMap.put("success", true);
            responseMap.put("message", "All bill cycles retrieved successfully");
            responseMap.put("bill_cycles", billCycles);
            responseMap.put("bill_cycles_by_region", groupedByRegion);
            responseMap.put("total_areas", billCycles.size());
            responseMap.put("areas_with_cycles", billCycles.stream().filter(BillCycleDTO.AreaBillCycleDTO::getHasBillCycle).count());
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve all bill cycles");
            responseMap.put("error", e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get bill cycle summary for multiple areas
     */
    @PostMapping(value = "/summary", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getBillCycleSummary(@RequestBody Map<String, Object> request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<String> areaCodes = (List<String>) request.get("area_codes");
            
            if (areaCodes == null || areaCodes.isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Area codes are required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            List<BillCycleDTO.BillCycleSummaryDTO> summary = billCycleService.getBillCycleSummary(areaCodes);
            
            responseMap.put("success", true);
            responseMap.put("message", "Bill cycle summary retrieved successfully");
            responseMap.put("summary", summary);
            responseMap.put("total_areas", summary.size());
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve bill cycle summary");
            responseMap.put("error", e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get all bill cycle configurations for an area (detailed view)
     */
    @GetMapping(value = "/area/{areaCode}/configurations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getAreaBillCycleConfigurations(
            @PathVariable String areaCode,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String userId) {
        
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Check access if session info provided
            if (sessionId != null && userId != null) {
                boolean hasAccess = billCycleService.hasAccessToArea(sessionId, userId, areaCode);
                                if (!hasAccess) {
                    responseMap.put("success", false);
                    responseMap.put("message", "Access denied to area " + areaCode);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseMap);
                }
            }

            List<BillCycleDTO.BillCycleConfigDTO> configurations = billCycleService.getAllBillCyclesForArea(areaCode);
            
            responseMap.put("success", true);
            responseMap.put("message", "Bill cycle configurations retrieved successfully");
            responseMap.put("area_code", areaCode);
            responseMap.put("configurations", configurations);
            responseMap.put("total_configurations", configurations.size());
            responseMap.put("active_configurations", configurations.stream().filter(BillCycleDTO.BillCycleConfigDTO::getIsActive).count());
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve bill cycle configurations");
            responseMap.put("error", e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Health check endpoint for bill cycle service
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Try to perform a simple operation
            billCycleService.getAreaBillCycles("00"); // Non-existent area
            
            responseMap.put("status", "healthy");
            responseMap.put("message", "Bill cycle service is operational");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("status", "unhealthy");
            responseMap.put("message", "Bill cycle service has issues: " + e.getMessage());
            responseMap.put("error", "SYSTEM_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(responseMap);
        }
    }
}