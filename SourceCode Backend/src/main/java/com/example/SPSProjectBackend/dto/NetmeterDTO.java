package com.example.SPSProjectBackend.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;  // Changed to java.util.Date
import java.sql.Timestamp;

@Getter
@Setter
public class NetmeterDTO {
    private String accNbr;
    private Integer bfUnits;
    private Integer avgImp;
    private Integer avgExp;
    private String netType;
    private Date agrmntDate;  // Now java.util.Date
    private BigDecimal rate1;
    private BigDecimal rate2;
    private Short period1;
    private Short period2;
    private String bankCode;
    private String branCode;
    private String bkAcNo;
    private BigDecimal genCap;
    private Timestamp enteredDtime;
    private Short addedBlcy;
    private String setoff;
    private String schm;
    private BigDecimal totReten;
    private BigDecimal rate3;
}