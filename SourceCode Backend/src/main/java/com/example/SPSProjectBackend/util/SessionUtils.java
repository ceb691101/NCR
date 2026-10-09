// SecInfoSessionData
package com.example.SPSProjectBackend.util;

import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.model.SecInfoSessionData;
import com.example.SPSProjectBackend.service.SecInfoAuthService;
import com.example.SPSProjectBackend.service.UserAreaPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for accessing user session data globally throughout the application
 */
@Component
public class SessionUtils {

    @Autowired
    @Lazy
    private SecInfoAuthService secInfoAuthService;

    @Autowired(required = false)
    @Lazy
    private com.example.SPSProjectBackend.repository.UserAccSecInfoRepository userAccSecInfoRepository;

    @Autowired
    @Lazy
    private UserAreaPermissionService areaPermissionService;

    /**
     * Get complete session data by session ID
     */
    public Optional<SecInfoSessionData> getSessionData(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return Optional.empty();
        }
        return secInfoAuthService.getSessionData(sessionId);
    }

    /**
     * Get user's location information from session
     */
    public Optional<SecInfoLoginDTO.UserInfo> getUserLocationFromSession(String sessionId, String userId) {
        return secInfoAuthService.getUserLocationFromSession(sessionId, userId);
    }

    /**
     * Check if user has access to a specific region.
     *
     * Derived from the sec_info code hierarchy: a NULL region_code grants every region,
     * otherwise only the assigned region is reachable.
     */
    public boolean hasRegionAccess(String sessionId, String userId, String targetRegionCode) {
        try {
            Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = getUserLocationFromSession(sessionId, userId);
            if (userInfoOpt.isEmpty()) {
                return false;
            }

            String assignedRegion = trimToNull(userInfoOpt.get().getRegionCode());
            if (assignedRegion == null) {
                return true;
            }

            String target = trimToNull(targetRegionCode);
            return target != null && assignedRegion.equals(target);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if user has access to a specific province using the same hierarchy.
     */
    public boolean hasProvinceAccess(String sessionId, String userId, String targetRegionCode, String targetProvinceCode) {
        try {
            Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = getUserLocationFromSession(sessionId, userId);
            if (userInfoOpt.isEmpty()) {
                return false;
            }

            SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();
            String assignedRegion = trimToNull(userInfo.getRegionCode());
            String assignedProvince = trimToNull(userInfo.getProvinceCode());

            if (assignedRegion == null) {
                return true;
            }

            String targetRegion = trimToNull(targetRegionCode);
            if (targetRegion == null || !assignedRegion.equals(targetRegion)) {
                return false;
            }
            if (assignedProvince == null) {
                return true;
            }

            String targetProvince = trimToNull(targetProvinceCode);
            return targetProvince != null && assignedProvince.equals(targetProvince);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if user has access to a specific area.
     *
     * Each NULL level in sec_info widens the scope one step: NULL region means all areas,
     * NULL province means all areas in the region, NULL area means all areas in the province.
     * The codes supplied by the caller are accepted as-is and compared against that scope.
     */
    public boolean hasAreaAccess(String sessionId, String userId, String targetRegionCode, 
                                String targetProvinceCode, String targetAreaCode) {
        try {
            Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = getUserLocationFromSession(sessionId, userId);
            if (userInfoOpt.isEmpty()) {
                return false;
            }

            SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();
            String assignedRegion = trimToNull(userInfo.getRegionCode());
            String assignedProvince = trimToNull(userInfo.getProvinceCode());
            String assignedArea = normalizeAreaCode(userInfo.getAreaCode());

            if (assignedRegion == null) {
                return true;
            }

            String targetRegion = trimToNull(targetRegionCode);
            if (targetRegion == null || !assignedRegion.equals(targetRegion)) {
                return false;
            }
            if (assignedProvince == null) {
                return true;
            }

            String targetProvince = trimToNull(targetProvinceCode);
            if (targetProvince == null || !assignedProvince.equals(targetProvince)) {
                return false;
            }
            if (assignedArea == null) {
                return true;
            }

            String targetArea = normalizeAreaCode(targetAreaCode);
            return targetArea != null && assignedArea.equals(targetArea);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Areas the user is permitted to read, resolved from the sec_info code hierarchy.
     */
    public List<String> getPermittedAreaCodes(String sessionId, String userId) {
        try {
            return areaPermissionService.resolvePermittedAreaCodes(sessionId, userId);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * The area currently narrowed to from the header bar, or NULL when showing all permitted areas.
     */
    public String getSelectedAreaCode(String sessionId) {
        return getSessionData(sessionId)
                .map(SecInfoSessionData::getSelectedAreaCode)
                .map(SessionUtils::trimToNull)
                .orElse(null);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** Area codes are CHAR(2) ("01") but arrive from clients as "1". */
    private static String normalizeAreaCode(String areaCode) {
        String code = trimToNull(areaCode);
        return code == null ? null : code.replaceFirst("^0+(?!$)", "");
    }

    /**
     * Get user's region code from session
     */
    public Optional<String> getUserRegionCode(String sessionId, String userId) {
        return getUserLocationFromSession(sessionId, userId)
                .map(SecInfoLoginDTO.UserInfo::getRegionCode);
    }

    /**
     * Get user's province code from session
     */
    public Optional<String> getUserProvinceCode(String sessionId, String userId) {
        return getUserLocationFromSession(sessionId, userId)
                .map(SecInfoLoginDTO.UserInfo::getProvinceCode);
    }

    /**
     * Get user's area code from session
     */
    public Optional<String> getUserAreaCode(String sessionId, String userId) {
        return getUserLocationFromSession(sessionId, userId)
                .map(SecInfoLoginDTO.UserInfo::getAreaCode);
    }

    /**
     * Get user category from session
     */
    public Optional<String> getUserCategory(String sessionId, String userId) {
        return getUserLocationFromSession(sessionId, userId)
                .map(SecInfoLoginDTO.UserInfo::getUserCategory);
    }

    /**
     * Check if session is valid and active
     */
    public boolean isSessionValid(String sessionId, String userId) {
        try {
            Optional<SecInfoSessionData> sessionOpt = getSessionData(sessionId);
            if (!sessionOpt.isPresent()) {
                return false;
            }

            SecInfoSessionData session = sessionOpt.get();
            return userId == null || userId.equals(session.getUserId());

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Clean and normalize user ID or EE ID string
     */
    public static String cleanId(String str) {
        if (str == null) return "";
        return str.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    /**
     * Match responsible EE against user ID
     */
    public static boolean matchEE(String responsibleEe, String userId) {
        if (responsibleEe == null || userId == null) return false;
        String cResp = cleanId(responsibleEe);
        String cUser = cleanId(userId);
        if (cResp.isEmpty() || cUser.isEmpty()) return false;
        return cResp.equals(cUser) || cResp.contains(cUser);
    }

    /**
     * Check if user category represents an Electrical Engineer (EE)
     */
    public static boolean isEEUserCategory(String userCategory) {
        if (userCategory == null) return false;
        return "EE".equalsIgnoreCase(userCategory.trim()) || 
               "Electrical Engineer".equalsIgnoreCase(userCategory.trim());
    }

    /**
     * Check if the user is an EE by session and user ID
     */
    public boolean isEEUser(String sessionId, String userId) {
        if (userId == null || userId.trim().isEmpty()) return false;
        Optional<String> categoryOpt = getUserCategory(sessionId, userId);
        if (categoryOpt.isPresent() && categoryOpt.get() != null && !categoryOpt.get().trim().isEmpty()) {
            return isEEUserCategory(categoryOpt.get());
        }
        // Fallback: check user repository directly if session record is missing or expired
        if (userAccSecInfoRepository != null) {
            try {
                return userAccSecInfoRepository.findByIdTrimmed(userId)
                        .map(com.example.SPSProjectBackend.model.UserAccSecInfo::getUserCat)
                        .map(SessionUtils::isEEUserCategory)
                        .orElse(false);
            } catch (Exception e) {
                // Ignore and return false
            }
        }
        return false;
    }
}