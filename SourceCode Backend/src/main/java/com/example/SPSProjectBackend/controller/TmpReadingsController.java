package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.TmpReadingsDTO;
import com.example.SPSProjectBackend.dto.NcreReadingsDTO;
import com.example.SPSProjectBackend.service.TmpReadingsService;
import com.example.SPSProjectBackend.service.SecInfoAuthService;
import com.example.SPSProjectBackend.service.HsbLocationService;
import com.example.SPSProjectBackend.service.UserAreaPermissionService;
import com.example.SPSProjectBackend.util.SessionUtils;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tmp-readings")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class TmpReadingsController {

    @Autowired
    private TmpReadingsService tmpReadingsService;

    @Autowired
    private SecInfoAuthService secInfoAuthService;

    @Autowired
    private HsbLocationService locationService;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private UserAreaPermissionService areaPermissionService;

    // Get all readings with active bill cycle filter (default behavior)
    @GetMapping
    public ResponseEntity<?> getAllReadings(
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id,
            @RequestParam(required = false, defaultValue = "false") boolean include_all_cycles) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            
            List<TmpReadingsDTO> readings;
            if (include_all_cycles) {
                readings = tmpReadingsService.getAllReadingsWithoutFilter();
            } else {
                readings = tmpReadingsService.getAllReadings();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", include_all_cycles ? 
                "All readings retrieved (all bill cycles)" : 
                "Readings retrieved (active bill cycles only)");
            response.put("total_readings", readings.size());
            response.put("filtered_by_active_bill_cycle", !include_all_cycles);
            response.put("readings", readings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve readings");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get readings by account number with active bill cycle filter (default behavior)
    @GetMapping("/account/{accNbr}")
    public ResponseEntity<?> getReadingsByAccNbr(
            @PathVariable String accNbr,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id,
            @RequestParam(required = false, defaultValue = "false") boolean include_all_cycles) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            
            List<TmpReadingsDTO> readings;
            if (include_all_cycles) {
                readings = tmpReadingsService.getReadingsByAccNbrWithoutFilter(accNbr);
            } else {
                readings = tmpReadingsService.getReadingsByAccNbr(accNbr);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("account_number", accNbr);
            response.put("total_readings", readings.size());
            response.put("filtered_by_active_bill_cycle", !include_all_cycles);
            response.put("message", include_all_cycles ? 
                "Account readings retrieved (all bill cycles)" : 
                "Account readings retrieved (active bill cycles only)");
            response.put("readings", readings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve readings for account");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get readings by account number and date
    @GetMapping("/account/{accNbr}/date/{rdngDate}")
    public ResponseEntity<?> getReadingsByAccNbrAndDate(
            @PathVariable String accNbr,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date rdngDate,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id,
            @RequestParam(required = false, defaultValue = "false") boolean include_all_cycles) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            
            List<TmpReadingsDTO> readings;
            if (include_all_cycles) {
                readings = tmpReadingsService.getReadingsByAccNbrAndDateWithoutFilter(accNbr, rdngDate);
            } else {
                readings = tmpReadingsService.getReadingsByAccNbrAndDate(accNbr, rdngDate);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("account_number", accNbr);
            response.put("reading_date", rdngDate);
            response.put("total_readings", readings.size());
            response.put("filtered_by_active_bill_cycle", !include_all_cycles);
            response.put("readings", readings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve readings");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get latest readings for account
    @GetMapping("/account/{accNbr}/latest")
    public ResponseEntity<?> getLatestReadingsByAccNbr(
            @PathVariable String accNbr,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id,
            @RequestParam(required = false, defaultValue = "false") boolean include_all_cycles) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            
            List<TmpReadingsDTO> readings;
            if (include_all_cycles) {
                readings = tmpReadingsService.getLatestReadingsByAccNbrWithoutFilter(accNbr);
            } else {
                readings = tmpReadingsService.getLatestReadingsByAccNbr(accNbr);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("account_number", accNbr);
            response.put("total_readings", readings.size());
            response.put("filtered_by_active_bill_cycle", !include_all_cycles);
            response.put("readings", readings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve latest readings");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get readings by area code with active bill cycle filter - FIXED WITH DEBUG LOGGING
    @GetMapping("/area/{areaCd}")
    public ResponseEntity<?> getReadingsByAreaCd(
            @PathVariable String areaCd,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id,
            @RequestParam(required = false, defaultValue = "false") boolean include_all_cycles,
            @RequestParam(required = false, defaultValue = "true") boolean exclude_finalized) {
        try {
            System.out.println("DEBUG: API called for area " + areaCd + ", include_all_cycles=" + include_all_cycles + ", exclude_finalized=" + exclude_finalized);
            
            validateSessionAndAccess(session_id, user_id, null, null, areaCd);
            
            boolean isEE = sessionUtils.isEEUser(session_id, user_id);
            String eeFilter = isEE ? user_id : null;
            
            List<NcreReadingsDTO> readings;
            Map<String, Object> response = new HashMap<>();
            
            if (include_all_cycles) {
                readings = tmpReadingsService.getNcreReadingsByAreaCdWithoutFilter(areaCd, eeFilter, exclude_finalized);
                response.put("message", "Area readings retrieved (all bill cycles)");
            } else {
                readings = tmpReadingsService.getNcreReadingsByAreaCd(areaCd, eeFilter, exclude_finalized);
                response.put("message", "Area readings retrieved (active bill cycle only)");
                
                // Add active bill cycle information
                Optional<Integer> activeBillCycle = tmpReadingsService.getActiveBillCycleForArea(areaCd);
                response.put("active_bill_cycle", activeBillCycle.orElse(null));
                
                System.out.println("DEBUG: Active bill cycle for area " + areaCd + " = " + activeBillCycle.orElse(null));
                
                // Add warning if no active bill cycle
                if (!activeBillCycle.isPresent()) {
                    response.put("warning", "No active bill cycle found for area " + areaCd);
                }
            }
            
            response.put("area_code", areaCd);
            response.put("total_readings", readings.size());
            response.put("filtered_by_active_bill_cycle", !include_all_cycles);
            response.put("exclude_finalized", exclude_finalized);
            response.put("readings", readings);
            
            System.out.println("DEBUG: Returning " + readings.size() + " readings for area " + areaCd);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("ERROR in getReadingsByAreaCd for area " + areaCd + ": " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve readings by area code");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Readings across every area in scope for this user.
     *
     * The area list comes from the sec_info region/province/area codes, narrowed to the
     * single area chosen in the header bar when there is one. Each area keeps its own
     * active bill cycle.
     */
    @GetMapping("/areas")
    public ResponseEntity<?> getReadingsByAreaCodes(
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id,
            @RequestParam(required = false, defaultValue = "false") boolean include_all_cycles,
            @RequestParam(required = false, defaultValue = "true") boolean exclude_finalized) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);

            boolean isEE = sessionUtils.isEEUser(session_id, user_id);
            String eeFilter = isEE ? user_id : null;

            String selectedArea = sessionUtils.getSelectedAreaCode(session_id);
            List<String> areaCodes = areaPermissionService
                    .resolveEffectiveAreaCodes(session_id, user_id, selectedArea);

            List<NcreReadingsDTO> readings = include_all_cycles
                    ? tmpReadingsService.getNcreReadingsByAreaCodesWithoutFilter(areaCodes, eeFilter, exclude_finalized)
                    : tmpReadingsService.getNcreReadingsByAreaCodes(areaCodes, eeFilter, exclude_finalized);

            Map<String, Object> response = new HashMap<>();
            response.put("message", include_all_cycles
                    ? "Readings retrieved for all areas (all bill cycles)"
                    : "Readings retrieved for all areas (active bill cycle only)");
            // "ALL_PERMITTED" when more than one area is covered, otherwise the single code.
            response.put("area_code", areaCodes.size() == 1 ? areaCodes.get(0) : "ALL_PERMITTED");
            response.put("area_codes", areaCodes);
            response.put("total_readings", readings.size());
            response.put("filtered_by_active_bill_cycle", !include_all_cycles);
            response.put("exclude_finalized", exclude_finalized);
            response.put("readings", readings);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("ERROR in getReadingsByAreaCodes: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve readings");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get readings by area code and specific bill cycle (for historical data)
    @GetMapping("/area/{areaCd}/bill-cycle/{billCycle}")
    public ResponseEntity<?> getReadingsByAreaCdAndBillCycle(
            @PathVariable String areaCd,
            @PathVariable String billCycle,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, areaCd);
            
            boolean isEE = sessionUtils.isEEUser(session_id, user_id);
            String eeFilter = isEE ? user_id : null;
            
            List<NcreReadingsDTO> readings = tmpReadingsService.getNcreReadingsByAreaCdAndBillCycle(areaCd, billCycle, eeFilter);
            
            Map<String, Object> response = new HashMap<>();
            response.put("area_code", areaCd);
            response.put("bill_cycle", billCycle);
            response.put("total_readings", readings.size());
            response.put("message", "Historical readings retrieved for bill cycle " + billCycle);
            response.put("readings", readings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve readings by area code and bill cycle");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get readings by meter number
    @GetMapping("/meter/{mtrNbr}")
    public ResponseEntity<?> getReadingsByMtrNbr(
            @PathVariable String mtrNbr,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id,
            @RequestParam(required = false, defaultValue = "false") boolean include_all_cycles) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            
            List<TmpReadingsDTO> readings;
            if (include_all_cycles) {
                readings = tmpReadingsService.getReadingsByMtrNbrWithoutFilter(mtrNbr);
            } else {
                readings = tmpReadingsService.getReadingsByMtrNbr(mtrNbr);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("meter_number", mtrNbr);
            response.put("total_readings", readings.size());
            response.put("filtered_by_active_bill_cycle", !include_all_cycles);
            response.put("readings", readings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve readings by meter number");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get readings by date range
    @GetMapping("/date-range")
    public ResponseEntity<?> getReadingsByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id,
            @RequestParam(required = false, defaultValue = "false") boolean include_all_cycles) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            
            List<TmpReadingsDTO> readings;
            if (include_all_cycles) {
                readings = tmpReadingsService.getReadingsByDateRangeWithoutFilter(startDate, endDate);
            } else {
                readings = tmpReadingsService.getReadingsByDateRange(startDate, endDate);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("start_date", startDate);
            response.put("end_date", endDate);
            response.put("total_readings", readings.size());
            response.put("filtered_by_active_bill_cycle", !include_all_cycles);
            response.put("readings", readings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve readings by date range");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get specific reading
    @GetMapping("/specific")
    public ResponseEntity<?> getSpecificReading(
            @RequestParam String accNbr,
            @RequestParam String areaCd,
            @RequestParam String addedBlcy,
            @RequestParam Integer mtrSeq,
            @RequestParam String mtrType,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date rdngDate,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, areaCd);
            
            Optional<TmpReadingsDTO> reading = tmpReadingsService.getSpecificReading(
                accNbr, areaCd, addedBlcy, mtrSeq, mtrType, rdngDate);
            
            if (reading.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Reading found");
                response.put("reading", reading.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Reading not found");
                error.put("message", "No reading found for the specified criteria");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve specific reading");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Create new reading
    @PostMapping
    public ResponseEntity<?> createReading(@RequestBody TmpReadingsDTO readingDTO) {
        try {
            TmpReadingsDTO createdReading = tmpReadingsService.createReading(readingDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Reading created successfully");
            response.put("reading", createdReading);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create reading");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to create reading: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Update existing reading
    @PutMapping
    public ResponseEntity<?> updateReading(@RequestBody TmpReadingsDTO readingDTO) {
        try {
            TmpReadingsDTO updatedReading = tmpReadingsService.updateReading(readingDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Reading updated successfully");
            response.put("reading", updatedReading);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Reading not found");
                error.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update reading");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to update reading: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get readings with errors
    @GetMapping("/errors")
    public ResponseEntity<?> getReadingsWithErrors(
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id,
            @RequestParam(required = false, defaultValue = "false") boolean include_all_cycles) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            
            List<TmpReadingsDTO> readings;
            if (include_all_cycles) {
                readings = tmpReadingsService.getReadingsWithErrorsWithoutFilter();
            } else {
                readings = tmpReadingsService.getReadingsWithErrors();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("total_errors", readings.size());
            response.put("filtered_by_active_bill_cycle", !include_all_cycles);
            response.put("readings", readings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve readings with errors");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get distinct account numbers
    @GetMapping("/accounts/distinct")
    public ResponseEntity<?> getDistinctAccNbrs() {
        try {
            List<String> accounts = tmpReadingsService.getDistinctAccNbrs();
            Map<String, Object> response = new HashMap<>();
            response.put("total_accounts", accounts.size());
            response.put("accounts", accounts);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve distinct account numbers");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get distinct meter types
    @GetMapping("/meter-types/distinct")
    public ResponseEntity<?> getDistinctMtrTypes() {
        try {
            List<String> meterTypes = tmpReadingsService.getDistinctMtrTypes();
            Map<String, Object> response = new HashMap<>();
            response.put("total_types", meterTypes.size());
            response.put("meter_types", meterTypes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve distinct meter types");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Get active bill cycle for area
    @GetMapping("/area/{areaCd}/active-bill-cycle")
    public ResponseEntity<?> getActiveBillCycleForArea(@PathVariable String areaCd) {
        try {
            Optional<Integer> activeBillCycle = tmpReadingsService.getActiveBillCycleForArea(areaCd);
            
            Map<String, Object> response = new HashMap<>();
            response.put("area_code", areaCd);
            response.put("active_bill_cycle", activeBillCycle.orElse(null));
            response.put("has_active_bill_cycle", activeBillCycle.isPresent());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve active bill cycle");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Helper method to validate session and access
    private void validateSessionAndAccess(String sessionId, String userId, String targetRegionCode, String targetProvinceCode, String targetAreaCode) {
        if (sessionId != null && userId != null) {
            // Validate session
            SecInfoLoginDTO.SessionValidationRequest validationRequest = new SecInfoLoginDTO.SessionValidationRequest();
            validationRequest.setSessionId(sessionId);
            validationRequest.setUserId(userId);
            SecInfoLoginDTO.SessionValidationResponse validationResponse = secInfoAuthService.validateSession(validationRequest);
            if (!validationResponse.getValid()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session");
            }

            // If area-specific, validate access
            if (targetAreaCode != null) {
                Optional<com.example.SPSProjectBackend.dto.HsbAreaDTO> areaOpt = locationService.getAreaByCode(targetAreaCode);
                if (areaOpt.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Area not found");
                }
                com.example.SPSProjectBackend.dto.HsbAreaDTO area = areaOpt.get();
                boolean hasAccess = sessionUtils.hasAreaAccess(sessionId, userId, area.getRegion(), area.getProvCode(), targetAreaCode);
                if (!hasAccess) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this area");
                }
            }
        }
    }
}