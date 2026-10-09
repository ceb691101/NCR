package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.config.MdmNcreProperties;
import com.example.SPSProjectBackend.dto.MdmPlantStatusDTO;
import com.example.SPSProjectBackend.dto.MdmPlantsResponse;
import com.example.SPSProjectBackend.dto.MdmTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MdmNcreMonitoringServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private MdmNcreProperties properties;
    private MdmNcreMonitoringService service;

    @BeforeEach
    void setUp() {
        properties = new MdmNcreProperties();
        properties.setBaseUrl("https://mdm.ceb.lk");
        properties.setTokenUrl("https://mdm.ceb.lk/public-api/OAuth/token");
        properties.setClientId("test-client-id");
        properties.setClientSecret("test-client-secret");
        properties.setConnectTimeoutMs(4000);
        properties.setReadTimeoutMs(15000);
        properties.setMaxRetries(1);

        service = new MdmNcreMonitoringService(properties, restTemplate);
    }

    @Test
    void testNormalizeFolio_TrimmingAndCaseAndLeadingZeros() {
        // Trims leading and trailing whitespace
        assertEquals("ABC001", MdmNcreMonitoringService.normalizeFolio(" ABC001 "));
        // Handles case differences
        assertEquals("ABC001", MdmNcreMonitoringService.normalizeFolio("abc001"));
        // Both match
        assertEquals(MdmNcreMonitoringService.normalizeFolio(" ABC001 "), MdmNcreMonitoringService.normalizeFolio("abc001"));
        // Preserves leading zeros
        assertEquals("0042", MdmNcreMonitoringService.normalizeFolio("0042"));
        assertEquals("00001", MdmNcreMonitoringService.normalizeFolio("  00001  "));
        // Handles null and empty safely
        assertEquals("", MdmNcreMonitoringService.normalizeFolio(null));
        assertEquals("", MdmNcreMonitoringService.normalizeFolio("   "));
    }

    @Test
    void testGetAccessToken_SendsCredentialsInBodyAndReusesToken() {
        MdmTokenResponse tokenResponse = new MdmTokenResponse();
        tokenResponse.setAccessToken("mock-bearer-token-12345");
        tokenResponse.setTokenType("Bearer");
        tokenResponse.setExpiresIn(3600L);

        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.postForEntity(eq("https://mdm.ceb.lk/public-api/OAuth/token"), captor.capture(), eq(MdmTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

        // First call requests token
        String token1 = service.getAccessToken();
        assertEquals("mock-bearer-token-12345", token1);

        // Verify credentials in body and no Basic auth
        HttpEntity captured = captor.getValue();
        assertNull(captured.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        org.springframework.util.MultiValueMap<String, String> body = (org.springframework.util.MultiValueMap<String, String>) captured.getBody();
        assertNotNull(body);
        assertEquals("client_credentials", body.getFirst("grant_type"));
        assertEquals("test-client-id", body.getFirst("client_id"));
        assertEquals("test-client-secret", body.getFirst("client_secret"));

        // Second call reuses cached token (no second HTTP post)
        String token2 = service.getAccessToken();
        assertEquals("mock-bearer-token-12345", token2);

        verify(restTemplate, times(1)).postForEntity(eq("https://mdm.ceb.lk/public-api/OAuth/token"), any(HttpEntity.class), eq(MdmTokenResponse.class));
    }

    @Test
    void testGetOnlinePlantStatus_ParsesOnlineFoliosAndSendsBearerHeader() {
        // Mock token response
        MdmTokenResponse tokenResponse = new MdmTokenResponse();
        tokenResponse.setAccessToken("valid-access-token");
        tokenResponse.setExpiresIn(3600L);

        when(restTemplate.postForEntity(eq("https://mdm.ceb.lk/public-api/OAuth/token"), any(HttpEntity.class), eq(MdmTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

        // Mock plant response
        MdmPlantsResponse.MdmPlantItem p1 = new MdmPlantsResponse.MdmPlantItem("id1", "FOLIO001", "online", null, true, "Solar Plant 1", "SPP");
        MdmPlantsResponse.MdmPlantItem p2 = new MdmPlantsResponse.MdmPlantItem("id2", "folio002", "ONLINE", null, true, "Wind Plant 2", "WPP");
        MdmPlantsResponse.MdmPlantItem p3 = new MdmPlantsResponse.MdmPlantItem("id3", "FOLIO003", "stale", null, true, "Hydro Plant 3", "MHP");
        MdmPlantsResponse.MdmPlantItem p4 = new MdmPlantsResponse.MdmPlantItem("id4", "FOLIO004", "never_seen", null, true, "Biomass Plant 4", "BMP");

        MdmPlantsResponse plantsResponse = new MdmPlantsResponse(Arrays.asList(p1, p2, p3, p4), null);

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.exchange(any(java.net.URI.class), eq(HttpMethod.GET), entityCaptor.capture(), eq(MdmPlantsResponse.class)))
                .thenReturn(new ResponseEntity<>(plantsResponse, HttpStatus.OK));

        MdmPlantStatusDTO status = service.getOnlinePlantStatus();

        assertNotNull(status);
        assertTrue(status.getSuccess());
        assertTrue(status.getSourceFresh());
        assertEquals(2, status.getOnlineFolios().size());
        assertTrue(status.getOnlineFolios().contains("FOLIO001"));
        assertTrue(status.getOnlineFolios().contains("FOLIO002"));
        assertFalse(status.getOnlineFolios().contains("FOLIO003"));
        assertFalse(status.getOnlineFolios().contains("FOLIO004"));

        // Verify Authorization Bearer header
        HttpEntity capturedEntity = entityCaptor.getValue();
        assertEquals("Bearer valid-access-token", capturedEntity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void testGetOnlinePlantStatus_HandlesCursorPagination() {
        MdmTokenResponse tokenResponse = new MdmTokenResponse();
        tokenResponse.setAccessToken("valid-access-token");
        tokenResponse.setExpiresIn(3600L);

        when(restTemplate.postForEntity(eq("https://mdm.ceb.lk/public-api/OAuth/token"), any(HttpEntity.class), eq(MdmTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(tokenResponse, HttpStatus.OK));

        // Page 1 with nextCursor
        MdmPlantsResponse.MdmPlantItem p1 = new MdmPlantsResponse.MdmPlantItem("id1", "FOLIO_P1", "online", null, true, "Plant 1", "SPP");
        MdmPlantsResponse page1 = new MdmPlantsResponse(Collections.singletonList(p1), "cursor-page-2");

        // Page 2 with nextCursor = null
        MdmPlantsResponse.MdmPlantItem p2 = new MdmPlantsResponse.MdmPlantItem("id2", "FOLIO_P2", "online", null, true, "Plant 2", "WPP");
        MdmPlantsResponse page2 = new MdmPlantsResponse(Collections.singletonList(p2), null);

        when(restTemplate.exchange(argThat((java.net.URI uri) -> uri != null && uri.toString().contains("cursor=cursor-page-2")), eq(HttpMethod.GET), any(HttpEntity.class), eq(MdmPlantsResponse.class)))
                .thenReturn(new ResponseEntity<>(page2, HttpStatus.OK));

        when(restTemplate.exchange(argThat((java.net.URI uri) -> uri != null && !uri.toString().contains("cursor=")), eq(HttpMethod.GET), any(HttpEntity.class), eq(MdmPlantsResponse.class)))
                .thenReturn(new ResponseEntity<>(page1, HttpStatus.OK));

        MdmPlantStatusDTO status = service.getOnlinePlantStatus();

        assertTrue(status.getSuccess());
        assertEquals(2, status.getOnlineFolios().size());
        assertTrue(status.getOnlineFolios().contains("FOLIO_P1"));
        assertTrue(status.getOnlineFolios().contains("FOLIO_P2"));
    }

    @Test
    void testGetOnlinePlantStatus_HandlesMdmFailureGracefully() {
        // When OAuth token fails or times out
        when(restTemplate.postForEntity(eq("https://mdm.ceb.lk/public-api/OAuth/token"), any(HttpEntity.class), eq(MdmTokenResponse.class)))
                .thenThrow(new ResourceAccessException("Connection timed out"));

        MdmPlantStatusDTO status = service.getOnlinePlantStatus();

        assertNotNull(status);
        assertFalse(status.getSuccess());
        assertFalse(status.getSourceFresh());
        assertTrue(status.getOnlineFolios().isEmpty());
        assertNotNull(status.getMessage());
    }

    @Test
    void testGetOnlinePlantStatus_Handles401UnauthorizedAndRetries() {
        MdmTokenResponse tokenResponse1 = new MdmTokenResponse();
        tokenResponse1.setAccessToken("expired-token");
        tokenResponse1.setExpiresIn(3600L);

        MdmTokenResponse tokenResponse2 = new MdmTokenResponse();
        tokenResponse2.setAccessToken("new-refreshed-token");
        tokenResponse2.setExpiresIn(3600L);

        when(restTemplate.postForEntity(eq("https://mdm.ceb.lk/public-api/OAuth/token"), any(HttpEntity.class), eq(MdmTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(tokenResponse1, HttpStatus.OK))
                .thenReturn(new ResponseEntity<>(tokenResponse2, HttpStatus.OK));

        // First exchange throws 401 Unauthorized
        MdmPlantsResponse.MdmPlantItem p1 = new MdmPlantsResponse.MdmPlantItem("id1", "FOLIO_RETRY", "online", null, true, "Plant Retry", "SPP");
        MdmPlantsResponse successPage = new MdmPlantsResponse(Collections.singletonList(p1), null);

        when(restTemplate.exchange(any(java.net.URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(MdmPlantsResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized"))
                .thenReturn(new ResponseEntity<>(successPage, HttpStatus.OK));

        MdmPlantStatusDTO status = service.getOnlinePlantStatus();

        assertTrue(status.getSuccess());
        assertEquals(1, status.getOnlineFolios().size());
        assertTrue(status.getOnlineFolios().contains("FOLIO_RETRY"));
    }
}
