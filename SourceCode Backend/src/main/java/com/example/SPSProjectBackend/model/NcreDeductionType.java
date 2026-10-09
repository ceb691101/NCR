package com.example.SPSProjectBackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "ncre_dedcution_type", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NcreDeductionType {

    @Id
    @Column(name = "ded_type_id", length = 4, nullable = false)
    private String dedTypeId;

    @Column(name = "ded_type_nm", length = 60)
    private String dedTypeNm;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "ent_by", length = 12)
    private String entBy;

    @Column(name = "ent_dt")
    @Temporal(TemporalType.DATE)
    private Date entDt;

    @Column(name = "modi_by", length = 12)
    private String modiBy;

    @Column(name = "modi_dt")
    @Temporal(TemporalType.DATE)
    private Date modiDt;
}