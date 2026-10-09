package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(
        name = "mon_tot",
        indexes = {
                // Non-unique index on bill_cycle for faster filtering
                @Index(name = "index_mon_tot_1", columnList = "bill_cycle"),

                // Composite index on acc_nbr + bill_cycle (non-unique, since PK already enforces uniqueness)
                @Index(name = "indx_mon_tot", columnList = "acc_nbr, bill_cycle")
        }
)
@IdClass(MonTotId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonTot {
    
    @Id
    @Column(name = "acc_nbr", length = 10, nullable = false)
    private String accNbr;
    
    @Id
    @Column(name = "bill_cycle", length = 3, nullable = false)
    private String billCycle;
    
    @Column(name = "err_stat", length = 1)
    private String errStat;
    
    @Column(name = "bf_bal", precision = 17, scale = 2)
    private BigDecimal bfBal;
    
    @Column(name = "tot_untskwo", precision = 10, scale = 2)
    private BigDecimal totUntsKwo;
    
    @Column(name = "tot_untskwd", precision = 10, scale = 2)
    private BigDecimal totUntsKwd;
    
    @Column(name = "tot_untskwp", precision = 10, scale = 2)
    private BigDecimal totUntsKwp;
    
    @Column(name = "tot_kva", precision = 10, scale = 2)
    private BigDecimal totKva;
    
    @Column(name = "tot_kwochg", precision = 15, scale = 2)
    private BigDecimal totKwoChg;
    
    @Column(name = "tot_kwdchg", precision = 15, scale = 2)
    private BigDecimal totKwdChg;
    
    @Column(name = "tot_kwpchg", precision = 15, scale = 2)
    private BigDecimal totKwpChg;
    
    @Column(name = "tot_kvachg", precision = 15, scale = 2)
    private BigDecimal totKvaChg;
    
    @Column(name = "tot_charge", precision = 17, scale = 2)
    private BigDecimal totCharge;
    
    @Column(name = "fixed_chg", precision = 6, scale = 2)
    private BigDecimal fixedChg;
    
    @Column(name = "tot_gst", precision = 13, scale = 2)
    private BigDecimal totGst;
    
    @Column(name = "tot_amt", precision = 17, scale = 2)
    private BigDecimal totAmt;
    
    @Column(name = "debt_tot", precision = 15, scale = 2)
    private BigDecimal debtTot;
    
    @Column(name = "crdt_tot", precision = 15, scale = 2)
    private BigDecimal crdtTot;
    
    @Column(name = "pay_tot", precision = 17, scale = 2)
    private BigDecimal payTot;
    
    @Column(name = "crnt_bal", precision = 17, scale = 2)
    private BigDecimal crntBal;
    
    @Column(name = "kwo_avg_3", precision = 13, scale = 2)
    private BigDecimal kwoAvg3;
    
    @Column(name = "kwo_avg_6", precision = 13, scale = 2)
    private BigDecimal kwoAvg6;
    
    @Column(name = "kwo_avg_12", precision = 13, scale = 2)
    private BigDecimal kwoAvg12;
    
    @Column(name = "kwd_avg_3", precision = 13, scale = 2)
    private BigDecimal kwdAvg3;
    
    @Column(name = "kwd_avg_6", precision = 13, scale = 2)
    private BigDecimal kwdAvg6;
    
    @Column(name = "kwd_avg_12", precision = 13, scale = 2)
    private BigDecimal kwdAvg12;
    
    @Column(name = "kwp_avg_3", precision = 13, scale = 2)
    private BigDecimal kwpAvg3;
    
    @Column(name = "kwp_avg_6", precision = 13, scale = 2)
    private BigDecimal kwpAvg6;
    
    @Column(name = "kwp_avg_12", precision = 13, scale = 2)
    private BigDecimal kwpAvg12;
    
    @Column(name = "kva_avg_3", precision = 13, scale = 2)
    private BigDecimal kvaAvg3;
    
    @Column(name = "kva_avg_6", precision = 13, scale = 2)
    private BigDecimal kvaAvg6;
    
    @Column(name = "kva_avg_12", precision = 13, scale = 2)
    private BigDecimal kvaAvg12;
    
    @Column(name = "tot_bill_stat", length = 1)
    private String totBillStat;
    
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