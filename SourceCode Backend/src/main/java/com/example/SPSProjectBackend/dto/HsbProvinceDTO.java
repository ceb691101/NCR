package com.example.SPSProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsbProvinceDTO {
    
    @JsonProperty("prov_code")
    private String provCode;
    
    @JsonProperty("prov_name")
    private String provName;
    
    @JsonProperty("region")
    private String region; // Region code derived from areas
}