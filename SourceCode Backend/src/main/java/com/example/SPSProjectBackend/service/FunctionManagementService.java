package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.FunctionManagementDTO;
import com.example.SPSProjectBackend.model.NcreFuncm;
import com.example.SPSProjectBackend.model.NcreFuncmId;
import com.example.SPSProjectBackend.model.NcreRoleFunc;
import com.example.SPSProjectBackend.model.NcreRoleFuncId;
import com.example.SPSProjectBackend.model.SecInfoSessionData;
import com.example.SPSProjectBackend.model.UserAccSecInfo;
import com.example.SPSProjectBackend.repository.NcreFuncmRepository;
import com.example.SPSProjectBackend.repository.NcreRoleFuncRepository;
import com.example.SPSProjectBackend.repository.SecInfoSessionRepository;
import com.example.SPSProjectBackend.repository.UserAccSecInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FunctionManagementService {

    private static final String ACTIVE_STATUS = "2";
    private static final String INACTIVE_STATUS = "1";
    private static final int USER_ID_MAX_LENGTH = 10;

    private final NcreFuncmRepository functionRepository;
    private final NcreRoleFuncRepository roleFunctionRepository;
    private final SecInfoSessionRepository sessionRepository;
    private final UserAccSecInfoRepository userRepository;

    public FunctionManagementService(NcreFuncmRepository functionRepository,
                                     NcreRoleFuncRepository roleFunctionRepository,
                                     SecInfoSessionRepository sessionRepository,
                                     UserAccSecInfoRepository userRepository) {
        this.functionRepository = functionRepository;
        this.roleFunctionRepository = roleFunctionRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<NcreFuncm> getFunctions(String sessionId) {
        requireAdministrator(sessionId);
        return functionRepository.findAllByOrderByApplIdAscSeqNoAscFuncIdAscSubFuncIdAsc();
    }

    public NcreFuncm createFunction(String sessionId, NcreFuncm function) {
        UserAccSecInfo actor = requireAdministrator(sessionId);
        validateFunction(function);
        NcreFuncmId id = new NcreFuncmId(function.getFuncId(), function.getApplId(), function.getSubFuncId());
        if (functionRepository.existsById(id)) {
            throw new IllegalArgumentException("A function with this application, function ID, and sub-function ID already exists");
        }

        function.setStatus(normalizeStatus(function.getStatus()));
        function.setEntBy(actor.getEpfNum());
        function.setEntDt(Date.valueOf(LocalDate.now()));
        function.setModiBy(null);
        function.setModiDt(null);
        return functionRepository.save(function);
    }

    public NcreFuncm updateFunction(String sessionId, String applId, String funcId,
                                    String subFuncId, NcreFuncm updates) {
        UserAccSecInfo actor = requireAdministrator(sessionId);
        NcreFuncmId id = new NcreFuncmId(clean(funcId), clean(applId), clean(subFuncId));
        NcreFuncm existing = functionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Function was not found"));

        if (updates.getFuncNm() == null || updates.getFuncNm().trim().isEmpty()
                || updates.getSubFuncNm() == null || updates.getSubFuncNm().trim().isEmpty()) {
            throw new IllegalArgumentException("Function and sub-function names are required");
        }
        existing.setFuncNm(updates.getFuncNm());
        existing.setSubFuncNm(updates.getSubFuncNm());
        if (updates.getSeqNo() != null) {
            existing.setSeqNo(updates.getSeqNo());
        }
        if (updates.getStatus() != null) {
            existing.setStatus(normalizeStatus(updates.getStatus()));
        }
        existing.setModiBy(actor.getEpfNum());
        existing.setModiDt(Date.valueOf(LocalDate.now()));
        return functionRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public FunctionManagementDTO.PermissionMatrixDTO getUserPermissions(
            String sessionId, String userId, String applId) {
        requireAdministrator(sessionId);
        String cleanedUserId = required(userId, "User ID", USER_ID_MAX_LENGTH);
        String cleanedApplId = required(applId, "Application ID", 3);
        if (!userRepository.findByIdTrimmed(cleanedUserId).isPresent()) {
            throw new NoSuchElementException("User was not found");
        }

        Map<String, NcreRoleFunc> grants = roleFunctionRepository
                .findByTrimmedUserIdAndTrimmedApplId(cleanedUserId, cleanedApplId).stream()
                .collect(Collectors.toMap(
                        grant -> permissionKey(grant.getFuncId(), grant.getSubFuncId()),
                        grant -> grant,
                        (first, duplicate) -> first));

        List<FunctionManagementDTO.PermissionItemDTO> items = functionRepository
                .findByApplIdOrderBySeqNoAscFuncIdAscSubFuncIdAsc(cleanedApplId).stream()
                .map(function -> {
                    FunctionManagementDTO.PermissionItemDTO item = new FunctionManagementDTO.PermissionItemDTO();
                    item.setFunction(function);
                    NcreRoleFunc grant = grants.get(permissionKey(function.getFuncId(), function.getSubFuncId()));
                        item.setGranted(ACTIVE_STATUS.equals(clean(function.getStatus()))
                            && grant != null && ACTIVE_STATUS.equals(clean(grant.getStatus())));
                    return item;
                }).collect(Collectors.toList());

        FunctionManagementDTO.PermissionMatrixDTO response = new FunctionManagementDTO.PermissionMatrixDTO();
        response.setUserId(cleanedUserId);
        response.setApplId(cleanedApplId);
        response.setPermissions(items);
        return response;
    }

    public FunctionManagementDTO.PermissionMatrixDTO updateUserPermissions(
            String sessionId, String userId, FunctionManagementDTO.PermissionUpdateDTO request) {
        UserAccSecInfo actor = requireAdministrator(sessionId);
        String cleanedUserId = required(userId, "User ID", USER_ID_MAX_LENGTH);
        String cleanedApplId = required(request.getApplId(), "Application ID", 3);
        if (!userRepository.findByIdTrimmed(cleanedUserId).isPresent()) {
            throw new NoSuchElementException("User was not found");
        }

        Set<String> selectedKeys = Optional.ofNullable(request.getPermissions()).orElse(Collections.emptyList())
                .stream()
                .map(key -> permissionKey(required(key.getFuncId(), "Function ID", 8),
                        required(key.getSubFuncId(), "Sub-function ID", 8)))
                .collect(Collectors.toSet());

        List<NcreFuncm> catalog = functionRepository.findByApplIdOrderBySeqNoAscFuncIdAscSubFuncIdAsc(cleanedApplId);
        Set<String> grantableKeys = catalog.stream()
                .filter(function -> ACTIVE_STATUS.equals(clean(function.getStatus())))
                .map(function -> permissionKey(function.getFuncId(), function.getSubFuncId()))
                .collect(Collectors.toSet());
        if (!grantableKeys.containsAll(selectedKeys)) {
            throw new IllegalArgumentException("Permissions may only be granted for active functions in the selected application");
        }

        Map<String, NcreRoleFunc> existingGrants = roleFunctionRepository
                .findByTrimmedUserIdAndTrimmedApplId(cleanedUserId, cleanedApplId).stream()
                .collect(Collectors.toMap(
                        grant -> permissionKey(grant.getFuncId(), grant.getSubFuncId()),
                        grant -> grant,
                        (first, duplicate) -> first));

        Date today = Date.valueOf(LocalDate.now());
        List<NcreRoleFunc> changedGrants = new ArrayList<>();
        for (NcreFuncm function : catalog) {
            String key = permissionKey(function.getFuncId(), function.getSubFuncId());
            boolean shouldBeActive = selectedKeys.contains(key);
            NcreRoleFunc grant = existingGrants.get(key);
            if (grant == null && shouldBeActive) {
                grant = new NcreRoleFunc();
                grant.setUserId(cleanedUserId);
                grant.setFuncId(clean(function.getFuncId()));
                grant.setApplId(clean(cleanedApplId));
                grant.setSubFuncId(clean(function.getSubFuncId()));
                grant.setStatus(ACTIVE_STATUS);
                grant.setEntBy(actor.getEpfNum());
                grant.setEntDt(today);
                changedGrants.add(grant);
            } else if (grant != null) {
                String nextStatus = shouldBeActive ? ACTIVE_STATUS : INACTIVE_STATUS;
                if (!nextStatus.equals(clean(grant.getStatus()))) {
                    grant.setStatus(nextStatus);
                    grant.setModiBy(actor.getEpfNum());
                    grant.setModiDt(today);
                    changedGrants.add(grant);
                }
            }
        }
        roleFunctionRepository.saveAll(changedGrants);
        return getUserPermissions(sessionId, cleanedUserId, cleanedApplId);
    }

    private UserAccSecInfo requireAdministrator(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new SecurityException("A valid login session is required");
        }
        SecInfoSessionData session = sessionRepository.findById(sessionId.trim())
                .orElseThrow(() -> new SecurityException("The login session is invalid or expired"));
        UserAccSecInfo actor = userRepository.findActiveUserByIdTrimmed(session.getUserId())
                .orElseThrow(() -> new SecurityException("The signed-in user is not active"));
        if (actor.getUserCat() == null || !"Admin".equalsIgnoreCase(actor.getUserCat().trim())) {
            throw new SecurityException("Only an active Admin can manage functions and user permissions");
        }
        if (actor.getEpfNum() == null || actor.getEpfNum().trim().isEmpty()) {
            throw new SecurityException("The signed-in Admin must have an EPF number for audit records");
        }
        return actor;
    }

    private void validateFunction(NcreFuncm function) {
        required(function.getFuncId(), "Function ID", 8);
        required(function.getApplId(), "Application ID", 3);
        required(function.getSubFuncId(), "Sub-function ID", 8);
        if (function.getFuncNm() == null || function.getFuncNm().trim().isEmpty() || function.getFuncNm().trim().length() > 60) {
            throw new IllegalArgumentException("Function name is required and must not exceed 60 characters");
        }
        if (function.getSubFuncNm() == null || function.getSubFuncNm().trim().isEmpty() || function.getSubFuncNm().trim().length() > 60) {
            throw new IllegalArgumentException("Sub-function name is required and must not exceed 60 characters");
        }
        if (function.getSeqNo() == null) {
            function.setSeqNo(BigDecimal.ZERO);
        }
        function.setFuncId(clean(function.getFuncId()));
        function.setApplId(clean(function.getApplId()));
        function.setSubFuncId(clean(function.getSubFuncId()));
        function.setFuncNm(clean(function.getFuncNm()));
        function.setSubFuncNm(clean(function.getSubFuncNm()));
    }

    private String normalizeStatus(String status) {
        String cleaned = clean(status);
        if (cleaned == null || cleaned.isEmpty()) return ACTIVE_STATUS;
        if (!ACTIVE_STATUS.equals(cleaned) && !INACTIVE_STATUS.equals(cleaned)) {
            throw new IllegalArgumentException("Status must be 2 (active) or 1 (inactive)");
        }
        return cleaned;
    }

    private String required(String value, String label, int maxLength) {
        String cleaned = clean(value);
        if (cleaned == null || cleaned.isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        if (cleaned.length() > maxLength) {
            throw new IllegalArgumentException(label + " must not exceed " + maxLength + " characters");
        }
        return cleaned;
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private String permissionKey(String funcId, String subFuncId) {
        return clean(funcId) + "\u0000" + clean(subFuncId);
    }
}