package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.FunctionManagementDTO;
import com.example.SPSProjectBackend.model.NcreFuncm;
import com.example.SPSProjectBackend.service.FunctionManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/function-management")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class FunctionManagementController {

    private final FunctionManagementService functionManagementService;

    public FunctionManagementController(FunctionManagementService functionManagementService) {
        this.functionManagementService = functionManagementService;
    }

    @GetMapping("/functions")
    public ResponseEntity<?> getFunctions(@RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        return respond(() -> functionManagementService.getFunctions(sessionId));
    }

    @PostMapping("/functions")
    public ResponseEntity<?> createFunction(@RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                                            @RequestBody NcreFuncm function) {
        return respond(() -> functionManagementService.createFunction(sessionId, function), HttpStatus.CREATED);
    }

    @PutMapping("/functions/{applId}/{funcId}/{subFuncId}")
    public ResponseEntity<?> updateFunction(@RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                                            @PathVariable String applId,
                                            @PathVariable String funcId,
                                            @PathVariable String subFuncId,
                                            @RequestBody NcreFuncm function) {
        return respond(() -> functionManagementService.updateFunction(sessionId, applId, funcId, subFuncId, function));
    }

    @GetMapping("/users/{userId}/permissions")
    public ResponseEntity<?> getUserPermissions(@RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                                                @PathVariable String userId,
                                                @RequestParam(defaultValue = "NCR") String applId) {
        return respond(() -> functionManagementService.getUserPermissions(sessionId, userId, applId));
    }

    @PutMapping("/users/{userId}/permissions")
    public ResponseEntity<?> updateUserPermissions(@RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                                                   @PathVariable String userId,
                                                   @RequestBody FunctionManagementDTO.PermissionUpdateDTO request) {
        return respond(() -> functionManagementService.updateUserPermissions(sessionId, userId, request));
    }

    private ResponseEntity<?> respond(ControllerAction action) {
        return respond(action, HttpStatus.OK);
    }

    private ResponseEntity<?> respond(ControllerAction action, HttpStatus successStatus) {
        try {
            return ResponseEntity.status(successStatus).body(action.run());
        } catch (SecurityException e) {
            return error(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (java.util.NoSuchElementException e) {
            return error(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Function management request failed");
        }
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    @FunctionalInterface
    private interface ControllerAction {
        Object run();
    }
}