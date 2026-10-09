package com.example.SPSProjectBackend.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "APPLICANTNEW")

public class Applicant {

    @Id
    @Column(name = "ID_NO", length = 12, nullable = false)
    private String idNo;

    @Column(name = "ID_TYPE", length = 3, nullable = false)
    private String idType;

    @NonNull
    @Column(name = "FIRST_NAME", length = 50, nullable = false)
    private String firstName;

    @Column(name = "LAST_NAME", length = 60, nullable = false)
    private String lastName;

    @Column(name = "STREET_ADDRESS", length = 100, nullable = false)
    private String streetAddress;

    @Column(name = "SUBURB", length = 100)
    private String suburb;

    @Column(name = "CITY", length = 100)
    private String city;

    @Column(name = "POSTAL_CODE", length = 5)
    private String postalCode;

    @Column(name = "EMAIL", length = 50)
    private String email;

    @Column(name = "TELEPHONE_NO", length = 10)
    private String telephoneNo;

    @Column(name = "MOBILE_NO", length = 10)
    private String mobileNo;

    @Column(name = "CEB_EMPLOYEE", length = 1)
    private Character cebEmployee;

    @Column(name = "PREFERRED_LANGUAGE", length = 2, nullable = false)
    private String preferredLanguage;

    @Column(name = "STATUS", length = 1)
    private Character status;

    @Column(name = "ADD_USER", length = 10)
    private String addUser;

    @Column(name = "ADD_DATE")
    @Temporal(TemporalType.DATE)
    private Date addDate;


    @Column(name = "ADD_TIME", length = 11)
    private String addTime;

    @Column(name = "UPD_USER", length = 10)
    private String updUser;

    @Column(name = "UPD_DATE")
    @Temporal(TemporalType.DATE)
    private Date updDate;

    @Column(name = "UPD_TIME", length = 11)
    private String updTime;

    @Column(name = "ENTITLED_4_LOAN", length = 1)
    private Character entitledForLoan;

    @Column(name = "MEMBER_OF_SAMURDHI", length = 1)
    private Character memberOfSamurdhi;

    @Column(name = "SAMURDHI_ID", length = 10)
    private String samurdhiId;

    @Column(name = "SHARE_PRICE", precision = 6, scale = 2)
    private BigDecimal sharePrice;

    @Column(name = "NO_OF_SHARES", precision = 2, scale = 0)
    private BigDecimal noOfShares;

    @Column(name = "LOAN_REFERENCE", length = 10)
    private String loanReference;

    @Column(name = "LOAN_AMOUNT", precision = 10, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "COMPANY_NAME", length = 100)
    private String companyName;

    @Column(name = "DEPT_ID", length = 6)
    private String deptId;

    @Column(name = "FULL_NAME", length = 100)
    private String fullName;

    @Column(name = "PERSONAL_CORPORATE", length = 3)
    private String personalCorporate;
}