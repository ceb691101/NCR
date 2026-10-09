package com.example.SPSProjectBackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "SPSTDESTDMT")

public class Spstdestdmt {
    @EmbeddedId
    private SpstdestdmtId id;

    @Column(name = "LENGTH")
    private Float length;

    @Column(name = "LINE_COST")
    private Double lineCost;

    @Column(name = "EST_COST")
    private Double estCost;

    @Column(name = "DEPT_ID", length = 10)
    private String deptId;

    @Column(name = "UOM", length = 10)
    private String uom;

    @Column(name = "LINEDES", length = 200)
    private String linedes;

}