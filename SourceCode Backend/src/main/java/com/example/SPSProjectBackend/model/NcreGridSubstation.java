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

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "ncre_grid_substation", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NcreGridSubstation {

    @Id
    @Column(name = "gss_id", nullable = false)
    private BigDecimal gssId;

    @Column(name = "license_code", length = 4, nullable = false)
    private String licenseCode;

    @Column(name = "gss_code", length = 50, nullable = false)
    private String gssCode;

    @Column(name = "gss_name", length = 150, nullable = false)
    private String gssName;

    @Column(name = "nominal_voltage", length = 10)
    private String nominalVoltage;

    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "status", nullable = false)
    private BigDecimal status;

    @Column(name = "created_by", length = 15, nullable = false)
    private String createdBy;

    @Column(name = "created_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date createdDate;

    @Column(name = "updated_by", length = 15)
    private String updatedBy;

    @Column(name = "updated_date")
    @Temporal(TemporalType.DATE)
    private Date updatedDate;

    @Column(name = "order_key")
    private BigDecimal orderKey;
}