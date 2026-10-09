package com.example.SPSProjectBackend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "PCESTHMT")
public class Pcesthmt {
    @EmbeddedId
    private PcesthmtId id;

    @Column(name = "CAT_CD", length = 20)
    private String catCd;

    @Column(name = "CUST_CONTRIB")
    private Long custContrib;

    @Column(name = "SUB_CONT")
    private Boolean subCont;

    @Column(name = "CONT_NO", length = 20)
    private String contNo;

    @Column(name = "PARTIAL_PMT")
    private Boolean partialPmt;

    @Column(name = "PART_PCNT")
    private Long partPcnt;

    @Column(name = "PARTIAL_AMT")
    private Long partialAmt;

    @Column(name = "TAX_PCNT")
    private Long taxPcnt;

    @Column(name = "TAX_AMT")
    private Long taxAmt;

    @Column(name = "LABEL_2", length = 30)
    private String label2;

    @Column(name = "LABEL_1", length = 30)
    private String label1;

    @Column(name = "TMPL_ID", length = 20)
    private String tmplId;

    @Column(name = "SUP_CD", length = 10)
    private String supCd;

    @Column(name = "LABEL_3", length = 30)
    private String label3;

    @Column(name = "LABEL_4", length = 30)
    private String label4;

    @Column(name = "LABEL_5", length = 30)
    private String label5;

    @Column(name = "LABEL_6", length = 30)
    private String label6;

    @Column(name = "LABEL_7", length = 30)
    private String label7;

    @Column(name = "LABEL_8", length = 30)
    private String label8;

    @Column(name = "LABEL_9", length = 30)
    private String label9;

    @Column(name = "LABEL_10", length = 30)
    private String label10;

    @Column(name = "ETIMATE_DT", nullable = false)
    private LocalDate etimateDt;

    @Column(name = "ACTUAL_UNITS")
    private Long actualUnits;

    @Column(name = "FUND_SOURCE", length = 10)
    private String fundSource;

    @Column(name = "FUND_ID", length = 27)
    private String fundId;

    @Column(name = "STD_COST")
    private Long stdCost;

    @Column(name = "CONTROLLED")
    private Boolean controlled;

    @Column(name = "CLIENT_NM", length = 100)
    private String clientNm;

    @Column(name = "PRIORITY")
    private Boolean priority;

    @Column(name = "REV_REASON", length = 300)
    private String revReason;

    @Column(name = "FUND_CONTRIB")
    private Long fundContrib;

    @Column(name = "STATUS")
    private Short status;

    @Column(name = "LOG_ID")
    private Integer logId;

    @Column(name = "ENT_BY", length = 12)
    private String entBy;

    @Column(name = "ENT_DT", nullable = false)
    private LocalDate entDt;

    @Column(name = "CONF_BY", length = 12)
    private String confBy;

    @Column(name = "CONF_DT", nullable = false)
    private LocalDate confDt;

    @Column(name = "APR_UID2", length = 12)
    private String aprUid2;

    @Column(name = "APR_DT2", nullable = false)
    private LocalDate aprDt2;

    @Column(name = "APR_UID1", length = 12)
    private String aprUid1;

    @Column(name = "APR_DT1", nullable = false)
    private LocalDate aprDt1;

    @Column(name = "APR_UID3", length = 12)
    private String aprUid3;

    @Column(name = "APR_DT3", nullable = false)
    private LocalDate aprDt3;

    @Column(name = "APR_UID4", length = 12)
    private String aprUid4;

    @Column(name = "APR_DT4", nullable = false)
    private LocalDate aprDt4;

    @Column(name = "APR_UID5", length = 12)
    private String aprUid5;

    @Column(name = "APR_DT5", nullable = false)
    private LocalDate aprDt5;

    @Column(name = "REJCT_UID", length = 12)
    private String rejctUid;

    @Column(name = "REJCT_DT", nullable = false)
    private LocalDate rejctDt;

    @Column(name = "SETTLED_AMT")
    private Long settledAmt;

    @Column(name = "ALLOC_SETTLE")
    private Long allocSettle;

    @Column(name = "PAID_AMT")
    private Long paidAmt;

    @Column(name = "ALLOC_PAID")
    private Long allocPaid;

    @Column(name = "PROJECT_NO", nullable = false, length = 20)
    private String projectNo;

    @Column(name = "NORM_DEFAULT")
    private Boolean normDefault;

    @Column(name = "REVISE_EST")
    private Long reviseEst;

    @Column(name = "EST_TYPE")
    private Boolean estType;

    @Column(name = "REVISE_UID", length = 12)
    private String reviseUid;

    @Column(name = "REVISE_DT", nullable = false)
    private LocalDate reviseDt;

    @Column(name = "DESCR", length = 2000)
    private String descr;

    @Column(name = "PRJ_ASS_DT")
    private LocalDate prjAssDt;

    @Column(name = "REJECT_REASON", length = 500)
    private String rejectReason;

    @Column(name = "OMS_REF_NO", length = 22)
    private String omsRefNo;

    @Column(name = "ESTIMATEDYEAR", length = 4)
    private String estimatedyear;

    @Column(name = "ESTIMATED_YEAR", length = 6)
    private String estimatedYear;

    @Column(name = "LB_RATE_YEAR", length = 7)
    private String lbRateYear;

    @Column(name = "FUND_COST")
    private Long fundCost;

    @Column(name = "SEC_DEP_YEAR", length = 6)
    private String secDepYear;

    @OneToMany(mappedBy = "pcesthmt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pcestdmt> details;
}