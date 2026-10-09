package com.example.SPSProjectBackend.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;  // Changed to java.util.Date
import java.sql.Timestamp;

@Getter
@Setter
public class MtrDetailDTO {
    private String instId;
    private String addedBlcy;
    private Short mtr1set;
    private Short mtr2set;
    private Short mtr3set;
    private Integer mtrSeq;
    private Short mtrsetType;
    private Short mtrOrder;
    private String mtrType;
    private String noOfPhases;
    private String mtrNbr;
    private Integer prsntRdn;
    private String ctRatio;
    private String mtrRatio;
    private BigDecimal mFactor;
    private String effctBlcy;
    private Date effctDate;  // Now java.util.Date
    private BigDecimal avgCnsp3;
    private BigDecimal avgCnsp6;
    private BigDecimal avgCnsp12;
    private String brCode;
    private String userId;
    private Timestamp enteredDtime;
    private String editedUserid;
    private Timestamp editedDtime;
}