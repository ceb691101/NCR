package com.example.SPSProjectBackend.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SpstdestdmtDto {
    @Id
    private String stdNo;
    private String lineType;
    private Float length;
    private Double lineCost;
    private Double estCost;
    private String deptId;
    private String uom;
    private String linedes;
}
