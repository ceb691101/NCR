package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PIV_AMOUNT")
@IdClass(PivAmountId.class)
public class PivAmount {

    @Id
    @Column(name = "DEPT_ID", length = 6, nullable = false)
    private String deptId;

    @Id
    @Column(name = "PIV_NO", length = 24, nullable = false)
    private String pivNo;

    @Id
    @Column(name = "ACCOUNT_CODE", length = 20, nullable = false)
    private String accountCode;

    @Column(name = "AMOUNT", nullable = false)
    private BigDecimal amount;

    @Column(name = "ADD_USER", length = 10)
    private String addUser;

    @Column(name = "UPD_USER", length = 10)
    private String updUser;

    @Column(name = "TAX_PERCENT")
    private BigDecimal taxPercent;

    @Column(name = "SORT_KEY")
    private BigDecimal sortKey;

    @Column(name = "ADD_DATE")
    private LocalDateTime addDate;

    @Column(name = "UPD_DATE")
    private LocalDateTime updDate;

    @Column(name = "DISPLAY_AMOUNT")
    private BigDecimal displayAmount;

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    // Getters and setters
    // Getter for amount
    public BigDecimal getAmount() {
        return amount;
    }
    // Setter for amount
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getSortKey() {
        return sortKey;
    }
    // Setter for amount
    public void setSortKey(BigDecimal sortKey) {
        this.sortKey = sortKey;
    }

    public BigDecimal getdisplayAmount() {
        return displayAmount;
    }
    // Setter for amount
    public void setdisplayAmount(BigDecimal displayAmount) {
        this.displayAmount = displayAmount;
    }
    public String getPivNo() {
        return pivNo;
    }

    public void setPivNo(String pivNo) {
        this.pivNo = pivNo;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getUpdUser() {
        return updUser;
    }

    public void setaddUser(String addUser) {
        this.addUser = addUser;
    }

    public String getaddUser() {
        return addUser;
    }

    public void setUpdUser(String updUser) {
        this.updUser = updUser;
    }

    public BigDecimal getTaxPercent() {
        return taxPercent;
    }

    public void setTaxPercent(BigDecimal taxPercent) {
        this.taxPercent = taxPercent;
    }
    // Other getters and setters
}