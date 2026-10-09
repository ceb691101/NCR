package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.PermissionDTO;
import com.example.SPSProjectBackend.model.NcreFuncm;
import com.example.SPSProjectBackend.model.SecInfoSessionData;
import com.example.SPSProjectBackend.repository.NcreRoleFuncRepository;
import com.example.SPSProjectBackend.repository.SecInfoSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NcreAuthorizationServiceTest {

    @Mock
    private NcreRoleFuncRepository ncreRoleFuncRepository;

    @Mock
    private SecInfoSessionRepository secInfoSessionRepository;

    @InjectMocks
    private NcreAuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService.invalidateCache(null, null);
    }

    @Test
    void testScenario1_UserHasActiveAccess() {
        String userId = "U001";
        String applId = "NCR";
        String funcId = "SYSADMIN";
        String subFuncId = "USER_ADM";

        NcreFuncm funcm = new NcreFuncm();
        funcm.setFuncId(funcId);
        funcm.setApplId(applId);
        funcm.setFuncNm("System Admin");
        funcm.setSubFuncId(subFuncId);
        funcm.setSubFuncNm("Administration");
        funcm.setSeqNo(new BigDecimal("1.00"));
        funcm.setStatus("2");

        when(ncreRoleFuncRepository.findActiveUserPermissions(userId, applId))
                .thenReturn(Collections.singletonList(funcm));

        boolean hasAccess = authorizationService.hasAccess(userId, funcId, subFuncId, applId);
        assertTrue(hasAccess, "User should have access when permission status is 2");

        PermissionDTO.UserPermissionsResponseDTO sidebarData = authorizationService.getSidebarFunctions(userId, applId);
        assertNotNull(sidebarData);
        assertEquals(1, sidebarData.getSidebarFunctions().size());
        assertEquals("System Admin", sidebarData.getSidebarFunctions().get(0).getFuncNm());
    }

    @Test
    void testScenario2_UserPermissionDisabled() {
        String userId = "U002";
        String applId = "NCR";
        String funcId = "SYSADMIN";
        String subFuncId = "USER_ADM";

        // Repository query returns empty because rf.status != '2'
        when(ncreRoleFuncRepository.findActiveUserPermissions(userId, applId))
                .thenReturn(Collections.emptyList());
        when(ncreRoleFuncRepository.countActiveUserPermission(userId, applId, funcId, subFuncId))
                .thenReturn(0L);

        boolean hasAccess = authorizationService.hasAccess(userId, funcId, subFuncId, applId);
        assertFalse(hasAccess, "Disabled user permission should deny access");
    }

    @Test
    void testScenario3_FunctionDisabledGlobally() {
        String userId = "U003";
        String applId = "NCR";
        String funcId = "REPORT";
        String subFuncId = "REP_DEV";

        // Function status != '2' -> repo returns empty list
        when(ncreRoleFuncRepository.findActiveUserPermissions(userId, applId))
                .thenReturn(Collections.emptyList());
        when(ncreRoleFuncRepository.countActiveUserPermission(userId, applId, funcId, subFuncId))
                .thenReturn(0L);

        boolean hasAccess = authorizationService.hasAccess(userId, funcId, subFuncId, applId);
        assertFalse(hasAccess, "Globally disabled function must deny access");
    }

    @Test
    void testScenario4_NoPermissionRecord() {
        String userId = "U999";
        String applId = "NCR";

        when(ncreRoleFuncRepository.findActiveUserPermissions(userId, applId))
                .thenReturn(Collections.emptyList());

        List<NcreFuncm> perms = authorizationService.getUserActivePermissions(userId, applId);
        assertTrue(perms.isEmpty(), "No permission record should result in empty perms (fail closed)");
    }

    @Test
    void testScenario5_DifferentApplicationId() {
        String userId = "U001";
        String otherApplId = "OTHER";

        when(ncreRoleFuncRepository.findActiveUserPermissions(userId, otherApplId))
                .thenReturn(Collections.emptyList());

        boolean hasAccess = authorizationService.hasAccess(userId, "SYSADMIN", "USER_ADM", otherApplId);
        assertFalse(hasAccess, "Permissions for another appl_id must not grant NCRE access");
    }

    @Test
    void testGetUserIdFromSession_ValidAndInvalid() {
        String sessionId = "sess123";
        SecInfoSessionData sessionData = new SecInfoSessionData();
        sessionData.setSessionId(sessionId);
        sessionData.setUserId("U001");

        when(secInfoSessionRepository.findById(sessionId)).thenReturn(Optional.of(sessionData));
        when(secInfoSessionRepository.findById("invalid")).thenReturn(Optional.empty());

        Optional<String> userOpt = authorizationService.getUserIdFromSession(sessionId);
        assertTrue(userOpt.isPresent());
        assertEquals("U001", userOpt.get());

        Optional<String> invalidOpt = authorizationService.getUserIdFromSession("invalid");
        assertFalse(invalidOpt.isPresent());
    }
}
