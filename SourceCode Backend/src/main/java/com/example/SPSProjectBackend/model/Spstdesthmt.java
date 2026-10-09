package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "SPSTDESTHMT")
public class Spstdesthmt {
    @EmbeddedId
    private SpstdesthmtId id;

    @Column(name = "JOBNAME", length = 150)
    private String jobname;

    @Column(name = "CONTINGENCY", length = 20)
    private String contingency;

    @Column(name = "DESCRIPTION", length = 150)
    private String description;

    @Column(name = "TOTAL_COST")
    private Double totalCost;

    @Column(name = "SECDEPOSIT")
    private Float secdeposit;

    @Column(name = "PROJECTTYPE", length = 30)
    private String projecttype;

    @Column(name = "STATUS")
    private Long status;

    @Column(name = "ENTRY_BY", length = 20)
    private String entryBy;

    @Column(name = "ENTRY_DATE")
    private LocalDate entryDate;

    @Column(name = "VALIDATE_BY_EE", length = 20)
    private String validateByEe;

    @Column(name = "VALIDATE_DATE_EE")
    private LocalDate validateDateEe;

    @Column(name = "APPROVED_BY", length = 20)
    private String approvedBy;

    @Column(name = "REJECTED_BY", length = 20)
    private String rejectedBy;

    @Column(name = "REJECTED_DATE")
    private LocalDate rejectedDate;

    @Column(name = "REJ_REASON_EE", length = 200)
    private String rejReasonEe;

    @Column(name = "REJ_RESON_CE", length = 100)
    private String rejResonCe;

    @Column(name = "PLANING_BY", length = 20)
    private String planingBy;

    @Column(name = "PLANING_DATE")
    private LocalDate planingDate;

    @Column(name = "POST_DEPTID", length = 10)
    private String postDeptid;

    @Column(name = "REBATE_COST")
    private Float rebateCost;

    @Column(name = "CEBCOST")
    private Float cebcost;

    @Column(name = "TOCONPAY")
    private Double toconpay;

    @Column(name = "CON_COST")
    private Float conCost;

    @Column(name = "SINNO", length = 20)
    private String sinno;

    @Column(name = "VALIDATE_DATE_CE")
    private LocalDate validateDateCe;

    @Column(name = "REJ_RESON_PE", length = 200)
    private String rejResonPe;

    @Column(name = "VALIDATE_BY_CE", length = 20)
    private String validateByCe;

    @Column(name = "APPROVED_DATE")
    private LocalDate approvedDate;

    @Column(name = "VAT")
    private Float vat;

    @Column(name = "ASSIGNEDTO", length = 30)
    private String assignedto;

    @Column(name = "COMMENT_PE", length = 500)
    private String commentPe;

    @Column(name = "COMMENT_EE", length = 500)
    private String commentEe;

    @Column(name = "COMMENT_CE", length = 500)
    private String commentCe;

    @Column(name = "COMMENT_PCE", length = 500)
    private String commentPce;

    @Column(name = "COMMENT_DGM", length = 500)
    private String commentDgm;

    @Column(name = "COMMENT_ES", length = 500)
    private String commentEs;

    @Column(name = "SINNO1", length = 20)
    private String sinno1;

    @Column(name = "SINNO2", length = 20)
    private String sinno2;

    @Column(name = "SINNO3", length = 20)
    private String sinno3;

    @Column(name = "SINNO4", length = 20)
    private String sinno4;

    @Column(name = "LINE_LENGTH")
    private Float lineLength;

    @Column(name = "NBTCOST")
    private Double nbtcost;

    @Column(name = "VATCOST")
    private Float vatcost;

    @Column(name = "NBTPERCENTAGE")
    private Float nbtpercentage;

    @Column(name = "EX_CAPACITY")
    private Float exCapacity;

    @Column(name = "NEW_CAPACITY")
    private Float newCapacity;

    @Column(name = "BAL_CAPACITY")
    private Float balCapacity;

    @Column(name = "SCTCOST")
    private Long sctcost;

    @Column(name = "LOANPERCENTAGE")
    private Float loanpercentage;

    @Column(name = "LOANAMOUNT")
    private Float loanamount;

    // ==== Add transient getters and setters for embedded ID fields ==== //

    @Transient
    public String getAppNo() {
        return id != null ? id.getAppNo() : null;
    }

    public void setAppNo(String appNo) {
        if (id == null) {
            id = new SpstdesthmtId();
        }
        id.setAppNo(appNo);
    }

    @Transient
    public String getStdNo() {
        return id != null ? id.getStdNo() : null;
    }

    public void setStdNo(String stdNo) {
        if (id == null) {
            id = new SpstdesthmtId();
        }
        id.setStdNo(stdNo);
    }

    @Transient
    public String getDeptId() {
        return id != null ? id.getDeptId() : null;
    }

    public void setDeptId(String deptId) {
        if (id == null) {
            id = new SpstdesthmtId();
        }
        id.setDeptId(deptId);
    }
}