package com.example.SPSProjectBackend.controller;

import com.example.SPSProjectBackend.dto.MdmPlantStatusDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.service.MdmNcreMonitoringService;
import com.example.SPSProjectBackend.service.SecInfoAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MdmNcreMonitoringControllerTest {

    @Mock
    private MdmNcreMonitoringService monitoringService;

    @Mock
    private SecInfoAuthService secInfoAuthService;

    private MdmNcreMonitoringController controller;

    @BeforeEach
    void setUp() {
        controller = new MdmNcreMonitoringController(monitoringService, secInfoAuthService);
    }

    @Test
    void testGetOnlineStatus_WithoutSession_ReturnsOk() {
        MdmPlantStatusDTO status = MdmPlantStatusDTO.builder()
                .success(true)
                .sourceFresh(true)
                .checkedAt("2026-09-25T10:00:00Z")
                .onlineFolios(Arrays.asList("FOLIO001", "FOLIO002"))
                .build();

        when(monitoringService.getOnlinePlantStatus()).thenReturn(status);

        ResponseEntity<MdmPlantStatusDTO> response = controller.getOnlineStatus(null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals(2, response.getBody().getOnlineFolios().size());
        verify(secInfoAuthService, never()).validateSession(any());
    }

    @Test
    void testGetOnlineStatus_WithValidSession_ReturnsOk() {
        SecInfoLoginDTO.SessionValidationResponse validationResponse = new SecInfoLoginDTO.SessionValidationResponse();
        validationResponse.setValid(true);

        when(secInfoAuthService.validateSession(any())).thenReturn(validationResponse);

        MdmPlantStatusDTO status = MdmPlantStatusDTO.builder()
                .success(true)
                .sourceFresh(true)
                .checkedAt("2026-09-25T10:00:00Z")
                .onlineFolios(Collections.singletonList("FOLIO001"))
                .build();

        when(monitoringService.getOnlinePlantStatus()).thenReturn(status);

        ResponseEntity<MdmPlantStatusDTO> response = controller.getOnlineStatus("sess-123", "user-456");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        verify(secInfoAuthService, times(1)).validateSession(any());
    }

    @Test
    void testGetOnlineStatus_WithInvalidSession_ThrowsUnauthorized() {
        SecInfoLoginDTO.SessionValidationResponse validationResponse = new SecInfoLoginDTO.SessionValidationResponse();
        validationResponse.setValid(false);

        when(secInfoAuthService.validateSession(any())).thenReturn(validationResponse);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
            controller.getOnlineStatus("bad-sess", "user-456");
        });

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(monitoringService, never()).getOnlinePlantStatus();
    }
}
