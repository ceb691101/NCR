package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "tmp_mon_tot")
@IdClass(TmpMonTotId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmpMonTot {
    
    @Id
    @Column(name = "acc_nbr", length = 10, nullable = false)
    private String accNbr;
    
    @Id
    @Column(name = "bill_cycle", length = 3, nullable = false)
    private String billCycle;
    
    @Column(name = "new_redercd", length = 2)
    private String newRederCd;
    
    @Column(name = "new_dlypack", length = 2)
    private String newDlyPack;
    
    @Column(name = "new_wlkord", length = 6)
    private String newWlkOrd;
    
    @Column(name = "err_stat")
    private Integer errStat;
    
    @Column(name = "tot_untskwo", precision = 10, scale = 2)
    private BigDecimal totUntsKwo;
    
    @Column(name = "tot_untskwd", precision = 10, scale = 2)
    private BigDecimal totUntsKwd;
    
    @Column(name = "tot_untskwp", precision = 10, scale = 2)
    private BigDecimal totUntsKwp;
    
    @Column(name = "tot_kva", precision = 10, scale = 2)
    private BigDecimal totKva;
    
    @Column(name = "tot_compchg", precision = 13, scale = 2)
    private BigDecimal totCompChg;
    
    @Column(name = "tot_charge", precision = 13, scale = 2)
    private BigDecimal totCharge;
    
    @Column(name = "fixed_chg", precision = 6, scale = 2)
    private BigDecimal fixedChg;
    
    @Column(name = "tot_gst", precision = 13, scale = 2)
    private BigDecimal totGst;
    
    @Column(name = "tot_amt", precision = 15, scale = 2)
    private BigDecimal totAmt;
    
    @Column(name = "crnt_bal", precision = 13, scale = 2)
    private BigDecimal crntBal;
    
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