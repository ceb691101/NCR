package com.example.SPSProjectBackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillCycleConfigId implements Serializable {
    private Integer billCycle;
    private String areaCode;
}