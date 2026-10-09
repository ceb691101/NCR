package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.MdmPlantStatusDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.service.MdmNcreMonitoringService;
import com.example.SPSProjectBackend.service.SecInfoAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/ncre-monitoring")
@CrossOrigin(
    origins = "http://localhost:3000",
    allowCredentials = "true",
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.OPTIONS},
    maxAge = 3600
)
@Slf4j
public class MdmNcreMonitoringController {

    private final MdmNcreMonitoringService monitoringService;
    private final SecInfoAuthService secInfoAuthService;

    @Autowired
    public MdmNcreMonitoringController(MdmNcreMonitoringService monitoringService, SecInfoAuthService secInfoAuthService) {
        this.monitoringService = monitoringService;
        this.secInfoAuthService = secInfoAuthService;
    }

    @GetMapping("/status")
    public ResponseEntity<MdmPlantStatusDTO> getOnlineStatus(
            @RequestParam(required = false) String session_id,
            @RequestParam(required = false) String user_id) {

        // Validate session if provided (consistent with BulkCustomerController)
        validateSession(session_id, user_id);

        MdmPlantStatusDTO status = monitoringService.getOnlinePlantStatus();
        return ResponseEntity.ok(status);
    }

    private void validateSession(String sessionId, String userId) {
        if (sessionId != null && userId != null && !sessionId.trim().isEmpty() && !userId.trim().isEmpty()) {
            try {
                SecInfoLoginDTO.SessionValidationRequest validationRequest = new SecInfoLoginDTO.SessionValidationRequest();
                validationRequest.setSessionId(sessionId);
                validationRequest.setUserId(userId);
                SecInfoLoginDTO.SessionValidationResponse validationResponse = secInfoAuthService.validateSession(validationRequest);

                if (validationResponse == null || !Boolean.TRUE.equals(validationResponse.getValid())) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session");
                }
            } catch (ResponseStatusException e) {
                throw e;
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Session validation error: " + e.getMessage());
            }
        }
    }
}
