package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.PermissionDTO;
import com.example.SPSProjectBackend.model.NcreFuncm;
import com.example.SPSProjectBackend.model.SecInfoSessionData;
import com.example.SPSProjectBackend.repository.NcreRoleFuncRepository;
import com.example.SPSProjectBackend.repository.SecInfoSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class NcreAuthorizationService {

    public static final String DEFAULT_APPL_ID = "NCR";

    @Autowired
    private NcreRoleFuncRepository ncreRoleFuncRepository;

    @Autowired
    private SecInfoSessionRepository secInfoSessionRepository;

    /**
     * Kept for API compatibility. Permissions are read from the database for
     * every request, so there is no application cache to invalidate.
     */
    public void invalidateCache(String userId, String applId) {
    }

    /**
     * Retrieve active joined permissions for a user and application ID.
     * Fails closed by returning an empty list on error or null inputs.
     */
    /**
     * Retrieve active joined permissions for a user and application ID.
     * Fails closed by returning an empty list on error or null inputs.
     */
    public List<NcreFuncm> getUserActivePermissions(String userId, String applId) {
        if (userId == null || userId.trim().isEmpty()) {
            System.out.println("[NcreAuthorizationService] getUserActivePermissions rejected null/empty userId");
            return Collections.emptyList();
        }

        String cleanedUserId = userId.trim();
        String effectiveApplId = (applId != null && !applId.trim().isEmpty()) ? applId.trim() : DEFAULT_APPL_ID;
        try {
            List<NcreFuncm> activePerms = ncreRoleFuncRepository.findActiveUserPermissions(cleanedUserId, effectiveApplId);
            return activePerms != null ? activePerms : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error fetching active permissions for user " + cleanedUserId + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Build nested sidebar function hierarchy for frontend menu rendering.
     */
    public PermissionDTO.UserPermissionsResponseDTO getSidebarFunctions(String userId, String applId) {
        String effectiveApplId = (applId != null && !applId.trim().isEmpty()) ? applId.trim() : DEFAULT_APPL_ID;
        List<NcreFuncm> activePerms = getUserActivePermissions(userId, effectiveApplId);

        List<PermissionDTO.UserPermissionItemDTO> rawItems = new ArrayList<>();
        Map<String, PermissionDTO.SidebarFunctionDTO> sidebarMap = new LinkedHashMap<>();

        for (NcreFuncm f : activePerms) {
            String funcId = f.getFuncId() != null ? f.getFuncId().trim() : "";
            String funcNm = f.getFuncNm() != null ? f.getFuncNm().trim() : funcId;
            String subFuncId = f.getSubFuncId() != null ? f.getSubFuncId().trim() : "";
            String subFuncNm = f.getSubFuncNm() != null ? f.getSubFuncNm().trim() : subFuncId;

            rawItems.add(new PermissionDTO.UserPermissionItemDTO(
                    funcId,
                    f.getApplId() != null ? f.getApplId().trim() : effectiveApplId,
                    funcNm,
                    subFuncId,
                    subFuncNm,
                    f.getSeqNo()
            ));

            PermissionDTO.SidebarFunctionDTO parent = sidebarMap.computeIfAbsent(funcId, k ->
                    new PermissionDTO.SidebarFunctionDTO(funcId, funcNm, f.getSeqNo(), new ArrayList<>())
            );

            // Add sub-function if not already present
            boolean exists = parent.getSubFunctions().stream()
                    .anyMatch(sub -> sub.getSubFuncId() != null && sub.getSubFuncId().equalsIgnoreCase(subFuncId));
            if (!exists) {
                parent.getSubFunctions().add(new PermissionDTO.SubFunctionDTO(subFuncId, subFuncNm, null));
            }
        }

        List<PermissionDTO.SidebarFunctionDTO> sidebarList = new ArrayList<>(sidebarMap.values());
        sidebarList.sort((a, b) -> {
            if (a.getSeqNo() != null && b.getSeqNo() != null) {
                return a.getSeqNo().compareTo(b.getSeqNo());
            }
            return a.getFuncId().compareTo(b.getFuncId());
        });

        // Debug log BM subfunctions
        sidebarList.stream()
                .filter(sf -> "BM".equalsIgnoreCase(sf.getFuncId()))
                .findFirst()
                .ifPresent(bm -> System.out.println("[NcreAuthorizationService] BM SubFunctions for " + userId + ": " 
                        + bm.getSubFunctions().stream().map(PermissionDTO.SubFunctionDTO::getSubFuncId).toList()));

        return new PermissionDTO.UserPermissionsResponseDTO(userId, effectiveApplId, rawItems, sidebarList);
    }

    /**
     * Check if a user has active access to a specific function and sub-function.
     */
    public boolean hasAccess(String userId, String funcId, String subFuncId, String applId) {
        if (userId == null || funcId == null || subFuncId == null) {
            System.out.println("[NcreAuthorizationService.hasAccess] Rejected null args: userId=" + userId + ", funcId=" + funcId + ", subFuncId=" + subFuncId);
            return false;
        }

        String cleanedUserId = userId.trim();
        String cleanedFuncId = funcId.trim();
        String cleanedSubFuncId = subFuncId.trim();
        String effectiveApplId = (applId != null && !applId.trim().isEmpty()) ? applId.trim() : DEFAULT_APPL_ID;

        // Always use the current database result so revoked permissions take effect immediately.
        List<NcreFuncm> activePerms = getUserActivePermissions(cleanedUserId, effectiveApplId);
        if (!activePerms.isEmpty()) {
            boolean hasMatch = activePerms.stream().anyMatch(f ->
                    f.getFuncId() != null && f.getFuncId().trim().equalsIgnoreCase(cleanedFuncId) &&
                    f.getSubFuncId() != null && f.getSubFuncId().trim().equalsIgnoreCase(cleanedSubFuncId) &&
                    f.getApplId() != null && f.getApplId().trim().equalsIgnoreCase(effectiveApplId) &&
                    "2".equals(f.getStatus() != null ? f.getStatus().trim() : "")
            );
            if (hasMatch) {
                return true;
            }
        }

        // Direct DB count check fallback
        try {
            long count = ncreRoleFuncRepository.countActiveUserPermission(
                    cleanedUserId,
                    effectiveApplId,
                    cleanedFuncId,
                    cleanedSubFuncId
            );
            System.out.println("[NcreAuthorizationService.hasAccess] Direct DB count query for user='" + cleanedUserId + "', funcId='" + cleanedFuncId + "', subFuncId='" + cleanedSubFuncId + "': count=" + count);
            return count > 0;
        } catch (Exception e) {
            System.err.println("Error verifying access for user " + cleanedUserId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate session ID and extract authenticated user_id from session store.
     */
    public Optional<String> getUserIdFromSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            Optional<SecInfoSessionData> sessionOpt = secInfoSessionRepository.findById(sessionId.trim());
            return sessionOpt.map(SecInfoSessionData::getUserId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
