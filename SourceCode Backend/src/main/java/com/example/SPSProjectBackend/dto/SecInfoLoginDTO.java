// SecInfoSessionData
package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class SecInfoLoginDTO {
    
    // Login Request DTOs
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("password")
        private String password;

        @JsonProperty("login_type")
        private String loginType; // "HR" or "AD"
    }
    
    // Login Response DTOs
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        @JsonProperty("success")
        private Boolean success;

        @JsonProperty("authenticated")
        private Boolean authenticated;

        @JsonProperty("has_ncre_access")
        private Boolean hasNcreAccess;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_info")
        private UserInfo userInfo;
        
        @JsonProperty("login_time")
        private LocalDateTime loginTime;
        
        @JsonProperty("expires_at")
        private LocalDateTime expiresAt;
    }
    
    // Updated User Info DTO for response with location codes
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("user_name")
        private String userName;
        
        @JsonProperty("user_category")
        private String userCategory;
        
        // Location codes exactly as stored in sec_info. A NULL code means "all of the level below".
        @JsonProperty("region_code")
        private String regionCode;
        
        @JsonProperty("province_code")
        private String provinceCode;
        
        @JsonProperty("area_code")
        private String areaCode;

        /**
         * Resolved at login from the region/province/area hierarchy:
         * ALL | REGION | PROVINCE | AREA
         */
        @JsonProperty("access_scope")
        private String accessScope;

        /**
         * Every area code the user is permitted to read. Data for all of these is loaded
         * by default; the header bar may narrow it down to one.
         */
        @JsonProperty("permitted_areas")
        private List<BillCycleDTO.AreaBillCycleDTO> permittedAreas;

        /**
         * Area currently narrowed to from the header bar. NULL means all permitted areas.
         */
        @JsonProperty("selected_area_code")
        private String selectedAreaCode;

        // Constructor without location codes (for backward compatibility)
        public UserInfo(String userId, String userName, String userCategory) {
            this.userId = userId;
            this.userName = userName;
            this.userCategory = userCategory;
        }
    }

    /**
     * Body for changing the header-bar area selection.
     * A NULL / blank / "ALL" area_code resets the selection back to all permitted areas.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectAreaRequest {
        @JsonProperty("session_id")
        private String sessionId;

        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("area_code")
        private String areaCode;
    }
    
    // Logout Request DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogoutRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
    }
    
    // Logout Response DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogoutResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("logout_time")
        private LocalDateTime logoutTime;
    }
    
    // Session Validation Request DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionValidationRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
    }
    
    // Updated Session Validation Response DTO with location codes
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionValidationResponse {
        @JsonProperty("valid")
        private Boolean valid;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("user_info")
        private UserInfo userInfo;
        
        @JsonProperty("last_access_time")
        private LocalDateTime lastAccessTime;
        
        @JsonProperty("expires_at")
        private LocalDateTime expiresAt;
    }
}