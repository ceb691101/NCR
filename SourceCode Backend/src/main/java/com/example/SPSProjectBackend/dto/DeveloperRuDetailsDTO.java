package com.example.SPSProjectBackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeveloperRuDetailsDTO {
    private Short folioNo;
    private String developerName;
    private String facilityName;
    private Short acceptRu;
    private String ncreType;
    private String tariffType;
    private String tariffDesc;
}
