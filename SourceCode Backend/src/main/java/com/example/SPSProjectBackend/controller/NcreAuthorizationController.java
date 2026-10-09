package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.PermissionDTO;
import com.example.SPSProjectBackend.service.NcreAuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class NcreAuthorizationController {

    @Autowired
    private NcreAuthorizationService authorizationService;

    /**
     * Get active sidebar functions and permissions for the authenticated session user.
     */
    @GetMapping(value = "/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPermissions(
            @RequestHeader(value = "X-Session-Id", required = false) String headerSessionId,
            @RequestParam(value = "session_id", required = false) String querySessionId,
            @RequestParam(value = "appl_id", required = false, defaultValue = NcreAuthorizationService.DEFAULT_APPL_ID) String applId,
            HttpServletRequest request) {

        String sessionId = headerSessionId != null ? headerSessionId : querySessionId;
        if (sessionId == null || sessionId.trim().isEmpty()) {
            // Try request attribute set by auth filter if available
            sessionId = (String) request.getAttribute("session_id");
        }

        if (sessionId == null || sessionId.trim().isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Session ID is required in X-Session-Id header or session_id query param.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        Optional<String> userIdOpt = authorizationService.getUserIdFromSession(sessionId);
        if (!userIdOpt.isPresent()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Invalid or expired session context.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        String userId = userIdOpt.get();
        PermissionDTO.UserPermissionsResponseDTO permissionsResponse =
                authorizationService.getSidebarFunctions(userId, applId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", permissionsResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * POST endpoint for checking permission access for a func_id and sub_func_id.
     */
    @PostMapping(value = "/check-access", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PermissionDTO.CheckAccessResponseDTO> checkAccess(
            @RequestHeader(value = "X-Session-Id", required = false) String headerSessionId,
            @RequestBody PermissionDTO.CheckAccessRequestDTO requestDTO) {

        String sessionId = headerSessionId;
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new PermissionDTO.CheckAccessResponseDTO(false, null, null, null, null, "Session ID is required."));
        }

        Optional<String> userIdOpt = authorizationService.getUserIdFromSession(sessionId);
        if (!userIdOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new PermissionDTO.CheckAccessResponseDTO(false, null, null, null, null, "Invalid or expired session."));
        }

        String userId = userIdOpt.get();
        String applId = requestDTO.getApplId() != null ? requestDTO.getApplId() : NcreAuthorizationService.DEFAULT_APPL_ID;

        boolean hasAccess = authorizationService.hasAccess(
                userId,
                requestDTO.getFuncId(),
                requestDTO.getSubFuncId(),
                applId
        );

        String message = hasAccess ? "Access granted." : "Access denied for requested function.";
        return ResponseEntity.ok(new PermissionDTO.CheckAccessResponseDTO(
                hasAccess,
                userId,
                applId,
                requestDTO.getFuncId(),
                requestDTO.getSubFuncId(),
                message
        ));
    }

    /**
     * Invalidate permission cache for a user or globally.
     */
    @PostMapping(value = "/invalidate-cache", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> invalidateCache(
            @RequestParam(value = "user_id", required = false) String userId,
            @RequestParam(value = "appl_id", required = false, defaultValue = NcreAuthorizationService.DEFAULT_APPL_ID) String applId) {

        authorizationService.invalidateCache(userId, applId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Permission cache invalidated successfully.");
        return ResponseEntity.ok(resp);
    }
}
