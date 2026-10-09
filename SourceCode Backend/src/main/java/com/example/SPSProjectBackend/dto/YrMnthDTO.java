package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class YrMnthDTO {
    
    @JsonProperty("bill_cycle")
    private Integer billCycle;
    
    @JsonProperty("bill_mnth")
    private String billMnth;
}