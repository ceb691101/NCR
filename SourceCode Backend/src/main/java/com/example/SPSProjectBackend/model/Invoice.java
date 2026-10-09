package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "invoice_number", length = 50, nullable = false, unique = true)
    private String invoiceNumber;

    @Column(name = "account_number", length = 10, nullable = false)
    private String accountNumber;

    @Column(name = "area_code", length = 2, nullable = false)
    private String areaCode;

    @Column(name = "bill_cycle", nullable = false)
    private Integer billCycle;

    @Column(name = "invoice_month", length = 20, nullable = false)
    private String invoiceMonth;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "region", length = 50)
    private String region;

    @Column(name = "folio_no")
    private Integer folioNo;

    @Column(name = "sr_no", length = 50)
    private String srNo;

    @Column(name = "company_name", length = 255, nullable = false)
    private String companyName;

    @Column(name = "project_name", length = 255, nullable = false)
    private String projectName;

    @Column(name = "file_ref_no", length = 50)
    private String fileRefNo;

    @Column(name = "reference_code", length = 100)
    private String referenceCode;

    @Column(name = "capacity_mw", precision = 10, scale = 3, nullable = false)
    private BigDecimal capacityMw;

    @Column(name = "allowed_generation_mw", precision = 10, scale = 3, nullable = false)
    private BigDecimal allowedGenerationMw;

    @Column(name = "present_reading_date", nullable = false)
    private LocalDate presentReadingDate;

    @Column(name = "previous_reading_date", nullable = false)
    private LocalDate previousReadingDate;

    @Column(name = "total_present_reading", nullable = false)
    private Integer totalPresentReading;

    @Column(name = "total_previous_reading", nullable = false)
    private Integer totalPreviousReading;

    @Column(name = "multiply_factor", precision = 8, scale = 3)
    private BigDecimal multiplyFactor;

    @Column(name = "energy_kwh", precision = 15, scale = 2, nullable = false)
    private BigDecimal energyKwh;

    @Column(name = "eligible_energy_kwh", precision = 15, scale = 2, nullable = false)
    private BigDecimal eligibleEnergyKwh;

    @Column(name = "period_of_generation_days", nullable = false)
    private Integer periodOfGenerationDays;

    @Column(name = "plant_factor_percent", precision = 5, scale = 2, nullable = false)
    private BigDecimal plantFactorPercent;

    @Column(name = "energy_purchased_kwh", precision = 15, scale = 2, nullable = false)
    private BigDecimal energyPurchasedKwh;

    @Column(name = "rate_per_kwh", precision = 7, scale = 2, nullable = false)
    private BigDecimal ratePerKwh;

    @Column(name = "cost_of_energy", precision = 15, scale = 2, nullable = false)
    private BigDecimal costOfEnergy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private InvoiceStatus status;

    @Column(name = "prepared_by", length = 50, nullable = false)
    private String preparedBy;

    @Transient
    private String preparedByName;

    @Transient
    private String preparedByTitle;

    @Column(name = "prepared_at")
    private LocalDateTime preparedAt;

    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "loyalty", precision = 15, scale = 2)
    private BigDecimal loyalty;

    @Column(name = "escrow", precision = 15, scale = 2)
    private BigDecimal escrow;

    @Column(name = "mahaweli", precision = 15, scale = 2)
    private BigDecimal mahaweli;

    @Column(name = "treasury", precision = 15, scale = 2)
    private BigDecimal treasury;

    @Column(name = "ded_1_perc", precision = 15, scale = 8)
    private BigDecimal ded1Perc;

    @Column(name = "developer_pymnt", precision = 15, scale = 2)
    private BigDecimal developerPymnt;

    @Column(name = "energy_after_gen_loss", precision = 15, scale = 2)
    private BigDecimal energyAfterGenLoss;

    @Column(name = "eng_send_r1", precision = 20, scale = 8)
    private BigDecimal engSendR1;

    @Column(name = "eng_send_r2", precision = 20, scale = 8)
    private BigDecimal engSendR2;

    @Column(name = "eng_send_r3", precision = 20, scale = 8)
    private BigDecimal engSendR3;

    @Transient
    private String remarks;

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("responsible_ee")
    private String responsibleEe;
}
