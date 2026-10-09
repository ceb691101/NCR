package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.UserAccSecInfoDTO;
import com.example.SPSProjectBackend.service.UserAccSecInfoService;
import com.example.SPSProjectBackend.service.UserAreaPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/accounts")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserAccSecInfoController {

    @Autowired
    private UserAccSecInfoService userAccSecInfoService;

    /**
     * Validates the region/province/area hierarchy. A NULL code widens the user's scope to
     * everything below it, so a lower level code requires its parent to be set.
     * Returns null when the codes are valid.
     */
    private String validateLocationCodes(UserAccSecInfoDTO userAccSecInfoDTO) {
        String regionCode = UserAreaPermissionService.normalizeCode(userAccSecInfoDTO.getRegionCode());
        String provinceCode = UserAreaPermissionService.normalizeCode(userAccSecInfoDTO.getProvinceCode());
        String areaCode = UserAreaPermissionService.normalizeCode(userAccSecInfoDTO.getAreaCode());

        if (regionCode == null && (provinceCode != null || areaCode != null)) {
            return "Region code is required when a province or area code is set";
        }
        if (provinceCode == null && areaCode != null) {
            return "Province code is required when an area code is set";
        }

        return null; // No validation errors
    }

    // Get all user accounts (including status)
    @GetMapping
    public ResponseEntity<?> getAllUserAccSecInfos() {
        try {
            List<UserAccSecInfoDTO> accounts = userAccSecInfoService.getAllUserAccSecInfos();
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve user accounts");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // NEW: Get all active users only
    @GetMapping("/active")
    public ResponseEntity<?> getAllActiveUsers() {
        try {
            List<UserAccSecInfoDTO> accounts = userAccSecInfoService.getAllActiveUsers();
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve active user accounts");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // NEW: Get all inactive users only
    @GetMapping("/inactive")
    public ResponseEntity<?> getAllInactiveUsers() {
        try {
            List<UserAccSecInfoDTO> accounts = userAccSecInfoService.getAllInactiveUsers();
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve inactive user accounts");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get user account by ID
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserAccSecInfoById(@PathVariable String userId) {
        try {
            Optional<UserAccSecInfoDTO> account = userAccSecInfoService.getUserAccSecInfoById(userId);
            if (account.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "User account retrieved successfully");
                response.put("user", account.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                error.put("message", "User with ID " + userId + " not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (RuntimeException e) {
            e.printStackTrace(); // For debugging
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve user account");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to retrieve user account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Create new user account (status automatically set to 1 - active)
    @PostMapping
        public ResponseEntity<?> createUserAccSecInfo(
            @RequestBody UserAccSecInfoDTO userAccSecInfoDTO,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        try {
            // Validation
            if (userAccSecInfoDTO.getUserId() == null || userAccSecInfoDTO.getUserId().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Validation failed");
                error.put("message", "User ID is required");
                return ResponseEntity.badRequest().body(error);
            }
            if (userAccSecInfoDTO.getUserId().trim().length() > 10) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Validation failed");
                error.put("message", "User ID must not exceed 10 characters");
                return ResponseEntity.badRequest().body(error);
            }

            if (userAccSecInfoDTO.getUserName() == null || userAccSecInfoDTO.getUserName().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Validation failed");
                error.put("message", "User name is required");
                return ResponseEntity.badRequest().body(error);
            }

            // EPF number validation (required)
            if (userAccSecInfoDTO.getEpfNum() == null || userAccSecInfoDTO.getEpfNum().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Validation failed");
                error.put("message", "EPF number is required");
                return ResponseEntity.badRequest().body(error);
            }

            if (userAccSecInfoDTO.getUserCat() == null || userAccSecInfoDTO.getUserCat().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Validation failed");
                error.put("message", "User category is required");
                return ResponseEntity.badRequest().body(error);
            }

            // Validate location codes based on user category
            String locationError = validateLocationCodes(userAccSecInfoDTO);
            if (locationError != null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Validation failed");
                error.put("message", locationError);
                return ResponseEntity.badRequest().body(error);
            }

            // Note: Status is not validated here as it's automatically set to 1 (active) for new users
            UserAccSecInfoDTO createdAccount = userAccSecInfoService.createUserAccSecInfo(userAccSecInfoDTO, sessionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User account created successfully");
            response.put("user", createdAccount);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create user account");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to create user account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Update user account (including status)
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUserAccSecInfo(@PathVariable String userId, @RequestBody UserAccSecInfoDTO userAccSecInfoDTO) {
        try {
            // Validate location codes if user category is provided
            if (userAccSecInfoDTO.getUserCat() != null && !userAccSecInfoDTO.getUserCat().trim().isEmpty()) {
                String locationError = validateLocationCodes(userAccSecInfoDTO);
                if (locationError != null) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Validation failed");
                    error.put("message", locationError);
                    return ResponseEntity.badRequest().body(error);
                }
            }

            // Validate status if provided
            if (userAccSecInfoDTO.getStatus() != null) {
                if (userAccSecInfoDTO.getStatus() != 0 && userAccSecInfoDTO.getStatus() != 1) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Validation failed");
                    error.put("message", "Status must be 0 (inactive) or 1 (active)");
                    return ResponseEntity.badRequest().body(error);
                }
            }

            UserAccSecInfoDTO updatedAccount = userAccSecInfoService.updateUserAccSecInfo(userId, userAccSecInfoDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User account updated successfully");
            response.put("user", updatedAccount);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                error.put("message", e.getMessage());
                return ResponseEntity.notFound().build();
            }
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update user account");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to update user account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // NEW: Toggle user status (activate/deactivate) - Perfect for frontend toggle button
    @PutMapping("/{userId}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable String userId) {
        try {
            UserAccSecInfoDTO updatedAccount = userAccSecInfoService.toggleUserStatus(userId);
            
            Map<String, Object> response = new HashMap<>();
            String statusText = updatedAccount.getStatus() == 1 ? "activated" : "deactivated";
            response.put("message", "User account " + statusText + " successfully");
            response.put("user", updatedAccount);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                error.put("message", e.getMessage());
                return ResponseEntity.notFound().build();
            }
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to toggle user status");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to toggle user status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // NEW: Set user status explicitly (activate/deactivate)
    @PutMapping("/{userId}/status/{status}")
    public ResponseEntity<?> setUserStatus(@PathVariable String userId, @PathVariable Integer status) {
        try {
            if (status != 0 && status != 1) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Validation failed");
                error.put("message", "Status must be 0 (inactive) or 1 (active)");
                return ResponseEntity.badRequest().body(error);
            }

            UserAccSecInfoDTO updatedAccount = userAccSecInfoService.setUserStatus(userId, status);
            
            Map<String, Object> response = new HashMap<>();
            String statusText = status == 1 ? "activated" : "deactivated";
            response.put("message", "User account " + statusText + " successfully");
            response.put("user", updatedAccount);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                error.put("message", e.getMessage());
                return ResponseEntity.notFound().build();
            }
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to set user status");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "Failed to set user status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // NEW: Check if user is active
    @GetMapping("/{userId}/status")
    public ResponseEntity<?> getUserStatus(@PathVariable String userId) {
        try {
            Optional<UserAccSecInfoDTO> account = userAccSecInfoService.getUserAccSecInfoById(userId);
            if (account.isPresent()) {
                boolean isActive = userAccSecInfoService.isUserActive(userId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("user_id", userId);
                response.put("is_active", isActive);
                response.put("status", account.get().getStatus());
                response.put("status_text", isActive ? "Active" : "Inactive");
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                error.put("message", "User with ID " + userId + " not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get user status");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // REMOVED: Delete user account endpoint - Users are never deleted from the system
    // @DeleteMapping("/{userId}") - REMOVED COMPLETELY

    // Get users by category
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getUsersByCategory(@PathVariable String category) {
        try {
            List<UserAccSecInfoDTO> accounts = userAccSecInfoService.getUsersByCategory(category);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve users by category");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get users by region code
    @GetMapping("/region/{regionCode}")
    public ResponseEntity<?> getUsersByRegionCode(@PathVariable String regionCode) {
        try {
            List<UserAccSecInfoDTO> accounts = userAccSecInfoService.getUsersByRegionCode(regionCode);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve users by region code");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get users by province code
    @GetMapping("/province/{provinceCode}")
    public ResponseEntity<?> getUsersByProvinceCode(@PathVariable String provinceCode) {
        try {
            List<UserAccSecInfoDTO> accounts = userAccSecInfoService.getUsersByProvinceCode(provinceCode);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve users by province code");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get users by area code
    @GetMapping("/area/{areaCode}")
    public ResponseEntity<?> getUsersByAreaCode(@PathVariable String areaCode) {
        try {
            List<UserAccSecInfoDTO> accounts = userAccSecInfoService.getUsersByAreaCode(areaCode);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve users by area code");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Get user by EPF number
    @GetMapping("/epf/{epfNum}")
    public ResponseEntity<?> getUserByEpfNum(@PathVariable String epfNum) {
        try {
            Optional<UserAccSecInfoDTO> account = userAccSecInfoService.getUserByEpfNum(epfNum);
            if (account.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "User account retrieved successfully");
                response.put("user", account.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                error.put("message", "User with EPF number " + epfNum + " not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve user by EPF number");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

}