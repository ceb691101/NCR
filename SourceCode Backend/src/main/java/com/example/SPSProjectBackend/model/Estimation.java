//package com.example.SPSProjectBackend.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.util.Date;
//
//@Entity
//@Getter
//@Setter
//@Table(name = "SPS_ESTIMATION", schema = "SPS")
//public class Estimation {
//
//    @Id
//    @Column(name = "APP_NO", length = 50, nullable = false)
//    private String appNo;
//
//    @Id
//    @Column(name = "STD_NO", length = 50, nullable = false)
//    private String stdNo;
//
//    @Id
//    @Column(name = "DEPT_ID", length = 20, nullable = false)
//    private String deptId;
//
//    @Column(name = "JOBNAME", length = 150)
//    private String jobname;
//
//    @Column(name = "CONTINGENCY", length = 20)
//    private String CONTINGENCY;
//
//    @Column(name = "DESCRIPTION", length = 150)
//    private String description;
//
//    @Column(name = "TOTAL_COST", length = 30)
//    private Double totalCost;
//
//    @Column(name = "SECDEPOSIT", length = 20)
//    private Double secdeposit;
//
//    @Column(name = "PROJECTTYPE", length = 30)
//    private String projecttype;
//
//    @Column(name = "STATUS", length = 22)
//    private Integer status;
//
//    @Column(name = "ENTRY_BY", length = 20)
//    private String entryBy;
//
//    @Column(name = "ENTRY_DATE")
//    @Temporal(TemporalType.DATE)
//    private Date entryDate;
//
//    @Column(name = "VALIDATE_BY_EE", length = 20)
//    private String validateByEe;
//
//    @Column(name = "VALIDATE_DATE_EE")
//    @Temporal(TemporalType.DATE)
//    private Date validateDateEe;
//
//    @Column(name = "APPROVED_BY", length = 20)
//    private String approvedBy;
//
//    @Column(name = "REJECTED_BY ", length = 20)
//    private String rejectedBy;
//
//    @Column(name = "REJECTED_DATE")
//    @Temporal(TemporalType.DATE)
//    private Date rejectedDate;
//
//    @Column(name = "REJECTED_REASON_EE", length = 200)
//    private String rejReasonEe;
//
//    @Column(name = "REJECTED_REASON_CE", length = 100)
//    private String rejResonCe;
//
//    @Column(name = "PLANING_BY", length = 20)
//    private String planingBy;
//
//    @Column(name = "PLANING_DATE")
//    @Temporal(TemporalType.DATE)
//    private Date planingDate;
//
//    @Column(name = "POST_DEPTID", length = 10)
//    private String postDeptid;
//
//    @Column(name = "REBATE_COST", length = 20)
//    private Double rebateCost;
//
//    @Column(name = "CEBCOST", length = 20)
//    private Double CEBCOST;
//
//    @Column(name = "TOCONPAY", length = 30)
//    private Double TOCONPAY;
//
//    @Column(name = "CON_COST", length = 20)
//    private Double conCost;
//
//    @Column(name = "SINNO", length = 20)
//    private String SINNO;
//
//    @Column(name = "ADD_DATE")
//    @Temporal(TemporalType.DATE)
//    private Date validateDateCe;
//
//    @Column(name = "REJ_RESON_PE ", length = 200)
//    private String rejResonPe;
//
//    @Column(name = "VALIDATE_BY_CE", length = 20)
//    private String validateByCe;
//
//    @Column(name = "APPROVED_DATE")
//    @Temporal(TemporalType.DATE)
//    private Date approvedDate;
//
//    @Column(name = "VAT", length = 20)
//    private Double vat;
//
//    @Column(name = "ASSIGNEDTO", length = 30)
//    private String assignedto;
//
//    @Column(name = "COMMENT_PE", length = 500)
//    private String commentPe;
//
//    @Column(name = "COMMENT_EE", length = 500)
//    private String commentEe;
//
//    @Column(name = "COMMENT_CE", length = 500)
//    private String commentCe;
//
//    @Column(name = "COMMENT_PCE", length = 500)
//    private String commentPce;
//
//    @Column(name = "COMMENT_DGM", length = 500)
//    private String commentDgm;
//
//    @Column(name = "COMMENT_ES", length = 500)
//    private String commentEs;
//
//    @Column(name = "SINNO1", length = 20)
//    private String sinno1;
//
//    @Column(name = "SINNO2", length = 20)
//    private String sinno2;
//
//    @Column(name = "SINNO3", length = 20)
//    private String sinno3;
//
//    @Column(name = "SINNO4", length = 20)
//    private String sinno4;
//
//    @Column(name = "LINE_LENGTH", length = 8)
//    private Double lineLength;
//
//    @Column(name = "NBTCOST", length = 30)
//    private Double nbtcost;
//
//    @Column(name = "VATCOST", length = 20)
//    private Double vatcost;
//
//    @Column(name = "NBTPERCENTAGE", length = 8)
//    private Double nbtpercentage;
//
//    @Column(name = "EX_CAPACITY", length = 20)
//    private Double exCapacity;
//
//    @Column(name = "NEW_CAPACITY", length = 20)
//    private Double newCapacity;
//
//    @Column(name = "BAL_CAPACITY", length = 20)
//    private Double balCapacity;
//
//    @Column(name = "SCTCOST", length = 67)
//    private Double sctcost;
//
//    @Column(name = "LOANPERCENTAGE", length = 8)
//    private Double loanpercentage;
//
//    @Column(name = "LOANAMOUNT", length = 8)
//    private Double loanamount;
//}
