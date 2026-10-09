package com.example.SPSProjectBackend.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class MeterAmendmentDTO {
    private String accNbr;
    @com.fasterxml.jackson.annotation.JsonProperty("folio_no")
    private Short folioNo;
    private String areaCd;
    private String addedBlcy;
    private String effctBlcy;
    private Timestamp effctDate;
    private String typeChg;
    private String amdType;
    private String mtrType;
    private Integer mtrSeq;
    private String mtrsetType;
    private Integer mtrOrder;
    private String mtrNbr;
    private Integer prsntRdn;
    private Integer prvRdn;
    private BigDecimal unts;
    private BigDecimal rate;
    private BigDecimal amt;
    private String mtrRatio;
    private String ctRatio;
    private BigDecimal mFactor;
    private String brCode;
    private String rsnCode;
    private String rsnDesc; // Reason description for display
    private Integer updateStatus;
    private String userId;
    private Timestamp enteredDtime;

    // Constructors
    public MeterAmendmentDTO() {}

    // Getters and Setters
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

    public String getAreaCd() {
        return areaCd;
    }

    public void setAreaCd(String areaCd) {
        this.areaCd = areaCd;
    }

    public String getAddedBlcy() {
        return addedBlcy;
    }

    public void setAddedBlcy(String addedBlcy) {
        this.addedBlcy = addedBlcy;
    }

    public String getEffctBlcy() {
        return effctBlcy;
    }

    public void setEffctBlcy(String effctBlcy) {
        this.effctBlcy = effctBlcy;
    }

    public Timestamp getEffctDate() {
        return effctDate;
    }

    public void setEffctDate(Timestamp effctDate) {
        this.effctDate = effctDate;
    }

    public String getTypeChg() {
        return typeChg;
    }

    public void setTypeChg(String typeChg) {
        this.typeChg = typeChg;
    }

    public String getAmdType() {
        return amdType;
    }

    public void setAmdType(String amdType) {
        this.amdType = amdType;
    }

    public String getMtrType() {
        return mtrType;
    }

    public void setMtrType(String mtrType) {
        this.mtrType = mtrType;
    }

    public Integer getMtrSeq() {
        return mtrSeq;
    }

    public void setMtrSeq(Integer mtrSeq) {
        this.mtrSeq = mtrSeq;
    }

    public String getMtrsetType() {
        return mtrsetType;
    }

    public void setMtrsetType(String mtrsetType) {
        this.mtrsetType = mtrsetType;
    }

    public Integer getMtrOrder() {
        return mtrOrder;
    }

    public void setMtrOrder(Integer mtrOrder) {
        this.mtrOrder = mtrOrder;
    }

    public String getMtrNbr() {
        return mtrNbr;
    }

    public void setMtrNbr(String mtrNbr) {
        this.mtrNbr = mtrNbr;
    }

    public Integer getPrsntRdn() {
        return prsntRdn;
    }

    public void setPrsntRdn(Integer prsntRdn) {
        this.prsntRdn = prsntRdn;
    }

    public Integer getPrvRdn() {
        return prvRdn;
    }

    public void setPrvRdn(Integer prvRdn) {
        this.prvRdn = prvRdn;
    }

    public BigDecimal getUnts() {
        return unts;
    }

    public void setUnts(BigDecimal unts) {
        this.unts = unts;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getAmt() {
        return amt;
    }

    public void setAmt(BigDecimal amt) {
        this.amt = amt;
    }

    public String getMtrRatio() {
        return mtrRatio;
    }

    public void setMtrRatio(String mtrRatio) {
        this.mtrRatio = mtrRatio;
    }

    public String getCtRatio() {
        return ctRatio;
    }

    public void setCtRatio(String ctRatio) {
        this.ctRatio = ctRatio;
    }

    public BigDecimal getmFactor() {
        return mFactor;
    }

    public void setmFactor(BigDecimal mFactor) {
        this.mFactor = mFactor;
    }

    public String getBrCode() {
        return brCode;
    }

    public void setBrCode(String brCode) {
        this.brCode = brCode;
    }

    public String getRsnCode() {
        return rsnCode;
    }

    public void setRsnCode(String rsnCode) {
        this.rsnCode = rsnCode;
    }

    public String getRsnDesc() {
        return rsnDesc;
    }

    public void setRsnDesc(String rsnDesc) {
        this.rsnDesc = rsnDesc;
    }

    public Integer getUpdateStatus() {
        return updateStatus;
    }

    public void setUpdateStatus(Integer updateStatus) {
        this.updateStatus = updateStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getEnteredDtime() {
        return enteredDtime;
    }

    public void setEnteredDtime(Timestamp enteredDtime) {
        this.enteredDtime = enteredDtime;
    }
}
