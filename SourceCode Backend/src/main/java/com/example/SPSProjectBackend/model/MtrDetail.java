package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "mtr_detail")
@IdClass(MtrDetailId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MtrDetail {
    
    @Id
    @Column(name = "inst_id", length = 8)
    private String instId;
    
    @Id
    @Column(name = "added_blcy", length = 3)
    private String addedBlcy;
    
    @Id
    @Column(name = "mtr_type", length = 6)
    private String mtrType;
    
    @Column(name = "mtr_1set")
    private Short mtr1set;
    
    @Column(name = "mtr_2set")
    private Short mtr2set;
    
    @Column(name = "mtr_3set")
    private Short mtr3set;
    
    @Column(name = "mtr_seq")
    private Integer mtrSeq;
    
    @Column(name = "mtrset_type")
    private Short mtrsetType;
    
    @Column(name = "mtr_order")
    private Short mtrOrder;
    
    @Column(name = "no_of_phases", length = 1)
    private String noOfPhases;
    
    @Column(name = "mtr_nbr", length = 10)
    private String mtrNbr;
    
    @Column(name = "prsnt_rdn")
    private Integer prsntRdn;
    
    @Column(name = "ct_ratio", length = 12)
    private String ctRatio;
    
    @Column(name = "mtr_ratio", length = 12)
    private String mtrRatio;
    
    @Column(name = "m_factor", precision = 8, scale = 3)
    private BigDecimal mFactor;
    
    @Column(name = "effct_blcy", length = 3)
    private String effctBlcy;
    
    @Column(name = "effct_date")
    @Temporal(TemporalType.DATE)
    private Date effctDate;
    
    @Column(name = "avg_cnsp_3", precision = 13, scale = 2)
    private BigDecimal avgCnsp3;
    
    @Column(name = "avg_cnsp_6", precision = 13, scale = 2)
    private BigDecimal avgCnsp6;
    
    @Column(name = "avg_cnsp_12", precision = 13, scale = 2)
    private BigDecimal avgCnsp12;
    
    @Column(name = "br_code", length = 2)
    private String brCode;
    
    @Column(name = "user_id", length = 8)
    private String userId;
    
    @Column(name = "entered_dtime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date enteredDtime;
    
    @Column(name = "edited_userid", length = 8)
    private String editedUserid;
    
    @Column(name = "edited_dtime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date editedDtime;
}
