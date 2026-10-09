package com.example.SPSProjectBackend.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "STANDARD_RATES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandardRates {

    @Id
    @Column(name = "LINESECTIONTYPEID", length = 150, nullable = false)
    private String linesectiontypeid;

    @Column(name = "UOM", length = 20)
    private String uom;

    @Column(name = "STANDARDCOST", length = 20)
    private Double standardcost;

    @Column(name = "DESCRIPTION", length = 200)
    private String description;

    @Column(name = "LINEPARENTID", length = 20)
    private String lineparentid;
}

