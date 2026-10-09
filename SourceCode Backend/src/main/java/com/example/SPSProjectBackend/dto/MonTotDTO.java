package com.example.SPSProjectBackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class MonTotDTO {
    private String accNbr;
    private String billCycle;
    private String errStat;
    private BigDecimal bfBal;
    private BigDecimal totUntsKwo;
    private BigDecimal totUntsKwd;
    private BigDecimal totUntsKwp;
    private BigDecimal totKva;
    private BigDecimal totKwoChg;
    private BigDecimal totKwdChg;
    private BigDecimal totKwpChg;
    private BigDecimal totKvaChg;
    private BigDecimal totCharge;
    private BigDecimal fixedChg;
    private BigDecimal totGst;
    private BigDecimal totAmt;
    private BigDecimal debtTot;
    private BigDecimal crdtTot;
    private BigDecimal payTot;
    private BigDecimal crntBal;
    private BigDecimal kwoAvg3;
    private BigDecimal kwoAvg6;
    private BigDecimal kwoAvg12;
    private BigDecimal kwdAvg3;
    private BigDecimal kwdAvg6;
    private BigDecimal kwdAvg12;
    private BigDecimal kwpAvg3;
    private BigDecimal kwpAvg6;
    private BigDecimal kwpAvg12;
    private BigDecimal kvaAvg3;
    private BigDecimal kvaAvg6;
    private BigDecimal kvaAvg12;
    private String totBillStat;
    private String userId;
    private Date enteredDtime;
    private String editedUserId;
    private Date editedDtime;
}