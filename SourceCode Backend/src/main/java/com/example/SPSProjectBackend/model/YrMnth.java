package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
        name = "yr_mnth",
        indexes = {
                @Index(name = "yr_mnth_bill", columnList = "bill_cycle")
        }
)
public class YrMnth {

    @Id
    @Column(name = "bill_cycle")
    private Integer billCycle;

    @Column(name = "bill_mnth", length = 20)
    private String billMnth;
}