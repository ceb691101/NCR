package com.example.SPSProjectBackend.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class TmpCustomerDTO {

    private String jobNbr;
    private String areaCd;
    private String cusCat;
    private String natSup;
    private String name;
    private String addressL1;
    private String addressL2;
    private String city;
    private String telNbr;
    private String idNbr;
    private String idType;
    private String issuedDt;  // yyyy-MM-dd
    private BigDecimal estAmnt;
    private String espayDt;   // yyyy-MM-dd
    private String estPivNbr;
    private String indType;
    private BigDecimal depositAmt;
    private String depDate;   // yyyy-MM-dd
    private String depPivNbr;
    private BigDecimal addDepAmt;  // Used for total security deposit calculation
    private BigDecimal totSecDep;  // New field added
    private BigDecimal cntrDmnd;
    private String tariff;
    private String gstApl;
    private String agrmntNo;
    private String cnectDate; // yyyy-MM-dd
    private Short noLoans;
    private BigDecimal loanAmt;  // Renamed from loanAmount
    private String loanType;     // New field
    private String lnStatus;     // New field
    private Short cstSt;
    private String custCd;  // Fixed: added 't' for consistency
    private String redCode;
    private String dlyPack;
    private String wlkOrd;
    private Short noOfPhases;
    private String taxInv;
    private String taxNum;
    private String userId;  // Used for accountant review
}