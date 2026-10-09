package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TmpPaymentDTO {
    
    @JsonProperty("agent_code")
    private String agentCode;
    
    @JsonProperty("cent_code")
    private String centCode;
    
    @JsonProperty("acc_nbr")
    private String accNbr;

    @JsonProperty("folio_no")
    private Short folioNo;
    
    @JsonProperty("counter")
    private String counter;
    
    @JsonProperty("lot")
    private String lot;
    
    @JsonProperty("stub_no")
    private Short stubNo;
    
    @JsonProperty("actl_pay_date")
    private LocalDate actualPayDate;
    
    @JsonProperty("credit_date")
    private LocalDate creditDate;
    
    @JsonProperty("pay_mode")
    private String payMode;
    
    @JsonProperty("bank_code")
    private String bankCode;
    
    @JsonProperty("branch_code")
    private String branchCode;
    
    @JsonProperty("chq_no")
    private String chequeNo;
    
    @JsonProperty("bank_account")
    private String bankAccount;
    
    @JsonProperty("paid_amt")
    private BigDecimal paidAmount;
    
    @JsonProperty("up_date")
    private LocalDate updateDate;
    
    @JsonProperty("crnt_cycle")
    private String currentCycle;

    // Constructors
    public TmpPaymentDTO() {
    }

    public TmpPaymentDTO(String agentCode, String centCode, String accNbr,
                        String counter, Short stubNo, LocalDate actualPayDate, 
                        LocalDate creditDate, String payMode, BigDecimal paidAmount) {
        this.agentCode = agentCode;
        this.centCode = centCode;
        this.accNbr = accNbr;
        this.counter = counter;
        this.stubNo = stubNo;
        this.actualPayDate = actualPayDate;
        this.creditDate = creditDate;
        this.payMode = payMode;
        this.paidAmount = paidAmount;
    }

    // Getters and Setters
    public String getAgentCode() {
        return agentCode;
    }

    public void setAgentCode(String agentCode) {
        this.agentCode = agentCode;
    }

    public String getCentCode() {
        return centCode;
    }

    public void setCentCode(String centCode) {
        this.centCode = centCode;
    }

    public String getAccNbr() {
        return accNbr;
    }

    public void setAccNbr(String accNbr) {
        this.accNbr = accNbr;
    }

    public Short getFolioNo() {
        return folioNo;
    }

    public void setFolioNo(Short folioNo) {
        this.folioNo = folioNo;
    }

    public String getCounter() {
        return counter;
    }

    public void setCounter(String counter) {
        this.counter = counter;
    }

    public String getLot() {
        return lot;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public Short getStubNo() {
        return stubNo;
    }

    public void setStubNo(Short stubNo) {
        this.stubNo = stubNo;
    }

    public LocalDate getActualPayDate() {
        return actualPayDate;
    }

    public void setActualPayDate(LocalDate actualPayDate) {
        this.actualPayDate = actualPayDate;
    }

    public LocalDate getCreditDate() {
        return creditDate;
    }

    public void setCreditDate(LocalDate creditDate) {
        this.creditDate = creditDate;
    }

    public String getPayMode() {
        return payMode;
    }

    public void setPayMode(String payMode) {
        this.payMode = payMode;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getChequeNo() {
        return chequeNo;
    }

    public void setChequeNo(String chequeNo) {
        this.chequeNo = chequeNo;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public LocalDate getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDate updateDate) {
        this.updateDate = updateDate;
    }

    public String getCurrentCycle() {
        return currentCycle;
    }

    public void setCurrentCycle(String currentCycle) {
        this.currentCycle = currentCycle;
    }
}