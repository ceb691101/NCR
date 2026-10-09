package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(
        name = "netmeter",
        indexes = {
                // Primary key is acc_nbr (already unique by @Id)

                // Filter by type of netmeter
                @Index(name = "idx_netmeter_net_type", columnList = "net_type"),

                // Filter by billing cycle
                @Index(name = "idx_netmeter_added_blcy", columnList = "added_blcy"),

                // Composite index for bank lookups
                @Index(name = "idx_netmeter_bank_branch", columnList = "bank_code, bran_code"),

                // Index for agreement date queries
                @Index(name = "idx_netmeter_agrmnt_date", columnList = "agrmnt_date")
        }
)
public class Netmeter {

    @Id
    @Column(name = "acc_nbr", length = 10, nullable = false)
    private String accNbr;

    @Column(name = "bf_units")
    private Integer bfUnits;

    @Column(name = "avg_imp")
    private Integer avgImp;

    @Column(name = "avg_exp")
    private Integer avgExp;

    @Column(name = "net_type", length = 1)
    private String netType;

    @Column(name = "agrmnt_date")
    private Date agrmntDate;

    @Column(name = "rate1", precision = 5, scale = 2)
    private BigDecimal rate1;

    @Column(name = "rate2", precision = 5, scale = 2)
    private BigDecimal rate2;

    @Column(name = "period1")
    private Short period1;

    @Column(name = "period2")
    private Short period2;

    @Column(name = "bank_code", length = 4)
    private String bankCode;

    @Column(name = "bran_code", length = 3)
    private String branCode;

    @Column(name = "bk_ac_no", length = 20)
    private String bkAcNo;

    @Column(name = "gen_cap", precision = 8, scale = 2)
    private BigDecimal genCap;

    @Column(name = "entered_dtime")
    private Timestamp enteredDtime;

    @Column(name = "added_blcy")
    private Short addedBlcy;

    @Column(name = "setoff", length = 1)
    private String setoff;

    @Column(name = "schm", length = 1)
    private String schm;

    @Column(name = "tot_reten", precision = 12, scale = 2)
    private BigDecimal totReten;

    @Column(name = "rate3", precision = 5, scale = 2)
    private BigDecimal rate3;
}
