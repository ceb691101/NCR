package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tmp_payment", schema = "dbadmin")
@IdClass(TmpPaymentId.class)
public class TmpPayment {
    
    @Id
    @Column(name = "agent_code", length = 4)
    private String agentCode;
    
    @Id
    @Column(name = "cent_code", length = 3)
    private String centCode;
    
    @Id
    @Column(name = "acc_nbr", length = 10)
    private String accNbr;
    
    @Column(name = "counter", length = 3)
    private String counter;
    
    @Column(name = "lot", length = 2)
    private String lot;
    
    @Id
    @Column(name = "stub_no")
    private Short stubNo;
    
    @Column(name = "actl_pay_date")
    private LocalDate actualPayDate;
    
    @Column(name = "credit_date")
    private LocalDate creditDate;
    
    @Column(name = "pay_mode", length = 1)
    private String payMode;
    
    @Column(name = "bank_code", length = 4)
    private String bankCode;
    
    @Column(name = "branch_code", length = 3)
    private String branchCode;
    
    @Column(name = "chq_no", length = 8)
    private String chequeNo;
    
    @Column(name = "bank_account", length = 15)
    private String bankAccount;
    
    @Column(name = "paid_amt", precision = 15, scale = 2)
    private BigDecimal paidAmount;
    
    @Column(name = "up_date")
    private LocalDate updateDate;
    
    @Column(name = "crnt_cycle", length = 3)
    private String currentCycle;

    // Constructors
    public TmpPayment() {
    }

    public TmpPayment(String agentCode, String centCode, String accNbr, String counter,
                      LocalDate actualPayDate, LocalDate creditDate, String payMode,
                      BigDecimal paidAmount) {
        this.agentCode = agentCode;
        this.centCode = centCode;
        this.accNbr = accNbr;
        this.counter = counter;
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

    @Override
    public String toString() {
        return "TmpPayment{" +
                "agentCode='" + agentCode + '\'' +
                ", centCode='" + centCode + '\'' +
                ", accNbr='" + accNbr + '\'' +
                ", stubNo=" + stubNo +
                ", paidAmount=" + paidAmount +
                ", payMode='" + payMode + '\'' +
                '}';
    }
}