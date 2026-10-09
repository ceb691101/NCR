package com.example.SPSProjectBackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ncre_tariff_desc", schema = "dbadmin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NcreTariffDescription {

    @Id
    @Column(name = "tariff_code", length = 7, nullable = false)
    private String tariffCode;

    @Column(name = "cus_cat", length = 1)
    private String cusCat;

    @Column(name = "tariff_desc", length = 30)
    private String tariffDesc;

    @Column(name = "status", length = 1)
    private String status;
}
