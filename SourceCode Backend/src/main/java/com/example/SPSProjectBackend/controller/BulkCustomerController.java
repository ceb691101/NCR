package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.BulkCustomerDTO;
import com.example.SPSProjectBackend.service.BulkCustomerService;
import com.example.SPSProjectBackend.service.UserAreaPermissionService;
import com.example.SPSProjectBackend.service.SecInfoAuthService;
import com.example.SPSProjectBackend.service.HsbLocationService;
import com.example.SPSProjectBackend.util.SessionUtils;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.dto.HsbAreaDTO;
import com.example.SPSProjectBackend.dto.MapDataDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.example.SPSProjectBackend.dto.BulkCustomerLocationUpdateRequest;
import com.example.SPSProjectBackend.dto.BulkCustomerStatusUpdateRequest;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/bulk-customers")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true", allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE,
        RequestMethod.OPTIONS }, maxAge = 3600)
public class BulkCustomerController {

    @Autowired
    private BulkCustomerService bulkCustomerService;

    @Autowired
    private SecInfoAuthService secInfoAuthService;

    @Autowired
    private HsbLocationService locationService;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private UserAreaPermissionService areaPermissionService;

    // Get customers for the areas the user is permitted to read
    @GetMapping
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            boolean isEE = sessionUtils.isEEUser(session_id, user_id);
            String eeFilter = isEE ? user_id : null;
            // Scoped to the permitted areas, narrowed to the header-bar selection when present.
            String selectedArea = sessionUtils.getSelectedAreaCode(session_id);
            List<String> areaCodes = areaPermissionService
                    .resolveEffectiveAreaCodes(session_id, user_id, selectedArea);
            List<BulkCustomerDTO> customers = bulkCustomerService.getCustomersForAreas(areaCodes, eeFilter);
            Map<String, Object> response = new HashMap<>();
            response.put("total_customers", customers.size());
            response.put("customers", customers);
            response.put("area_codes", areaCodes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customers");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get customer by account number - Add session validation if needed
    @GetMapping("/account/{accNbr}")
    public ResponseEntity<?> getCustomerByAccNbr(@PathVariable String accNbr,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            Optional<BulkCustomerDTO> customer = bulkCustomerService.getCustomerByAccNbr(accNbr);

            if (customer.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Customer found");
                response.put("customer", customer.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Customer not found");
                error.put("message", "No customer found with account number: " + accNbr);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customer");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get customers by area code - Add session validation and access check
    @GetMapping("/area/{areaCd}")
    public ResponseEntity<?> getCustomersByAreaCd(@PathVariable String areaCd,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, areaCd);
            boolean isEE = sessionUtils.isEEUser(session_id, user_id);
            String eeFilter = isEE ? user_id : null;
            List<BulkCustomerDTO> customers = bulkCustomerService.getCustomersByAreaCd(areaCd, eeFilter);
            Map<String, Object> response = new HashMap<>();
            response.put("area_code", areaCd);
            response.put("total_customers", customers.size());
            response.put("customers", customers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customers by area code");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Update customer location (latitude & longitude)
    @PostMapping("/updateLocation")
    public ResponseEntity<?> updateCustomerLocation(@Valid @RequestBody BulkCustomerLocationUpdateRequest request) {
        try {
            bulkCustomerService.updateCustomerLocation(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Customer location updated successfully");
            response.put("acc_nbr", request.getAcc_nbr());
            response.put("latitude", request.getLatitude());
            response.put("longitude", request.getLongitude());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid input");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update customer location");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Update developer status (1 = Inactive, 2 = Active)
    @PostMapping("/update-status")
    public ResponseEntity<?> updateDeveloperStatus(@Valid @RequestBody BulkCustomerStatusUpdateRequest request) {
        try {
            bulkCustomerService.updateDeveloperStatus(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Developer status updated successfully");
            response.put("acc_nbr", request.getAcc_nbr());
            response.put("status", request.getStatus());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid input");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update developer status");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get customers by zone
    @GetMapping("/zone/{zone}")
    public ResponseEntity<?> getCustomersByZone(@PathVariable String zone,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            List<BulkCustomerDTO> customers = bulkCustomerService.getCustomersByZone(zone);
            Map<String, Object> response = new HashMap<>();
            response.put("zone", zone);
            response.put("total_customers", customers.size());
            response.put("customers", customers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customers by zone");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get customers by zone and area
    @GetMapping("/zone/{zone}/area/{areaCd}")
    public ResponseEntity<?> getCustomersByZoneAndArea(@PathVariable String zone, @PathVariable String areaCd,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, areaCd);
            List<BulkCustomerDTO> customers = bulkCustomerService.getCustomersByZoneAndArea(zone, areaCd);
            Map<String, Object> response = new HashMap<>();
            response.put("zone", zone);
            response.put("area_code", areaCd);
            response.put("total_customers", customers.size());
            response.put("customers", customers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customers by zone and area");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Search customers by name
    @GetMapping("/search/name")
    public ResponseEntity<?> searchCustomersByName(@RequestParam String name,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            List<BulkCustomerDTO> customers = bulkCustomerService.searchCustomersByName(name);
            Map<String, Object> response = new HashMap<>();
            response.put("search_term", name);
            response.put("total_customers", customers.size());
            response.put("customers", customers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to search customers by name");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get customers by tariff
    @GetMapping("/tariff/{tariff}")
    public ResponseEntity<?> getCustomersByTariff(@PathVariable String tariff,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            List<BulkCustomerDTO> customers = bulkCustomerService.getCustomersByTariff(tariff);
            Map<String, Object> response = new HashMap<>();
            response.put("tariff", tariff);
            response.put("total_customers", customers.size());
            response.put("customers", customers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customers by tariff");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get customers by operational status
    @GetMapping("/status/{opStat}")
    public ResponseEntity<?> getCustomersByOpStat(@PathVariable String opStat,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            List<BulkCustomerDTO> customers = bulkCustomerService.getCustomersByOpStat(opStat);
            Map<String, Object> response = new HashMap<>();
            response.put("operational_status", opStat);
            response.put("total_customers", customers.size());
            response.put("customers", customers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customers by operational status");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get customer by mobile number
    @GetMapping("/mobile/{mobileNo}")
    public ResponseEntity<?> getCustomerByMobileNo(@PathVariable String mobileNo,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            Optional<BulkCustomerDTO> customer = bulkCustomerService.getCustomerByMobileNo(mobileNo);

            if (customer.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Customer found");
                response.put("mobile_number", mobileNo);
                response.put("customer", customer.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Customer not found");
                error.put("message", "No customer found with mobile number: " + mobileNo);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customer by mobile number");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get customers by city
    @GetMapping("/city/{city}")
    public ResponseEntity<?> getCustomersByCity(@PathVariable String city,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            List<BulkCustomerDTO> customers = bulkCustomerService.getCustomersByCity(city);
            Map<String, Object> response = new HashMap<>();
            response.put("city", city);
            response.put("total_customers", customers.size());
            response.put("customers", customers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customers by city");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get customers by bill cycle
    @GetMapping("/bill-cycle/{billCycle}")
    public ResponseEntity<?> getCustomersByBillCycle(@PathVariable Integer billCycle,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            List<BulkCustomerDTO> customers = bulkCustomerService.getCustomersByBillCycle(billCycle);
            Map<String, Object> response = new HashMap<>();
            response.put("bill_cycle", billCycle);
            response.put("total_customers", customers.size());
            response.put("customers", customers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve customers by bill cycle");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Search customers by multiple criteria
    @GetMapping("/search/advanced")
    public ResponseEntity<?> searchCustomersByMultipleCriteria(
            @RequestParam(required = false) String areaCd,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String tariff,
            @RequestParam(required = false) String opStat,
            @RequestParam(required = false) String cusCat,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, areaCd);
            List<BulkCustomerDTO> customers = bulkCustomerService.searchCustomersByMultipleCriteria(areaCd, zone,
                    tariff, opStat, cusCat);
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> searchCriteria = new HashMap<>();
            searchCriteria.put("area_code", areaCd);
            searchCriteria.put("zone", zone);
            searchCriteria.put("tariff", tariff);
            searchCriteria.put("operational_status", opStat);
            searchCriteria.put("customer_category", cusCat);
            response.put("search_criteria", searchCriteria);
            response.put("total_customers", customers.size());
            response.put("customers", customers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to search customers by multiple criteria");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // General search customers
    @GetMapping("/search")
    public ResponseEntity<?> searchCustomers(@RequestParam String searchTerm,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            List<BulkCustomerDTO> customers = bulkCustomerService.searchCustomers(searchTerm);
            Map<String, Object> response = new HashMap<>();
            response.put("search_term", searchTerm);
            response.put("total_customers", customers.size());
            response.put("customers", customers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to search customers");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Create new customer
    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody BulkCustomerDTO customerDTO) {
        try {
            BulkCustomerDTO createdCustomer = bulkCustomerService.createCustomer(customerDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Customer created successfully");
            response.put("customer", createdCustomer);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create customer");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to create customer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Update existing customer
    @PutMapping("/{accNbr}")
    public ResponseEntity<?> updateCustomer(@PathVariable String accNbr, @RequestBody BulkCustomerDTO customerDTO) {
        try {
            BulkCustomerDTO updatedCustomer = bulkCustomerService.updateCustomer(accNbr, customerDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Customer updated successfully");
            response.put("customer", updatedCustomer);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Customer not found");
                error.put("message", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update customer");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to update customer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get distinct area codes
    @GetMapping("/distinct/area-codes")
    public ResponseEntity<?> getDistinctAreaCodes() {
        try {
            List<String> areaCodes = bulkCustomerService.getDistinctAreaCodes();
            Map<String, Object> response = new HashMap<>();
            response.put("total_area_codes", areaCodes.size());
            response.put("area_codes", areaCodes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve distinct area codes");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get distinct zones
    @GetMapping("/distinct/zones")
    public ResponseEntity<?> getDistinctZones() {
        try {
            List<String> zones = bulkCustomerService.getDistinctZones();
            Map<String, Object> response = new HashMap<>();
            response.put("total_zones", zones.size());
            response.put("zones", zones);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve distinct zones");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get distinct tariffs
    @GetMapping("/distinct/tariffs")
    public ResponseEntity<?> getDistinctTariffs() {
        try {
            List<String> tariffs = bulkCustomerService.getDistinctTariffs();
            Map<String, Object> response = new HashMap<>();
            response.put("total_tariffs", tariffs.size());
            response.put("tariffs", tariffs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve distinct tariffs");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get distinct customer categories
    @GetMapping("/distinct/categories")
    public ResponseEntity<?> getDistinctCusCategories() {
        try {
            List<String> categories = bulkCustomerService.getDistinctCusCategories();
            Map<String, Object> response = new HashMap<>();
            response.put("total_categories", categories.size());
            response.put("categories", categories);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve distinct customer categories");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get distinct cities
    @GetMapping("/distinct/cities")
    public ResponseEntity<?> getDistinctCities() {
        try {
            List<String> cities = bulkCustomerService.getDistinctCities();
            Map<String, Object> response = new HashMap<>();
            response.put("total_cities", cities.size());
            response.put("cities", cities);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve distinct cities");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Count customers by area
    @GetMapping("/count/area/{areaCd}")
    public ResponseEntity<?> countCustomersByArea(@PathVariable String areaCd,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, areaCd);
            Long count = bulkCustomerService.countCustomersByArea(areaCd);
            Map<String, Object> response = new HashMap<>();
            response.put("area_code", areaCd);
            response.put("customer_count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to count customers by area");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Count customers by zone
    @GetMapping("/count/zone/{zone}")
    public ResponseEntity<?> countCustomersByZone(@PathVariable String zone,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            Long count = bulkCustomerService.countCustomersByZone(zone);
            Map<String, Object> response = new HashMap<>();
            response.put("zone", zone);
            response.put("customer_count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to count customers by zone");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Count customers by operational status
    @GetMapping("/count/status/{opStat}")
    public ResponseEntity<?> countCustomersByOpStat(@PathVariable String opStat,
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {
        try {
            validateSessionAndAccess(session_id, user_id, null, null, null);
            Long count = bulkCustomerService.countCustomersByOpStat(opStat);
            Map<String, Object> response = new HashMap<>();
            response.put("operational_status", opStat);
            response.put("customer_count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to count customers by operational status");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // UPDATED: Helper method to validate session and access
    private void validateSessionAndAccess(String sessionId, String userId, String targetRegionCode,
            String targetProvinceCode, String targetAreaCode) {
        if (sessionId != null && userId != null && !sessionId.isEmpty() && !userId.isEmpty()) {
            try {
                // Validate session
                SecInfoLoginDTO.SessionValidationRequest validationRequest = new SecInfoLoginDTO.SessionValidationRequest();
                validationRequest.setSessionId(sessionId);
                validationRequest.setUserId(userId);
                SecInfoLoginDTO.SessionValidationResponse validationResponse = secInfoAuthService
                        .validateSession(validationRequest);

                if (!validationResponse.getValid()) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session");
                }

                // If area-specific, validate access - FIXED LOGIC
                if (targetAreaCode != null) {
                    Optional<HsbAreaDTO> areaOpt = locationService.getAreaByCode(targetAreaCode);
                    if (areaOpt.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Area not found");
                    }

                    HsbAreaDTO area = areaOpt.get();

                    // Get user info from session validation response
                    SecInfoLoginDTO.UserInfo userInfo = validationResponse.getUserInfo();
                    if (userInfo != null) {
                        boolean hasAccess = hasAreaAccessBasedOnUserCategory(userInfo, area.getRegion(),
                                area.getProvCode(), targetAreaCode);
                        if (!hasAccess) {
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this area");
                        }
                    } else {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                "User information not found in session");
                    }
                }
            } catch (ResponseStatusException e) {
                throw e; // Re-throw existing exceptions
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Session validation error: " + e.getMessage());
            }
        }
        // If no session params provided, allow the request to proceed (for public
        // endpoints or internal calls)
    }

    /**
     * Area access is derived from the sec_info region/province/area hierarchy:
     * a NULL code widens the scope to the level above.
     */
    private boolean hasAreaAccessBasedOnUserCategory(SecInfoLoginDTO.UserInfo userInfo, String targetRegion,
            String targetProvince, String targetArea) {
        if (userInfo == null) {
            return false;
        }
        return areaPermissionService.isAreaPermitted(userInfo.getRegionCode(), userInfo.getProvinceCode(),
                userInfo.getAreaCode(), targetArea);
    }

    // ================= MAP DATA (FAST LOADING) =================
    @GetMapping("/map-data")
    public ResponseEntity<?> getMapData(
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {

        try {
            // Optional: validate session (same pattern you use elsewhere)
            validateSessionAndAccess(session_id, user_id, null, null, null);

            boolean isEE = sessionUtils.isEEUser(session_id, user_id);
            String eeFilter = isEE ? user_id : null;
            List<MapDataDTO> mapData = bulkCustomerService.getMapData(eeFilter);

            Map<String, Object> response = new HashMap<>();
            response.put("total_locations", mapData.size());
            response.put("data", mapData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to load map data");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}