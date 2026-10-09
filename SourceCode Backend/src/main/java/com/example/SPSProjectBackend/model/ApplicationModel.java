package com.example.SPSProjectBackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "APPLICATIONS")

public class ApplicationModel {
    @EmbeddedId
    private ApplicationModelId id;

    @Column(name = "APPLICATION_NO", length = 21)
    private String applicationNo;

    @Column(name = "DEPT_ID", nullable = false, length = 6)
    private String deptId;

    @Column(name = "APPLICATION_TYPE", nullable = false, length = 2)
    private String applicationType;

    @Column(name = "APPLICATION_SUB_TYPE", length = 2)
    private String applicationSubType;

    @Column(name = "SUBMIT_DATE", nullable = false)
    private LocalDate submitDate;

    @Column(name = "ID_NO", nullable = false, length = 12)
    private String idNo;

    @Column(name = "PREPARED_BY", nullable = false, length = 50)
    private String preparedBy;

    @Column(name = "CONFIRMED_BY", length = 10)
    private String confirmedBy;

    @Column(name = "CONFIRMED_DATE")
    private LocalDate confirmedDate;

    @Column(name = "CONFIRMED_TIME", length = 11)
    private String confirmedTime;

    @Column(name = "ALLOCATED_TO", length = 10)
    private String allocatedTo;

    @Column(name = "ALLOCATED_BY", length = 10)
    private String allocatedBy;

    @Column(name = "ALLOCATED_DATE")
    private LocalDate allocatedDate;

    @Column(name = "ALLOCATED_TIME", length = 11)
    private String allocatedTime;

    @Column(name = "STATUS", nullable = false, length = 2)
    private String status;

    @Column(name = "ADD_USER", length = 50)
    private String addUser;

    @Column(name = "ADD_DATE")
    private LocalDate addDate;

    @Column(name = "ADD_TIME", length = 11)
    private String addTime;

    @Column(name = "UPD_USER", length = 50)
    private String updUser;

    @Column(name = "UPD_DATE")
    private LocalDate updDate;

    @Column(name = "UPD_TIME", length = 11)
    private String updTime;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "FROM_DATE")
    private LocalDate fromDate;

    @Column(name = "TO_DATE")
    private LocalDate toDate;

    @Column(name = "DURATION_IN_DAYS")
    private Short durationInDays;

    @Column(name = "DURATION_TYPE")
    private Boolean durationType;

    @Column(name = "DURATION")
    private Short duration;

    @Column(name = "DISCONNECTED_WITHIN")
    private Short disconnectedWithin;

    @Column(name = "FINALIZED_WITHIN")
    private Short finalizedWithin;

    @Column(name = "IS_LOAN_APP")
    private Boolean isLoanApp;

    @Column(name = "IS_VISITNG_NEEDED")
    private Boolean isVisitngNeeded;

    @Column(name = "SAMURDHI_MEMBER", length = 1)
    private String samurdhiMember;

    @Column(name = "CONTACT_ID_NO", length = 10)
    private String contactIdNo;

    @Column(name = "CONTACT_NAME", length = 100)
    private String contactName;

    @Column(name = "CONTACT_ADDRESS", length = 150)
    private String contactAddress;

    @Column(name = "CONTACT_TELEPHONE", length = 10)
    private String contactTelephone;

    @Column(name = "CONTACT_MOBILE", length = 10)
    private String contactMobile;

    @Column(name = "CONTACT_EMAIL", length = 30)
    private String contactEmail;

    @Column(name = "IS_PIV1_NEEDED")
    private Boolean isPiv1Needed;

    @Column(name = "LINKED_WITH", length = 24)
    private String linkedWith;

    @Column(name = "APPLICABLE_STD_YEAR", length = 6)
    private String applicableStdYear;

    @Column(name = "IS_TARIFF_CHANGE")
    private Boolean isTariffChange;

    @Column(name = "IS_SEQUENCE_CHANGE")
    private Boolean isSequenceChange;

    @Column(name = "EXIST_TARIFF", length = 2)
    private String existTariff;

    // Getters and Setters
    public String getApplicationId() {
        return id != null ? id.getApplicationId() : null;
    }

    public void setApplicationId(String applicationId) {
        if (id == null) {
            id = new ApplicationModelId();
        }
        id.setApplicationId(applicationId);
    }

    public String getApplicationNo() {
        return applicationNo;
    }

    public void setApplicationNo(String applicationNo) {
        this.applicationNo = applicationNo;
    }

}