package com.example.SPSProjectBackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CentralizedAuthService {

    @Value("${company.auth.hr.url:http://10.128.1.126/CBRSAPI/CBRSUPERUserLogin}")
    private String hrAuthUrl;

    @Value("${company.auth.ad.url:http://smartceb.ceb:81/SMART_API/api/UserManagement/ValidateADLoginCEBINFO}")
    private String adAuthUrl;

    private final RestTemplate restTemplate;

    public CentralizedAuthService() {
        this.restTemplate = new RestTemplate();
    }

    public static class AuthResult {
        private final boolean authenticated;
        private final String message;
        private final boolean serviceUnavailable;

        public AuthResult(boolean authenticated, String message, boolean serviceUnavailable) {
            this.authenticated = authenticated;
            this.message = message;
            this.serviceUnavailable = serviceUnavailable;
        }

        public boolean isAuthenticated() {
            return authenticated;
        }

        public String getMessage() {
            return message;
        }

        public boolean isServiceUnavailable() {
            return serviceUnavailable;
        }
    }

    /**
     * Authenticate employee credentials against the chosen company auth service (HR or AD).
     */
    public AuthResult authenticate(String epfNumber, String password, String loginType) {
        if (epfNumber == null || epfNumber.trim().isEmpty()) {
            return new AuthResult(false, "EPF Number is required.", false);
        }

        if (password == null || password.trim().isEmpty()) {
            return new AuthResult(false, "Password is required.", false);
        }

        String trimmedEpf = epfNumber.trim();
        String normalizedType = loginType != null ? loginType.trim().toUpperCase() : "";

        if ("HR".equals(normalizedType)) {
            return authenticateHr(trimmedEpf, password);
        } else if ("AD".equals(normalizedType)) {
            return authenticateAd(trimmedEpf, password);
        } else {
            return new AuthResult(false, "Invalid login type specified. Please choose HR Login or AD Login.", false);
        }
    }

    private AuthResult authenticateHr(String epfNumber, String password) {
        try {
            System.out.println("Initiating HR Auth call to endpoint: " + hrAuthUrl + " for EPF: " + epfNumber);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("Username", epfNumber);
            requestBody.put("Password", password);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(hrAuthUrl, entity, Map.class);
            System.out.println("HR Auth Response Status: " + response.getStatusCode() + ", Body: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map body = response.getBody();
                Object loggedObj = body.get("Logged");
                if (loggedObj == null) {
                    loggedObj = body.get("logged");
                }
                if (loggedObj == null) {
                    loggedObj = body.get("success");
                }
                if (loggedObj == null) {
                    loggedObj = body.get("isSuccess");
                }

                boolean isLogged = false;
                if (loggedObj instanceof Boolean) {
                    isLogged = (Boolean) loggedObj;
                } else if (loggedObj != null) {
                    isLogged = "true".equalsIgnoreCase(loggedObj.toString().trim());
                }

                if (isLogged) {
                    return new AuthResult(true, "HR Authentication successful.", false);
                } else {
                    String msg = body.get("Errormsg") != null ? body.get("Errormsg").toString() : "Invalid EPF number or password.";
                    return new AuthResult(false, msg, false);
                }
            } else {
                return new AuthResult(false, "Invalid EPF number or password.", false);
            }
        } catch (ResourceAccessException rae) {
            System.err.println("HR Auth service connection error connecting to " + hrAuthUrl + ": " + rae.getMessage());
            return new AuthResult(false, "Authentication service is currently unavailable (" + hrAuthUrl + ").", true);
        } catch (HttpStatusCodeException hsce) {
            System.err.println("HR Auth service HTTP error: " + hsce.getStatusCode() + " - " + hsce.getResponseBodyAsString());
            return new AuthResult(false, "Invalid EPF number or password.", false);
        } catch (Exception e) {
            System.err.println("HR Auth error: " + e.getMessage());
            e.printStackTrace();
            return new AuthResult(false, "Authentication service is currently unavailable.", true);
        }
    }

    private AuthResult authenticateAd(String epfNumber, String password) {
        try {
            System.out.println("Initiating AD Auth call to endpoint: " + adAuthUrl + " for EPF: " + epfNumber);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("ad_user_name", epfNumber);
            requestBody.put("ad_password", password);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(adAuthUrl, entity, Map.class);
            System.out.println("AD Auth Response Status: " + response.getStatusCode() + ", Body: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map body = response.getBody();
                Object isSuccessObj = body.get("isSuccess");
                if (isSuccessObj == null) {
                    isSuccessObj = body.get("IsSuccess");
                }
                if (isSuccessObj == null) {
                    isSuccessObj = body.get("success");
                }

                boolean isSuccess = false;
                if (isSuccessObj instanceof Boolean) {
                    isSuccess = (Boolean) isSuccessObj;
                } else if (isSuccessObj != null) {
                    isSuccess = "true".equalsIgnoreCase(isSuccessObj.toString().trim());
                }

                if (isSuccess) {
                    return new AuthResult(true, "AD Authentication successful.", false);
                } else {
                    Object msgObj = body.get("message");
                    if (msgObj == null) msgObj = body.get("Message");
                    if (msgObj == null) msgObj = body.get("error");
                    String msg = msgObj != null ? msgObj.toString() : "Invalid EPF number or password.";
                    return new AuthResult(false, msg, false);
                }
            } else {
                return new AuthResult(false, "Invalid EPF number or password.", false);
            }
        } catch (ResourceAccessException rae) {
            System.err.println("AD Auth service connection error connecting to " + adAuthUrl + ": " + rae.getMessage());
            return new AuthResult(false, "Authentication service is currently unavailable (" + adAuthUrl + ").", true);
        } catch (HttpStatusCodeException hsce) {
            System.err.println("AD Auth service HTTP error: " + hsce.getStatusCode() + " - " + hsce.getResponseBodyAsString());
            return new AuthResult(false, "Invalid EPF number or password.", false);
        } catch (Exception e) {
            System.err.println("AD Auth error: " + e.getMessage());
            e.printStackTrace();
            return new AuthResult(false, "Authentication service is currently unavailable.", true);
        }
    }
}
