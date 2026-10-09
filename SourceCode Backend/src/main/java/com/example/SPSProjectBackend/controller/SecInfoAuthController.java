package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.BillCycleDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.service.SecInfoAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SPSProjectBackend.service.BillCycleService;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/secinfo")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class SecInfoAuthController {

    @Autowired
    private SecInfoAuthService secInfoAuthService;

    @Autowired
    private BillCycleService billCycleService;

    /**
     * User Login Endpoint - Updated to include location codes based on user category
     * Update the login method to include bill cycles
     */
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> login(@RequestBody SecInfoLoginDTO.LoginRequest loginRequest, 
                                HttpServletRequest request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Get client information
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            // Authenticate user
            SecInfoLoginDTO.LoginResponse response = secInfoAuthService.authenticateUser(
                loginRequest, ipAddress, userAgent);

            // Convert to Map to avoid Jackson serialization issues
            responseMap.put("success", response.getSuccess());
            responseMap.put("authenticated", response.getAuthenticated() != null ? response.getAuthenticated() : false);
            responseMap.put("has_ncre_access", response.getHasNcreAccess() != null ? response.getHasNcreAccess() : false);
            responseMap.put("message", response.getMessage());
            
            if (response.getSuccess()) {
                responseMap.put("session_id", response.getSessionId());
                
                if (response.getUserInfo() != null) {
                    Map<String, Object> userInfo = buildUserInfoMap(response.getUserInfo());
                    responseMap.put("user_info", userInfo);

                    // The permitted areas are already resolved at login, so the client can
                    // load data for all of them immediately without prompting the user.
                    List<?> permittedAreas = response.getUserInfo().getPermittedAreas();
                    responseMap.put("permitted_areas",
                            permittedAreas != null ? permittedAreas : new ArrayList<>());
                    responseMap.put("permitted_area_count",
                            permittedAreas != null ? permittedAreas.size() : 0);
                    responseMap.put("access_scope", response.getUserInfo().getAccessScope());
                    responseMap.put("selected_area_code", null);

                    // Kept for backwards compatibility with existing clients.
                    try {
                        BillCycleDTO.BillCycleResponse billCycleResponse = billCycleService.getBillCyclesForUser(
                            response.getSessionId(), response.getUserInfo().getUserId());

                        if (billCycleResponse.getSuccess()) {
                            responseMap.put("bill_cycles", billCycleResponse.getBillCycles());

                            int totalAreas = billCycleResponse.getBillCycles().size();
                            long areasWithCycles = billCycleResponse.getBillCycles().stream()
                                    .filter(BillCycleDTO.AreaBillCycleDTO::getHasBillCycle)
                                    .count();

                            Map<String, Object> billCycleSummary = new HashMap<>();
                            billCycleSummary.put("total_areas", totalAreas);
                            billCycleSummary.put("areas_with_cycles", areasWithCycles);
                            billCycleSummary.put("areas_without_cycles", totalAreas - areasWithCycles);

                            responseMap.put("bill_cycle_summary", billCycleSummary);
                        }
                    } catch (Exception e) {
                        // Log error but don't fail login
                        System.err.println("Failed to get bill cycles during login: " + e.getMessage());
                        responseMap.put("bill_cycles", new ArrayList<>());
                    }
                }
                
                responseMap.put("login_time", response.getLoginTime() != null ? response.getLoginTime().toString() : null);
                responseMap.put("expires_at", response.getExpiresAt() != null ? response.getExpiresAt().toString() : null);
                
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMap);
            }

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Login failed: " + e.getMessage());
            responseMap.put("error", "INTERNAL_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            // Log the error for debugging
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * User Logout Endpoint
     */
    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> logout(@RequestBody SecInfoLoginDTO.LogoutRequest logoutRequest) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            SecInfoLoginDTO.LogoutResponse response = secInfoAuthService.logoutUser(logoutRequest);
            
            responseMap.put("success", response.getSuccess());
            responseMap.put("message", response.getMessage());
            responseMap.put("logout_time", response.getLogoutTime() != null ? response.getLogoutTime().toString() : null);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.badRequest().body(responseMap);
            }

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Logout failed: " + e.getMessage());
            responseMap.put("error", "INTERNAL_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Session Validation Endpoint - Updated to include location codes
     */
    @PostMapping(value = "/validate-session", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> validateSession(@RequestBody SecInfoLoginDTO.SessionValidationRequest validationRequest) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            SecInfoLoginDTO.SessionValidationResponse response = secInfoAuthService.validateSession(validationRequest);
            
            responseMap.put("valid", response.getValid());
            responseMap.put("message", response.getMessage());
            
            if (response.getValid() && response.getUserInfo() != null) {
                responseMap.put("user_info", buildUserInfoMap(response.getUserInfo()));

                List<?> permittedAreas = response.getUserInfo().getPermittedAreas();
                responseMap.put("permitted_areas",
                        permittedAreas != null ? permittedAreas : new ArrayList<>());
                responseMap.put("permitted_area_count",
                        permittedAreas != null ? permittedAreas.size() : 0);
                responseMap.put("access_scope", response.getUserInfo().getAccessScope());
                responseMap.put("selected_area_code", response.getUserInfo().getSelectedAreaCode());

                responseMap.put("last_access_time", response.getLastAccessTime() != null ? response.getLastAccessTime().toString() : null);
                responseMap.put("expires_at", response.getExpiresAt() != null ? response.getExpiresAt().toString() : null);
            }
            
            if (response.getValid()) {
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMap);
            }

        } catch (Exception e) {
            responseMap.put("valid", false);
            responseMap.put("message", "Session validation failed: " + e.getMessage());
            responseMap.put("error", "INTERNAL_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Get user location data from session - New endpoint for global access
     */
    @PostMapping(value = "/get-user-location", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getUserLocationFromSession(@RequestBody Map<String, String> request) {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            String sessionId = request.get("session_id");
            String userId = request.get("user_id");
            
            if (sessionId == null || sessionId.trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Session ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            var userLocationOptional = secInfoAuthService.getUserLocationFromSession(sessionId, userId);
            
            if (userLocationOptional.isPresent()) {
                SecInfoLoginDTO.UserInfo userInfo = userLocationOptional.get();

                Map<String, Object> locationData = buildUserInfoMap(userInfo);
                List<?> permittedAreas = userInfo.getPermittedAreas();
                locationData.put("permitted_areas", permittedAreas != null ? permittedAreas : new ArrayList<>());
                locationData.put("permitted_area_count", permittedAreas != null ? permittedAreas.size() : 0);

                responseMap.put("success", true);
                responseMap.put("message", "User location data retrieved successfully");
                responseMap.put("user_location", locationData);

                return ResponseEntity.ok(responseMap);
            } else {
                responseMap.put("success", false);
                responseMap.put("message", "Invalid session or session expired");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMap);
            }

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve user location: " + e.getMessage());
            responseMap.put("error", "INTERNAL_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Areas the logged in user is permitted to read, resolved from the
     * region_code / province_code / area_code values in sec_info.
     *
     * No area is selected here. A NULL region_code means every area, NULL province_code means
     * every area in that region, and NULL area_code means every area in that province.
     */
    @PostMapping(value = "/permitted-areas", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getPermittedAreas(@RequestBody Map<String, String> request) {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            String sessionId = request.get("session_id");
            String userId = request.get("user_id");

            if (sessionId == null || sessionId.trim().isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Session ID is required");
                return ResponseEntity.badRequest().body(responseMap);
            }

            Optional<SecInfoLoginDTO.UserInfo> userInfoOptional =
                    secInfoAuthService.getUserLocationFromSession(sessionId, userId);

            if (userInfoOptional.isEmpty()) {
                responseMap.put("success", false);
                responseMap.put("message", "Invalid session or session expired");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseMap);
            }

            SecInfoLoginDTO.UserInfo userInfo = userInfoOptional.get();
            List<?> permittedAreas = userInfo.getPermittedAreas();

            responseMap.put("success", true);
            responseMap.put("message", "Permitted areas retrieved successfully");
            responseMap.put("access_scope", userInfo.getAccessScope());
            responseMap.put("selected_area_code", userInfo.getSelectedAreaCode());
            responseMap.put("permitted_areas", permittedAreas != null ? permittedAreas : new ArrayList<>());
            responseMap.put("permitted_area_count", permittedAreas != null ? permittedAreas.size() : 0);

            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", "Failed to retrieve permitted areas: " + e.getMessage());
            responseMap.put("error", "INTERNAL_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
        }
    }

    /**
     * Narrow the session to one permitted area from the header bar.
     * A blank / missing / "ALL" area_code resets back to loading every permitted area.
     */
    @PostMapping(value = "/select-area", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> selectArea(@RequestBody SecInfoLoginDTO.SelectAreaRequest selectAreaRequest) {
        Map<String, Object> result = secInfoAuthService.selectArea(selectAreaRequest);

        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        }
        String message = String.valueOf(result.getOrDefault("message", "Invalid session or area"));
        if (message.contains("Invalid") || message.contains("expired") || message.contains("belongs")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }

    /**
     * Builds the client facing user info payload. The sec_info codes are passed through as-is
     * because a NULL code is meaningful: it widens the user's scope to the level above.
     */
    private Map<String, Object> buildUserInfoMap(SecInfoLoginDTO.UserInfo userInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", userInfo.getUserId());
        map.put("user_name", userInfo.getUserName());
        map.put("user_category", userInfo.getUserCategory());
        map.put("region_code", userInfo.getRegionCode());
        map.put("province_code", userInfo.getProvinceCode());
        map.put("area_code", userInfo.getAreaCode());
        map.put("access_scope", userInfo.getAccessScope());
        map.put("selected_area_code", userInfo.getSelectedAreaCode());
        return map;
    }

    /**
     * Check System Health (Session Store connectivity)
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> responseMap = new HashMap<>();
        
        try {
            // Try to perform a simple operation to check Redis connectivity
            // We'll do a simple check by trying to validate an empty session (which will fail gracefully)
            SecInfoLoginDTO.SessionValidationRequest testRequest = new SecInfoLoginDTO.SessionValidationRequest();
            testRequest.setSessionId("health-check");
            
            // This will return false but won't throw exception if Redis is working
            secInfoAuthService.validateSession(testRequest);
            
            responseMap.put("status", "healthy");
            responseMap.put("message", "SecInfo authentication system is operational");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            responseMap.put("status", "unhealthy");
            responseMap.put("message", "SecInfo authentication system has issues: " + e.getMessage());
            responseMap.put("error", "SYSTEM_ERROR");
            responseMap.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(responseMap);
        }
    }

    // Helper method to get client IP address
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // Handle multiple IP addresses (take the first one)
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        return ipAddress;
    }
}