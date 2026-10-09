package com.example.SPSProjectBackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LoanMasterDTO {
    private String accNbr;
    private String loanType;
    private BigDecimal loanAmt;
    private Short noMonths;
    private Short stBillCycle;
    private Short endBillCycle;
    private BigDecimal intRate;
    private String activeSt;
    private BigDecimal monPmnt;
    private String enteredUser;
    private String editedUser;
}
