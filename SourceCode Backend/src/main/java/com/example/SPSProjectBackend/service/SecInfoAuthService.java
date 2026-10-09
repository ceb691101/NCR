// SecInfoSessionData
package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.BillCycleDTO;
import com.example.SPSProjectBackend.dto.HsbAreaDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.model.SecInfoSessionData;
import com.example.SPSProjectBackend.model.UserAccSecInfo;
import com.example.SPSProjectBackend.repository.SecInfoSessionRepository;
import com.example.SPSProjectBackend.repository.UserAccSecInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import util.common.Encryption;
import com.example.SPSProjectBackend.service.BillCycleService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SecInfoAuthService {

    @Autowired
    private UserAccSecInfoRepository userAccSecInfoRepository;

    @Autowired
    private SecInfoSessionRepository secInfoSessionRepository;

    @Autowired
    private Encryption encryption;

    @Autowired
    private BillCycleService billCycleService;

    @Autowired
    private CentralizedAuthService centralizedAuthService;

    @Autowired
    @Lazy
    private UserAreaPermissionService userAreaPermissionService;

    // Session timeout in hours (24 hours)
    private static final int DEFAULT_SESSION_TIMEOUT_HOURS = 24;

    /**
     * Authenticate user via Centralized HR/AD authentication first, then evaluate NCRE access authorization via SEC_INFO.
     */
    @Transactional
    public SecInfoLoginDTO.LoginResponse authenticateUser(SecInfoLoginDTO.LoginRequest loginRequest, 
                                                        String ipAddress, String userAgent) {
        try {
            // Validate input
            if (loginRequest.getUserId() == null || loginRequest.getUserId().trim().isEmpty()) {
                return createLoginResponse(false, false, false, "EPF Number is required", null, null, null, null);
            }
            
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                return createLoginResponse(false, false, false, "Password is required", null, null, null, null);
            }

            String epfNumber = loginRequest.getUserId().trim();
            String loginType = loginRequest.getLoginType();

            if (loginType == null || loginType.trim().isEmpty()) {
                return createLoginResponse(false, false, false, "Please select a login type (HR Login or AD Login)", null, null, null, null);
            }

            // Step 1: Perform Centralized Company Authentication (HR or AD)
            CentralizedAuthService.AuthResult authResult = centralizedAuthService.authenticate(epfNumber, loginRequest.getPassword(), loginType);

            if (!authResult.isAuthenticated()) {
                // Company authentication failed
                return createLoginResponse(false, false, false, authResult.getMessage(), null, null, null, null);
            }

            // Step 2: Centralized Authentication SUCCESS -> Evaluate NCRE Authorization in SEC_INFO table by EPF Number (epf_num column)
            Optional<UserAccSecInfo> userOptional = userAccSecInfoRepository.findByEpfNumTrimmed(epfNumber);

            if (!userOptional.isPresent()) {
                // Fallback check by USER_ID in case USER_ID equals EPF Number
                userOptional = userAccSecInfoRepository.findByIdTrimmed(epfNumber);
            }

            if (!userOptional.isPresent()) {
                // Centralized Auth passed, but no SEC_INFO record exists for this EPF Number
                return createLoginResponse(true, true, false, "Authentication successful, but no NCRE access is configured.", null, null, null, null);
            }

            UserAccSecInfo user = userOptional.get();

            // Check if user account is active in NCRE SEC_INFO
            if (user.getStatus() == null || user.getStatus() != 1) {
                return createLoginResponse(true, true, false, "Authentication successful, but your NCRE user account is inactive.", null, null, null, null);
            }

            // Step 3: NCRE Authorization Granted -> Create user session & load permissions
            Optional<SecInfoSessionData> existingSession = secInfoSessionRepository.findByUserId(user.getUserId());
            if (existingSession.isPresent()) {
                // Invalidate existing session
                secInfoSessionRepository.delete(existingSession.get());
            }

            // Create new session
            String sessionId = generateSessionId();
            LocalDateTime loginTime = LocalDateTime.now();
            Long timeToLive = DEFAULT_SESSION_TIMEOUT_HOURS * 3600L; // 24 hours in seconds

            // Resolve the areas this user may read from the sec_info code hierarchy.
            // No area is chosen here - the client loads data for all of them by default.
            List<String> permittedAreaCodes = userAreaPermissionService
                    .resolvePermittedAreas(user.getRegionCode(), user.getProvinceCode(), user.getAreaCode())
                    .stream()
                    .map(HsbAreaDTO::getAreaCode)
                    .map(UserAreaPermissionService::normalizeCode)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            SecInfoSessionData sessionData = new SecInfoSessionData(
                sessionId,
                user.getUserId(),
                user.getUserName(),
                user.getUserCat(),
                user.getRegionCode(),
                user.getProvinceCode(),
                user.getAreaCode(),
                loginTime,
                loginTime,
                ipAddress,
                userAgent
            );
            sessionData.setTimeToLive(timeToLive);
            sessionData.setPermittedAreaCodes(permittedAreaCodes);
            // NULL selection = load every permitted area until the user narrows it down.
            sessionData.setSelectedAreaCode(null);

            // Save session to Redis
            secInfoSessionRepository.save(sessionData);

            // Create user info carrying the permitted areas for the client
            SecInfoLoginDTO.UserInfo userInfo = createUserInfoWithLocationCodes(user);
            userInfo.setAccessScope(userAreaPermissionService.describeScope(
                    user.getRegionCode(), user.getProvinceCode(), user.getAreaCode()));
            userInfo.setPermittedAreas(billCycleService.getBillCyclesForAreaCodes(permittedAreaCodes));
            userInfo.setSelectedAreaCode(null);

            LocalDateTime expiresAt = loginTime.plusSeconds(timeToLive);

            return createLoginResponse(true, true, true, "Login successful", sessionId, userInfo, loginTime, expiresAt);

        } catch (Exception e) {
            return createLoginResponse(false, false, false, "Authentication failed: " + e.getMessage(), null, null, null, null);
        }
    }

    /**
     * Logout user and invalidate session
     */
    @Transactional
    public SecInfoLoginDTO.LogoutResponse logoutUser(SecInfoLoginDTO.LogoutRequest logoutRequest) {
        try {
            // Validate input
            if (logoutRequest.getSessionId() == null || logoutRequest.getSessionId().trim().isEmpty()) {
                return new SecInfoLoginDTO.LogoutResponse(false, "Session ID is required", LocalDateTime.now());
            }

            // Find and delete session
            Optional<SecInfoSessionData> sessionOptional = secInfoSessionRepository.findById(logoutRequest.getSessionId());
            if (!sessionOptional.isPresent()) {
                return new SecInfoLoginDTO.LogoutResponse(false, "Invalid session", LocalDateTime.now());
            }

            SecInfoSessionData session = sessionOptional.get();
            
            // Verify user ID if provided
            if (logoutRequest.getUserId() != null && !logoutRequest.getUserId().equals(session.getUserId())) {
                return new SecInfoLoginDTO.LogoutResponse(false, "Session does not belong to this user", LocalDateTime.now());
            }

            // Delete session
            secInfoSessionRepository.delete(session);

            return new SecInfoLoginDTO.LogoutResponse(true, "Logout successful", LocalDateTime.now());

        } catch (Exception e) {
            return new SecInfoLoginDTO.LogoutResponse(false, "Logout failed: " + e.getMessage(), LocalDateTime.now());
        }
    }

    /**
     * Validate session and update last access time - includes location codes
     * Added check, to ensure user is still active even after login
     */
    @Transactional
    public SecInfoLoginDTO.SessionValidationResponse validateSession(SecInfoLoginDTO.SessionValidationRequest request) {
        try {
            if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
                return new SecInfoLoginDTO.SessionValidationResponse(false, "Session ID is required", null, null, null);
            }

            Optional<SecInfoSessionData> sessionOptional = secInfoSessionRepository.findById(request.getSessionId());
            if (!sessionOptional.isPresent()) {
                return new SecInfoLoginDTO.SessionValidationResponse(false, "Invalid or expired session", null, null, null);
            }

            SecInfoSessionData session = sessionOptional.get();

            // Verify user ID if provided
            if (request.getUserId() != null && !request.getUserId().equals(session.getUserId())) {
                return new SecInfoLoginDTO.SessionValidationResponse(false, "Session does not belong to this user", null, null, null);
            }

            // CRITICAL: Check if user is still active in the database
            // This handles cases where user was deactivated after login
            Optional<UserAccSecInfo> userOptional = userAccSecInfoRepository.findById(session.getUserId());
            if (!userOptional.isPresent()) {
                // User no longer exists - invalidate session
                secInfoSessionRepository.delete(session);
                return new SecInfoLoginDTO.SessionValidationResponse(false, "User account no longer exists", null, null, null);
            }

            UserAccSecInfo user = userOptional.get();
            if (user.getStatus() == null || user.getStatus() != 1) {
                // User is no longer active - invalidate session immediately
                secInfoSessionRepository.delete(session);
                return new SecInfoLoginDTO.SessionValidationResponse(false, "User account is no longer active", null, null, null);
            }

            // Update last access time
            session.setLastAccessTime(LocalDateTime.now());
            secInfoSessionRepository.save(session);

            // Create user info with location codes from session
            SecInfoLoginDTO.UserInfo userInfo = createUserInfoFromSession(session);

            LocalDateTime expiresAt = session.getLoginTime().plusSeconds(session.getTimeToLive());

            return new SecInfoLoginDTO.SessionValidationResponse(
                true, 
                "Session is valid", 
                userInfo, 
                session.getLastAccessTime(), 
                expiresAt
            );

        } catch (Exception e) {
            return new SecInfoLoginDTO.SessionValidationResponse(false, "Session validation failed: " + e.getMessage(), null, null, null);
        }
    }

    /**
     * Helper method to create UserInfo from the sec_info record.
     * The codes are passed through untouched: a NULL code is meaningful and means
     * "all of the level below", so it must not be blanked out based on user_cat.
     */
    private SecInfoLoginDTO.UserInfo createUserInfoWithLocationCodes(UserAccSecInfo user) {
        SecInfoLoginDTO.UserInfo userInfo = new SecInfoLoginDTO.UserInfo();
        userInfo.setUserId(user.getUserId());
        userInfo.setUserName(user.getUserName());
        userInfo.setUserCategory(user.getUserCat());
        userInfo.setRegionCode(UserAreaPermissionService.normalizeCode(user.getRegionCode()));
        userInfo.setProvinceCode(UserAreaPermissionService.normalizeCode(user.getProvinceCode()));
        userInfo.setAreaCode(UserAreaPermissionService.normalizeCode(user.getAreaCode()));
        return userInfo;
    }

    /**
     * Helper method to create UserInfo from session data.
     * Permitted areas are read from the cached session value when present, otherwise
     * they are resolved from the stored codes.
     */
    private SecInfoLoginDTO.UserInfo createUserInfoFromSession(SecInfoSessionData session) {
        SecInfoLoginDTO.UserInfo userInfo = new SecInfoLoginDTO.UserInfo();
        userInfo.setUserId(session.getUserId());
        userInfo.setUserName(session.getUserName());
        userInfo.setUserCategory(session.getUserCategory());
        userInfo.setRegionCode(UserAreaPermissionService.normalizeCode(session.getRegionCode()));
        userInfo.setProvinceCode(UserAreaPermissionService.normalizeCode(session.getProvinceCode()));
        userInfo.setAreaCode(UserAreaPermissionService.normalizeCode(session.getAreaCode()));
        userInfo.setAccessScope(userAreaPermissionService.describeScope(
                session.getRegionCode(), session.getProvinceCode(), session.getAreaCode()));

        List<String> permittedCodes = resolveSessionPermittedAreaCodes(session);
        userInfo.setPermittedAreas(billCycleService.getBillCyclesForAreaCodes(permittedCodes));
        userInfo.setSelectedAreaCode(UserAreaPermissionService.normalizeCode(session.getSelectedAreaCode()));

        return userInfo;
    }

    /**
     * Uses the codes cached at login when available so repeated session validation does
     * not re-query the location tables on every request.
     */
    private List<String> resolveSessionPermittedAreaCodes(SecInfoSessionData session) {
        List<String> cached = session.getPermittedAreaCodes();
        if (cached != null) {
            return cached;
        }
        List<String> resolved = userAreaPermissionService
                .resolvePermittedAreas(session.getRegionCode(), session.getProvinceCode(), session.getAreaCode())
                .stream()
                .map(HsbAreaDTO::getAreaCode)
                .map(UserAreaPermissionService::normalizeCode)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        try {
            session.setPermittedAreaCodes(resolved);
            secInfoSessionRepository.save(session);
        } catch (Exception ignored) {
            // Caching is an optimisation only; never fail the request because of it.
        }
        return resolved;
    }

    /**
     * Narrow the active session to a single permitted area.
     * Passing a blank / "ALL" area code restores the default of loading every permitted area.
     */
    @Transactional
    public Map<String, Object> selectArea(SecInfoLoginDTO.SelectAreaRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (request == null || request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Session ID is required");
                return result;
            }

            Optional<SecInfoSessionData> sessionOptional = secInfoSessionRepository.findById(request.getSessionId().trim());
            if (sessionOptional.isEmpty()) {
                result.put("success", false);
                result.put("message", "Invalid or expired session");
                return result;
            }

            SecInfoSessionData session = sessionOptional.get();
            if (request.getUserId() != null && !request.getUserId().trim().isEmpty()
                    && !request.getUserId().trim().equals(session.getUserId())) {
                result.put("success", false);
                result.put("message", "Session does not belong to this user");
                return result;
            }

            List<String> permittedCodes = resolveSessionPermittedAreaCodes(session);
            String requested = UserAreaPermissionService.normalizeCode(request.getAreaCode());

            if (requested == null || UserAreaPermissionService.ALL_AREAS.equalsIgnoreCase(requested)) {
                session.setSelectedAreaCode(null);
                secInfoSessionRepository.save(session);
                result.put("success", true);
                result.put("message", "Showing all permitted areas");
                result.put("selected_area_code", null);
                result.put("scope", "ALL_PERMITTED");
                result.put("permitted_areas", billCycleService.getBillCyclesForAreaCodes(permittedCodes));
                result.put("effective_area_count", permittedCodes.size());
                return result;
            }

            String normalizedRequested = UserAreaPermissionService.normalizeAreaCode(requested);
            String matched = permittedCodes.stream()
                    .filter(code -> normalizedRequested.equals(UserAreaPermissionService.normalizeAreaCode(code)))
                    .findFirst()
                    .orElse(null);

            if (matched == null) {
                result.put("success", false);
                result.put("message", "Access denied to area: " + requested);
                return result;
            }

            session.setSelectedAreaCode(matched);
            secInfoSessionRepository.save(session);

            result.put("success", true);
            result.put("message", "Area selection updated");
            result.put("selected_area_code", matched);
            result.put("scope", "SINGLE_AREA");
            result.put("permitted_areas", billCycleService.getBillCyclesForAreaCodes(permittedCodes));
            result.put("effective_area_count", 1);
            return result;

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to update area selection: " + e.getMessage());
            return result;
        }
    }

    /**
     * Get session data by session ID (for accessing location codes globally)
     */
    @Transactional(readOnly = true)
    public Optional<SecInfoSessionData> getSessionData(String sessionId) {
        try {
            return secInfoSessionRepository.findById(sessionId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Get user's location codes from active session
     */
    @Transactional(readOnly = true)
    public Optional<SecInfoLoginDTO.UserInfo> getUserLocationFromSession(String sessionId, String userId) {
        try {
            Optional<SecInfoSessionData> sessionOptional = secInfoSessionRepository.findById(sessionId);
            if (sessionOptional.isPresent()) {
                SecInfoSessionData session = sessionOptional.get();
                if (userId == null || userId.equals(session.getUserId())) {
                    return Optional.of(createUserInfoFromSession(session));
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // Helper methods
    private String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private SecInfoLoginDTO.LoginResponse createLoginResponse(Boolean success, Boolean authenticated, Boolean hasNcreAccess, String message, 
                                                            String sessionId, SecInfoLoginDTO.UserInfo userInfo, 
                                                            LocalDateTime loginTime, LocalDateTime expiresAt) {
        return new SecInfoLoginDTO.LoginResponse(success, authenticated, hasNcreAccess, message, sessionId, userInfo, loginTime, expiresAt);
    }
}