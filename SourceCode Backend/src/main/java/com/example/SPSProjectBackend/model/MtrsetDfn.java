package com.example.SPSProjectBackend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "mtrset_dfn")
public class MtrsetDfn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mtr_type", length = 6, nullable = false)
    private String mtrType;

    @Column(name = "cus_cat", length = 1, nullable = false)
    private String cusCat;

    @Column(name = "mtrset_type", nullable = false)
    private Short mtrsetType;

    @Column(name = "mtr_order", nullable = false)
    private Short mtrOrder;
}