package com.example.SPSProjectBackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "ncre_agreements", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agreement_id")
    private Long agreementId;

    @Column(name = "folio_no", nullable = false)
    private Short folioNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folio_no", referencedColumnName = "folio_no", insertable = false, updatable = false)
    @JsonIgnore
    private NcreDeveloper developer;

    @Column(name = "initial_tariff", length = 50)
    private String initialTariff;

    @Column(name = "voltage_level_kv", precision = 10, scale = 3)
    private BigDecimal voltageLevelKv;

    @Column(name = "generation_losses", precision = 5, scale = 2)
    private BigDecimal generationLosses;

    @OneToMany(mappedBy = "agreement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Addendum> addendums;

    @OneToMany(mappedBy = "agreement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentDeduction> paymentDeductions;
}
