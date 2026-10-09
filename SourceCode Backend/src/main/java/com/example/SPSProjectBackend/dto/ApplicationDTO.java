package com.example.SPSProjectBackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ApplicationDTO {
    private String applicationId;
    private String deptId;
    private String applicationNo;
    private String applicationType;
    private String applicationSubType;
    private LocalDate submitDate;
    private String idNo;
    private String preparedBy;
    private String confirmedBy;
    private LocalDate confirmedDate;
    private String confirmedTime;
    private String allocatedTo;
    private String allocatedBy;
    private LocalDate allocatedDate;
    private String allocatedTime;
    private String status;
    private String addUser;
    private LocalDate addDate;
    private String addTime;
    private String updUser;
    private LocalDate updDate;
    private String updTime;
    private String description;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Short durationInDays;
    private Boolean durationType;
    private Short duration;
    private Short disconnectedWithin;
    private Short finalizedWithin;
    private Boolean isLoanApp;
    private Boolean isVisitngNeeded;
    private String samurdhiMember;
    private String contactIdNo;
    private String contactName;
    private String contactAddress;
    private String contactTelephone;
    private String contactMobile;
    private String contactEmail;
    private Boolean isPiv1Needed;
    private String linkedWith;
    private String applicableStdYear;
    private Boolean isTariffChange;
    private Boolean isSequenceChange;
    private String existTariff;
}
