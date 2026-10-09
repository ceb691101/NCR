package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;

@Entity
@Table(name = "ncre_bill_cycle", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NcreBillCycle {

    @Id
    @Column(name = "bill_cycle", nullable = false)
    private Short billCycle;

    @Column(name = "bill_year")
    private Short billYear;

    @Column(name = "bill_month")
    private Short billMonth;

    @Column(name = "remarks", length = 100)
    private String remarks;

    @Column(name = "created_by", length = 12)
    private String createdBy;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    @Column(name = "updated_by", length = 12)
    private String updatedBy;

    @Column(name = "is_current")
    private Short isCurrent;

    @Column(name = "recorded_peak_demand")
    private BigDecimal recordedPeakDemand;

    @Column(name = "peak_demand_date")
    private LocalDate peakDemandDate;

    @Column(name = "is_closed")
    private BigDecimal isClosed;

    @Column(name = "closed_by", length = 12)
    private String closedBy;

    @Column(name = "closed_date")
    private LocalDate closedDate;

    public void setRemarks(String remarks) {
        this.remarks = remarks != null ? remarks.trim() : null;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy != null ? createdBy.trim() : null;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy != null ? updatedBy.trim() : null;
    }

    public void setClosedBy(String closedBy) {
        this.closedBy = closedBy != null ? closedBy.trim() : null;
    }
}
