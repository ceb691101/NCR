package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(
        name = "loan_master",
        indexes = {

                // Common filters
                @Index(name = "idx_loan_master_loan_type", columnList = "loan_type"),
                @Index(name = "idx_loan_master_active_st", columnList = "active_st"),

                // User tracking
                @Index(name = "idx_loan_master_entered_user", columnList = "entered_user"),
                @Index(name = "idx_loan_master_edited_user", columnList = "edited_user"),

                // Composite index
                @Index(name = "idx_loan_master_type_active", columnList = "loan_type, active_st")
        }
)
public class LoanMaster {

    @Id
    @Column(name = "acc_nbr", length = 10, nullable = false)
    private String accNbr;

    @Column(name = "loan_type", length = 3)
    private String loanType;

    @Column(name = "loan_amt", precision = 12, scale = 2)
    private BigDecimal loanAmt;

    @Column(name = "no_months")
    private Short noMonths;

    @Column(name = "st_bill_cycle")
    private Short stBillCycle;

    @Column(name = "end_bill_cycle")
    private Short endBillCycle;

    @Column(name = "int_rate", precision = 5, scale = 2)
    private BigDecimal intRate;

    @Column(name = "active_st", length = 1)
    private String activeSt;

    @Column(name = "mon_pmnt", precision = 12, scale = 2)
    private BigDecimal monPmnt;

    @Column(name = "entered_user", length = 10)
    private String enteredUser;

    @Column(name = "edited_user", length = 10)
    private String editedUser;
}
