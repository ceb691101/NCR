package com.example.SPSProjectBackend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class DeveloperRegistrationRequest {
    
    // Developer Info
    private String developerName;
    private String groupOfCompany;
    private String contactPerson;
    private String email;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;

    // Project Info
    private Short folioNumber;
    private String accountNumber;
    private String fileReferenceNo;
    private String projectName; // Maps to facilityName
    private String province;
    private Date loiIssued;
    private Date sppaSignedDate;
    private Date gridConnectionDate;
    private Date expirationDate;
    private String referenceCode;
    private String gridSubstation;
    private String ncreType;
    private String feederNo;
    private String meterNo;
    private String reductions;

    // Location Info
    private String area;
    private Double longitude;
    private Double latitude;
    private String region;
    private String srNo;
    private String status;

    // Global Tariff Details
    private String agreementType;
    private String tariffType;
    private Short commissionedYear;
    private BigDecimal commissionedCapacityMw;
    private BigDecimal sppaSignedCapacityMw;
    private Short acceptRu;
    private Date epExpired;
    private Date glExpired;
    private Date firstTierDate;
    private Date secondTierDate;
    private Date thirdTierDate;

    private String responsibleEe;

    // Dynamic Agreements Array
    private List<AgreementDTO> agreements;
}
