// HsbProvince
package com.example.SPSProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsbAreaDTO {
    
    @JsonProperty("area_code")
    private String areaCode;
    
    @JsonProperty("prov_code")
    private String provCode;
    
    @JsonProperty("area_name")
    private String areaName;
    
    @JsonProperty("region")
    private String region;
    
    @JsonProperty("province_name")
    private String provinceName; // From joined province table
    
    // Constructor without province name (basic info)
    public HsbAreaDTO(String areaCode, String provCode, String areaName, String region) {
        this.areaCode = areaCode;
        this.provCode = provCode;
        this.areaName = areaName;
        this.region = region;
    }
}