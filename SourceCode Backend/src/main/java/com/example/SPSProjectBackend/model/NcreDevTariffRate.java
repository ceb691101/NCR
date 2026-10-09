package com.example.SPSProjectBackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "ncre_dev_year_tariff_rates", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NcreDevTariffRate {

    @Id
    @Column(name = "folio_no", nullable = false)
    private Short folioNo;

    @Column(name = "techno_type", length = 20)
    private String technoType;

    @Column(name = "tariff_code", length = 20)
    private String tariffCode;

    @Column(name = "prv_tariff_rate", precision = 5, scale = 2)
    private BigDecimal prvTariffRate;

    @Column(name = "cur_tariff_rate", precision = 5, scale = 2)
    private BigDecimal curTariffRate;

    @Column(name = "tariff_changed")
    @Temporal(TemporalType.DATE)
    private Date tariffChanged;

    @Column(name = "year")
    private Integer year;

    @Column(name = "responsble_ee", length = 100)
    private String responsibleEe;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "ent_by", length = 12)
    private String entBy;

    @Column(name = "ent_dt")
    @Temporal(TemporalType.DATE)
    private Date entDt;

    @Column(name = "validate_by", length = 12)
    private String validateBy;

    @Column(name = "validate_dt")
    @Temporal(TemporalType.DATE)
    private Date validateDt;

    @Column(name = "approve_by", length = 12)
    private String approveBy;

    @Column(name = "approve_dt")
    @Temporal(TemporalType.DATE)
    private Date approveDt;

    @Column(name = "change_time_per_year", length = 1)
    private String changeTimesPerYear;
}
