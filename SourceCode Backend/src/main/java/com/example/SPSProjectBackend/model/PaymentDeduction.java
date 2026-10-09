package com.example.SPSProjectBackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "ncre_payment_deductions", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDeduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deduction_id")
    private Long deductionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agreement_id", nullable = false)
    @JsonIgnore
    private Agreement agreement;

    @Column(name = "deduction_type", length = 100)
    private String deductionType;

    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;
}
