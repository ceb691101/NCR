package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NcreDeveloperDTO {

    @JsonProperty("developerName")
    @NotBlank(message = "developerName is required")
    @Size(max = 150)
    private String developerName;

    @JsonProperty("groupOfCompany")
    @Size(max = 100)
    private String groupOfCompany;

    @JsonProperty("email")
    @Email(message = "email must be valid")
    @Size(max = 100)
    private String email;

    @JsonProperty("phone")
    @Size(max = 50)
    private String phone;

    @JsonProperty("accountNumber")
    @NotBlank(message = "accountNumber is required")
    @Size(max = 10)
    private String accountNumber;

    @JsonProperty("folioNumber")
    private Short folioNumber;

    @JsonProperty("projectName")
    @Size(max = 150)
    private String projectName;

    @JsonProperty("area")
    @Size(max = 50)
    private String area;

    @JsonProperty("fileReferenceNo")
    @Size(max = 50)
    private String fileReferenceNo;

    @JsonProperty("srNoUptoDate")
    private Integer srNoUptoDate;    //not sending from the frontend

    @JsonProperty("fileNo")
    private Short fileNo;          //not sending from the frontend

    @JsonProperty("province")
    @Size(max = 50)
    private String province;

    @JsonProperty("loiIssued")
    // @Size(max = 100)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate loiIssued;

    @JsonProperty("sppaSignedDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sppaSignedDate;

    @JsonProperty("gridConnectionDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate gridConnectionDate;

    @JsonProperty("referenceCode")
    @Size(max = 50)
    private String referenceCode;

    @JsonProperty("region")
    @Size(max = 10)
    private String region;

    @JsonProperty("srNo")
    @Size(max = 10)
    private String srNo;

    @JsonProperty("gridSubstation")
    @Size(max = 100)
    private String gridSubstation;

    @JsonProperty("ncreType")
    @Size(max = 20)
    private String ncreType;

    @JsonProperty("initialTariff")
    @Size(max = 50)
    private String initialTariff;

    @JsonProperty("tariffType")
    @NotBlank(message = "tariffType is required")
    @Size(max = 20)
    private String tariffType;

    @JsonProperty("expirationDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;

    @JsonProperty("expirationExtensionDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationExtensionDate;

    @JsonProperty("feederNo")
    private Short feederNo;

    @JsonProperty("commissionedYear")
    private Short commissionedYear;

    @JsonProperty("commissionedCapacityMw")
    private BigDecimal commissionedCapacityMw;

    @JsonProperty("sppaSignedCapacityMw")
    private BigDecimal sppaSignedCapacityMw;

    @JsonProperty("acceptRu")
    private Short acceptRu;

    @JsonProperty("acExpirationWithExtension")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate acExpirationWithExtension;

    @JsonProperty("exDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate exDate;

    @JsonProperty("acDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate acDate;

    @JsonProperty("flatDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate flatDate;

    @JsonProperty("tttDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tttDate;

    @JsonProperty("firstTierDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate firstTierDate;

    @JsonProperty("secondTierDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate secondTierDate;

    @JsonProperty("thirdTierDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate thirdTierDate;

    @JsonProperty("newSppaSigned")
    // @Size(max = 50)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate newSppaSigned;

    @JsonProperty("validityStart")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validityStart;

    @JsonProperty("validityExpiry")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate validityExpiry;

    @JsonProperty("initialTariffRevised")
    private BigDecimal initialTariffRevised;

    @JsonProperty("recommissionedOn")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate recommissionedOn;

    @JsonProperty("epExpired")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate epExpired;

    @JsonProperty("glExpired")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate glExpired;

    @JsonProperty("projectStatus")
    @Size(max = 50)
    private String projectStatus;

    @JsonProperty("voltageLevelKv")
    private BigDecimal voltageLevelKv;

    @JsonProperty("generationLosses")
    private BigDecimal generationLosses;

    @JsonProperty("paymentDeductions")
    private BigDecimal paymentDeductions;

    @JsonProperty("addressLine1")
    // @NotBlank(message = "addressLine1 is required")
    @Size(max = 200)
    private String addressLine1;

    @JsonProperty("addressLine2")
    @Size(max = 200)
    private String addressLine2;

    @JsonProperty("addressLine3")
    @Size(max = 200)
    private String addressLine3;

    @JsonProperty("contactPerson")
    @Size(max = 100)
    private String contactPerson;

    @JsonProperty("companyGroup")
    @Size(max = 100)
    private String companyGroup;

    @JsonProperty("tenderOrNot")
    //@Size(max = 20)
    private String tenderOrNot;     //not sending from the frontend

    @JsonProperty("reductions")
    @Size(max = 50)
    private String reductions;

    @JsonProperty("agreementType")
    @Size(max = 100)
    private String agreementType;

    @JsonProperty("longitude")
    private BigDecimal longitude;

    @JsonProperty("latitude")
    private BigDecimal latitude;

    @JsonProperty("meterNo")
    @Size(max = 10)
    private String meterNo;

    @JsonProperty("responsibleEe")
    @Size(max = 100)
    private String responsibleEe;
}
