package com.example.SPSProjectBackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

import java.time.LocalDateTime;

@RedisHash("SecInfoSession")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecInfoSessionData {
    
    @Id
    private String sessionId;
    
    private String userId;
    
    private String userName;
    
    private String userCategory;
    
    // Add location fields for global session access
    private String regionCode;
    
    private String provinceCode;
    
    private String areaCode;
    
    private LocalDateTime loginTime;
    
    private LocalDateTime lastAccessTime;
    
    private String ipAddress;
    
    private String userAgent;

    // Add to store active bill cycles in session
    private Map<String, Integer> activeBillCycles;

    // Area codes resolved from the sec_info region/province/area hierarchy.
    // Every area the user may read. Loaded once at login.
    private List<String> permittedAreaCodes;

    // Area the user narrowed to from the header bar. NULL/blank means "all permitted areas",
    // which is the default right after login.
    private String selectedAreaCode;

    // TTL in seconds (24 hours = 86400 seconds)
    @TimeToLive
    private Long timeToLive = 86400L;
    
    // Constructor without TTL (uses default)
    public SecInfoSessionData(String sessionId, String userId, String userName, 
                             String userCategory, String regionCode, String provinceCode, 
                             String areaCode, LocalDateTime loginTime, 
                             LocalDateTime lastAccessTime, String ipAddress, String userAgent) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.userName = userName;
        this.userCategory = userCategory;
        this.regionCode = regionCode;
        this.provinceCode = provinceCode;
        this.areaCode = areaCode;
        this.loginTime = loginTime;
        this.lastAccessTime = lastAccessTime;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.timeToLive = 86400L; // 24 hours default
    }

    public List<String> getPermittedAreaCodes() {
        return permittedAreaCodes;
    }

    public void setPermittedAreaCodes(List<String> permittedAreaCodes) {
        this.permittedAreaCodes = permittedAreaCodes;
    }

    public String getSelectedAreaCode() {
        return selectedAreaCode;
    }

    public void setSelectedAreaCode(String selectedAreaCode) {
        this.selectedAreaCode = selectedAreaCode;
    }

    // Add getter and setter for active bill cycles
    public Map<String, Integer> getActiveBillCycles() {
        return activeBillCycles;
    }

    public void setActiveBillCycles(Map<String, Integer> activeBillCycles) {
        this.activeBillCycles = activeBillCycles;
    }
}