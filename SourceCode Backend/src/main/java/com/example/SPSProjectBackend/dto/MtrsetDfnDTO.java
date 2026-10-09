package com.example.SPSProjectBackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MtrsetDfnDTO {
    private Long id;
    private String mtrType;
    private String cusCat;
    private Short mtrsetType;
    private Short mtrOrder;
}