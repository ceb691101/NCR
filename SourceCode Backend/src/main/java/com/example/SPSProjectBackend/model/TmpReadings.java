package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "tmp_rdngs", indexes = {
    @Index(name = "idx_acc_nbr_mtr_type_rdng_date", columnList = "acc_nbr, mtr_type, rdng_date"),
    @Index(name = "idx_acc_nbr_area_cd", columnList = "acc_nbr, area_cd"),
    @Index(name = "idx_mtr_nbr", columnList = "mtr_nbr")
}, schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(TmpReadingsId.class)
public class TmpReadings {
    
    @Id
    @Column(name = "acc_nbr", length = 10, nullable = false)
    private String accNbr;
    
    @Column(name = "inst_id", length = 8)
    private String instId;
    
    @Id
    @Column(name = "area_cd", length = 2, nullable = false)
    private String areaCd;
    
    @Id
    @Column(name = "added_blcy", length = 3, nullable = false)
    private String addedBlcy;
    
    @Id
    @Column(name = "mtr_seq", nullable = false)
    private Integer mtrSeq;
    
    @Id
    @Column(name = "mtr_type", length = 6, nullable = false)
    private String mtrType;
    
    @Column(name = "prv_date")
    @Temporal(TemporalType.DATE)
    private Date prvDate;
    
    @Column(name = "rdng_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date rdngDate;
    
    @Column(name = "prsnt_rdn")
    private Integer prsntRdn;
    
    @Column(name = "prv_rdn")
    private Integer prvRdn;
    
    @Column(name = "mtr_nbr", length = 10)
    private String mtrNbr;
    
    @Column(name = "units")
    private Integer units;
    
    @Column(name = "rate", precision = 7, scale = 2)
    private BigDecimal rate;
    
    @Column(name = "computed_chg", precision = 15, scale = 2)
    private BigDecimal computedChg;
    
    @Column(name = "mnt_chg", precision = 15, scale = 2)
    private BigDecimal mntChg;
    
    @Column(name = "acode", length = 1)
    private String acode;
    
    @Column(name = "m_factor", precision = 8, scale = 3)
    private BigDecimal mFactor;
    
    @Column(name = "bill_stat", length = 1)
    private String billStat;
    
    @Column(name = "err_stat")
    private Integer errStat;
    
    @Column(name = "mtr_stat", length = 1)
    private String mtrStat;
    
    @Column(name = "rdn_stat", length = 1)
    private String rdnStat;
    
    @Column(name = "user_id", length = 8)
    private String userId;
    
    @Column(name = "entered_dtime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date enteredDtime;
    
    @Column(name = "edited_user_id", length = 8)
    private String editedUserId;
    
    @Column(name = "edited_dtime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date editedDtime;

    // Add trimming setters and getters for all string fields
    public void setAccNbr(String accNbr) {
        this.accNbr = accNbr != null ? accNbr.trim() : null;
    }

    public void setInstId(String instId) {
        this.instId = instId != null ? instId.trim() : null;
    }

    public void setAreaCd(String areaCd) {
        this.areaCd = areaCd != null ? areaCd.trim() : null;
    }

    public void setAddedBlcy(String addedBlcy) {
        this.addedBlcy = addedBlcy != null ? addedBlcy.trim() : null;
    }

    public void setMtrType(String mtrType) {
        this.mtrType = mtrType != null ? mtrType.trim() : null;
    }

    public void setMtrNbr(String mtrNbr) {
        this.mtrNbr = mtrNbr != null ? mtrNbr.trim() : null;
    }

    public void setAcode(String acode) {
        this.acode = acode != null ? acode.trim() : null;
    }

    public void setBillStat(String billStat) {
        this.billStat = billStat != null ? billStat.trim() : null;
    }

    public void setMtrStat(String mtrStat) {
        this.mtrStat = mtrStat != null ? mtrStat.trim() : null;
    }

    public void setRdnStat(String rdnStat) {
        this.rdnStat = rdnStat != null ? rdnStat.trim() : null;
    }

    public void setUserId(String userId) {
        this.userId = userId != null ? userId.trim() : null;
    }

    public void setEditedUserId(String editedUserId) {
        this.editedUserId = editedUserId != null ? editedUserId.trim() : null;
    }

    // Override getters to ensure trimmed values are returned
    public String getAccNbr() {
        return accNbr != null ? accNbr.trim() : null;
    }

    public String getInstId() {
        return instId != null ? instId.trim() : null;
    }

    public String getAreaCd() {
        return areaCd != null ? areaCd.trim() : null;
    }

    public String getAddedBlcy() {
        return addedBlcy != null ? addedBlcy.trim() : null;
    }

    public String getMtrType() {
        return mtrType != null ? mtrType.trim() : null;
    }

    public String getMtrNbr() {
        return mtrNbr != null ? mtrNbr.trim() : null;
    }

    public String getAcode() {
        return acode != null ? acode.trim() : null;
    }

    public String getBillStat() {
        return billStat != null ? billStat.trim() : null;
    }

    public String getMtrStat() {
        return mtrStat != null ? mtrStat.trim() : null;
    }

    public String getRdnStat() {
        return rdnStat != null ? rdnStat.trim() : null;
    }

    public String getUserId() {
        return userId != null ? userId.trim() : null;
    }

    public String getEditedUserId() {
        return editedUserId != null ? editedUserId.trim() : null;
    }
}