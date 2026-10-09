package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "ncre_inv_rdngs", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(NcreInvRdngsId.class)
public class NcreInvRdngs {

    @Id
    @Column(name = "acc_nbr", length = 10, nullable = false)
    private String accNbr;

    @Id
    @Column(name = "area_cd", length = 2, nullable = false)
    private String areaCd;

    @Id
    @Column(name = "added_blcy", length = 3, nullable = false)
    private String addedBlcy;

    @Column(name = "rdng_date")
    @Temporal(TemporalType.DATE)
    private Date rdngDate;

    @Id
    @Column(name = "mtr_nbr", length = 10, nullable = false)
    private String mtrNbr;

    @Column(name = "kwh_tot", precision = 20, scale = 3)
    private BigDecimal kwhTot;

    @Column(name = "kwh_r1", precision = 20, scale = 3)
    private BigDecimal kwhR1;

    @Column(name = "kwh_r2", precision = 20, scale = 3)
    private BigDecimal kwhR2;

    @Column(name = "kwh_r3", precision = 20, scale = 3)
    private BigDecimal kwhR3;

    @Column(name = "max_dmnd", precision = 20, scale = 3)
    private BigDecimal maxDmnd;

    @Column(name = "max_dmnd_exp", precision = 20, scale = 3)
    private BigDecimal maxDmndExp;

    @Column(name = "kwh_exp_tot", precision = 20, scale = 3)
    private BigDecimal kwhExpTot;

    @Column(name = "kwh_r1_exp", precision = 20, scale = 3)
    private BigDecimal kwhR1Exp;

    @Column(name = "kwh_r2_exp", precision = 20, scale = 3)
    private BigDecimal kwhR2Exp;

    @Column(name = "kwh_r3_exp", precision = 20, scale = 3)
    private BigDecimal kwhR3Exp;

    @Column(name = "kvarh_tot", precision = 20, scale = 3)
    private BigDecimal kvarhTot;

    @Column(name = "kvarh_r1", precision = 20, scale = 3)
    private BigDecimal kvarhR1;

    @Column(name = "kvarh_r2", precision = 20, scale = 3)
    private BigDecimal kvarhR2;

    @Column(name = "kvarh_r3", precision = 20, scale = 3)
    private BigDecimal kvarhR3;

    @Column(name = "kvarh_exp_tot", precision = 20, scale = 3)
    private BigDecimal kvarhExpTot;

    @Column(name = "kvarh_r1_exp", precision = 20, scale = 3)
    private BigDecimal kvarhR1Exp;

    @Column(name = "kvarh_r2_exp", precision = 20, scale = 3)
    private BigDecimal kvarhR2Exp;

    @Column(name = "kvarh_r3_exp", precision = 20, scale = 3)
    private BigDecimal kvarhR3Exp;

    @Column(name = "mtr_stat", length = 2)
    private String mtrStat;

    @Column(name = "rdn_stat", length = 2)
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

    @Column(name = "flag", length = 1)
    private String flag;

    @Column(name = "rdng_profile", length = 1)
    private String rdngProfile;

    @Column(name = "m_factor", precision = 8, scale = 3)
    private BigDecimal mFactor;

    // Setter overrides to trim char inputs
    public void setAccNbr(String accNbr) {
        this.accNbr = accNbr != null ? accNbr.trim() : null;
    }

    public void setAreaCd(String areaCd) {
        this.areaCd = areaCd != null ? areaCd.trim() : null;
    }

    public void setAddedBlcy(String addedBlcy) {
        this.addedBlcy = addedBlcy != null ? addedBlcy.trim() : null;
    }

    public void setMtrNbr(String mtrNbr) {
        this.mtrNbr = mtrNbr != null ? mtrNbr.trim() : null;
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

    public void setFlag(String flag) {
        this.flag = flag != null ? flag.trim() : null;
    }

    public void setRdngProfile(String rdngProfile) {
        this.rdngProfile = rdngProfile != null ? rdngProfile.trim() : null;
    }
}
