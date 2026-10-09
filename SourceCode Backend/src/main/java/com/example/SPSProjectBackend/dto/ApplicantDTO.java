package com.example.SPSProjectBackend.dto;



import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class ApplicantDTO {
    private String idNo;
    private String idType;
    private String firstName;
    private String lastName;
    private String streetAddress;
    private String suburb;
    private String city;
    private String postalCode;
    private String email;
    private String telephoneNo;
    private String mobileNo;
    private Character cebEmployee;
    private String preferredLanguage;
    private Character status;
    private String addUser;
    private Date addDate;
    private String addTime;
    private String updUser;
    private Date updDate;
    private String updTime;
    private Character entitledForLoan;
    private Character memberOfSamurdhi;
    private String samurdhiId;
    private BigDecimal sharePrice;
    private BigDecimal noOfShares;
    private String loanReference;
    private BigDecimal loanAmount;
    private String companyName;
    private String deptId;
    private String fullName;
    private String personalCorporate;
}