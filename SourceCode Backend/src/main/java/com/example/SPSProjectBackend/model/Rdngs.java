package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "rdngs", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(RdngsId.class)
public class Rdngs {
    @Id
    @Column(name = "acc_nbr", length = 10, nullable = false)
    private String accNbr;

    @Id
    @Column(name = "added_blcy", length = 3, nullable = false)
    private String addedBlcy;

    @Id
    @Column(name = "mtr_seq", nullable = false)
    private Short mtrSeq;

    @Id
    @Column(name = "mtr_type", length = 6, nullable = false)
    private String mtrType;

    @Column(name = "prv_date")
    @Temporal(TemporalType.DATE)
    private Date prvDate;

    @Column(name = "rdng_date")
    @Temporal(TemporalType.DATE)
    private Date rdngDate;

    @Column(name = "prv_rdn")
    private Integer prvRdn;

    @Column(name = "rdn")
    private Integer rdn;

    @Column(name = "m_factor", precision = 8, scale = 3)
    private BigDecimal mFactor;

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

    @Column(name = "bill_stat", length = 1)
    private String billStat;

    @Column(name = "err_stat", length = 1)
    private String errStat;

    @Column(name = "mtr_stat", length = 1)
    private String mtrStat;

    @Column(name = "rdn_stat", length = 2)
    private String rdnStat;

    @Column(name = "tariff", length = 3)
    private String tariff;

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
}
