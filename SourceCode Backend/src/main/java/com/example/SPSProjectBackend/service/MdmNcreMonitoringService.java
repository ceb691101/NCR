package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.config.MdmNcreProperties;
import com.example.SPSProjectBackend.dto.MdmPlantStatusDTO;
import com.example.SPSProjectBackend.dto.MdmPlantsResponse;
import com.example.SPSProjectBackend.dto.MdmTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class MdmNcreMonitoringService {

    private final MdmNcreProperties properties;
    private final RestTemplate restTemplate;

    private volatile String cachedToken;
    private volatile Instant tokenExpiresAt;
    private final Object tokenLock = new Object();

    @Autowired
    public MdmNcreMonitoringService(MdmNcreProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeoutMs() > 0 ? properties.getConnectTimeoutMs() : 4000);
        factory.setReadTimeout(properties.getReadTimeoutMs() > 0 ? properties.getReadTimeoutMs() : 15000);
        this.restTemplate = new RestTemplate(factory);
    }

    // Constructor for testing with mock RestTemplate
    public MdmNcreMonitoringService(MdmNcreProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    /**
     * Normalizes a folio number for consistent matching.
     * Rules:
     * - Trim leading and trailing whitespace
     * - Handle case differences (convert to uppercase)
     * - Preserve leading zeros (no numeric conversion)
     * - Handle null safely (returns empty string)
     */
    public static String normalizeFolio(String folio) {
        if (folio == null) {
            return "";
        }
        return folio.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Retrieves or refreshes OAuth2 client credentials access token.
     * Caches token until shortly before expiration (60-second buffer).
     */
    public String getAccessToken() {
        if (cachedToken != null && tokenExpiresAt != null && Instant.now().isBefore(tokenExpiresAt.minusSeconds(60))) {
            return cachedToken;
        }

        synchronized (tokenLock) {
            if (cachedToken != null && tokenExpiresAt != null && Instant.now().isBefore(tokenExpiresAt.minusSeconds(60))) {
                return cachedToken;
            }

            String clientId = properties.getClientId();
            String clientSecret = properties.getClientSecret();
            String tokenUrl = properties.getTokenUrl();

            if (tokenUrl == null || tokenUrl.trim().isEmpty()) {
                throw new IllegalStateException("MDM token URL is not configured");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            // MDM expects client_id and client_secret directly in the form body
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            if (clientId != null && !clientId.trim().isEmpty()) {
                body.add("client_id", clientId.trim());
            }
            if (clientSecret != null && !clientSecret.trim().isEmpty()) {
                body.add("client_secret", clientSecret.trim());
            }

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            int maxAttempts = Math.max(properties.getMaxRetries(), 1);
            RestClientException lastException = null;

            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                try {
                    ResponseEntity<MdmTokenResponse> response = restTemplate.postForEntity(tokenUrl, requestEntity, MdmTokenResponse.class);
                    return processTokenResponse(response.getBody());
                } catch (RestClientException e) {
                    lastException = e;
                    log.warn("OAuth token request attempt {}/{} failed: {}", attempt, maxAttempts, e.getMessage());
                    if (attempt < maxAttempts) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }

            throw lastException != null ? lastException : new IllegalStateException("Failed to obtain MDM OAuth token after " + maxAttempts + " attempts");
        }
    }

    private String processTokenResponse(MdmTokenResponse tokenResponse) {
        if (tokenResponse == null || tokenResponse.getAccessToken() == null || tokenResponse.getAccessToken().trim().isEmpty()) {
            throw new IllegalStateException("MDM OAuth token response did not contain an access token");
        }

        this.cachedToken = tokenResponse.getAccessToken();
        long expiresIn = (tokenResponse.getExpiresIn() != null && tokenResponse.getExpiresIn() > 0)
                ? tokenResponse.getExpiresIn()
                : 1800; // Default to 30 minutes if not provided

        this.tokenExpiresAt = Instant.now().plusSeconds(expiresIn);
        return this.cachedToken;
    }

    public void invalidateTokenCache() {
        synchronized (tokenLock) {
            this.cachedToken = null;
            this.tokenExpiresAt = null;
        }
    }

    /**
     * Fetches all online NCRE plants from MDM via cursor pagination and returns their normalized folios.
     */
    public MdmPlantStatusDTO getOnlinePlantStatus() {
        String checkedAt = Instant.now().toString();
        Set<String> onlineFolios = new LinkedHashSet<>();
        Boolean overallSourceFresh = null;

        try {
            String token = getAccessToken();

            String baseUrl = properties.getBaseUrl();
            if (baseUrl == null || baseUrl.trim().isEmpty()) {
                baseUrl = "https://mdm.ceb.lk";
            }

            String cursor = null;
            int pageCount = 0;
            final int maxPages = 50; // Safety guard against infinite loops

            do {
                pageCount++;
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                        .path("/public-api/v1/ncre/plants")
                        .queryParam("status", "online")
                        .queryParam("limit", 100);

                if (cursor != null && !cursor.trim().isEmpty()) {
                    builder.queryParam("cursor", cursor);
                }

                java.net.URI uri = builder.build().toUri();

                HttpHeaders headers = new HttpHeaders();
                headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                HttpEntity<Void> entity = new HttpEntity<>(headers);

                ResponseEntity<MdmPlantsResponse> response = null;
                int maxExchangeAttempts = Math.max(properties.getMaxRetries(), 1);

                for (int attempt = 1; attempt <= maxExchangeAttempts; attempt++) {
                    try {
                        response = restTemplate.exchange(uri, HttpMethod.GET, entity, MdmPlantsResponse.class);
                        break;
                    } catch (HttpClientErrorException e) {
                        if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                            log.warn("MDM returned 401 Unauthorized for plant query. Invalidating token cache and retrying once.");
                            invalidateTokenCache();
                            token = getAccessToken();
                            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                            entity = new HttpEntity<>(headers);
                            response = restTemplate.exchange(uri, HttpMethod.GET, entity, MdmPlantsResponse.class);
                            break;
                        } else {
                            throw e;
                        }
                    } catch (org.springframework.web.client.ResourceAccessException e) {
                        if (attempt == maxExchangeAttempts) {
                            throw e;
                        }
                        log.warn("MDM plant exchange attempt {}/{} timed out. Retrying...", attempt, maxExchangeAttempts);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw e;
                        }
                    }
                }

                MdmPlantsResponse plantsResponse = response.getBody();
                if (plantsResponse == null) {
                    break;
                }

                if (plantsResponse.getItems() != null) {
                    for (MdmPlantsResponse.MdmPlantItem plant : plantsResponse.getItems()) {
                        if (plant != null && "online".equalsIgnoreCase(plant.getStatus())) {
                            String normalized = normalizeFolio(plant.getFolioNo());
                            if (!normalized.isEmpty()) {
                                onlineFolios.add(normalized);
                            }
                        }
                        if (plant != null && plant.getSourceFresh() != null) {
                            overallSourceFresh = (overallSourceFresh == null)
                                    ? plant.getSourceFresh()
                                    : (overallSourceFresh && plant.getSourceFresh());
                        }
                    }
                }

                cursor = plantsResponse.getNextCursor();

            } while (cursor != null && !cursor.trim().isEmpty() && pageCount < maxPages);

            return MdmPlantStatusDTO.builder()
                    .success(true)
                    .sourceFresh(overallSourceFresh != null ? overallSourceFresh : true)
                    .checkedAt(checkedAt)
                    .onlineFolios(new ArrayList<>(onlineFolios))
                    .build();

        } catch (RestClientException | IllegalStateException e) {
            log.error("Failed to load online plant statuses from MDM: {}", e.getMessage());
            return MdmPlantStatusDTO.builder()
                    .success(false)
                    .sourceFresh(false)
                    .checkedAt(checkedAt)
                    .onlineFolios(Collections.emptyList())
                    .message("MDM monitoring service temporarily unavailable")
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error loading online plant statuses from MDM: {}", e.getMessage());
            return MdmPlantStatusDTO.builder()
                    .success(false)
                    .sourceFresh(false)
                    .checkedAt(checkedAt)
                    .onlineFolios(Collections.emptyList())
                    .message("Unexpected error querying MDM monitoring service")
                    .build();
        }
    }
}
