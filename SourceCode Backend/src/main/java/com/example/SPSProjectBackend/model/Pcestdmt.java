package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "PCESTDMT")
public class Pcestdmt {
    @EmbeddedId
    private PcestdmtId id;

    @Column(name = "RES_TYPE", length = 20)
    private String resType;

    @Column(name = "RES_CAT")
    private Long resCat;

    @Column(name = "GEN_RES")
    private Boolean genRes;

    @Column(name = "UOM", length = 4)
    private String uom;

    @Column(name = "UNIT_PRICE")
    private Long unitPrice;

    @Column(name = "ESTIMATE_QTY")
    private Long estimateQty;

    @Column(name = "ESTIMATE_COST")
    private Long estimateCost;

    @Column(name = "TOLERANCE")
    private Long tolerance;

    @Column(name = "ISSUED_QTY")
    private Long issuedQty;

    @Column(name = "COMMITED_QTY")
    private Long commitedQty;

    @Column(name = "COMMITED_COST")
    private Long commitedCost;

    @Column(name = "ISSUED_COST")
    private Long issuedCost;

    @Column(name = "NORM_DEFAULT")
    private Boolean normDefault;

    @Column(name = "REQUESTED_QTY")
    private Long requestedQty;

    @Column(name = "REQUESTED_COST")
    private Long requestedCost;

    @Column(name = "APPROVED_QTY")
    private Long approvedQty;

    @Column(name = "APPROVED_COST")
    private Long approvedCost;

    @Column(name = "RETURNED_QTY")
    private Long returnedQty;

    @Column(name = "RETURNED_COST")
    private Long returnedCost;

    @Column(name = "CUSTOMER_QTY")
    private Long customerQty;

    @Column(name = "DAMAGE_QTY")
    private Long damageQty;

    @Column(name = "MNT_QTY")
    private Long mntQty;

    @Column(name = "FUND_QTY")
    private Long fundQty;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "ESTIMATE_NO", referencedColumnName = "ESTIMATE_NO", insertable = false, updatable = false),
            @JoinColumn(name = "REV_NO", referencedColumnName = "REV_NO", insertable = false, updatable = false),
            @JoinColumn(name = "DEPT_ID", referencedColumnName = "DEPT_ID", insertable = false, updatable = false)
    })
    private Pcesthmt pcesthmt;
}