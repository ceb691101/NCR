package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoice_tariff_charge_lines", schema = "appadm1")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceTariffChargeLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "line_sequence", nullable = false)
    private Integer lineSequence;

    @Column(name = "label", length = 100, nullable = false)
    private String label;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "number_of_days")
    private Integer numberOfDays;

    @Column(name = "energy_kwh", precision = 15, scale = 2)
    private BigDecimal energyKwh;

    @Column(name = "rate_per_kwh", precision = 10, scale = 4)
    private BigDecimal ratePerKwh;

    @Column(name = "cost_of_energy", precision = 15, scale = 2)
    private BigDecimal costOfEnergy;

    @Column(name = "rate_type", length = 20)
    private String rateType;
}
