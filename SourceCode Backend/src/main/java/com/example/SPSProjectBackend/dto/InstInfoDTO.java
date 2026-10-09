package com.example.SPSProjectBackend.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Date;  // Changed to java.util.Date
import java.sql.Timestamp;

@Getter
@Setter
public class InstInfoDTO {
    private String instId;
    private Date dateCnnct;  // Now java.util.Date
    private Short mtrSet;
    private Short nbrMet;
    private String custNum;
    private String typeMet;
    private String dpCode;
    private String trCb;
    private String cnnctTrpnl;
    private BigDecimal trpnlVolt;
    private BigDecimal trpnlAmps;
    private String userId;
    private Timestamp enteredDtime;
    private String editedUserId;
    private Timestamp editedDtime;
}