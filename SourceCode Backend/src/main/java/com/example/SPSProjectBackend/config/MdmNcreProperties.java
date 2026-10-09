package com.example.SPSProjectBackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mdm.ncre")
@Data
public class MdmNcreProperties {
    private String baseUrl = "https://mdm.ceb.lk";
    private String tokenUrl = "https://mdm.ceb.lk/public-api/OAuth/token";
    private String clientId;
    private String clientSecret;
    private int connectTimeoutMs = 4000;
    private int readTimeoutMs = 15000;
    private int maxRetries = 3;
}
