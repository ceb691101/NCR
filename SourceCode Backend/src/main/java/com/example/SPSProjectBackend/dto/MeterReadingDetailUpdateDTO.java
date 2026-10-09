// FILE: src/main/java/com/example/SPSProjectBackend/dto/MeterReadingUpdateDTO.java
package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeterReadingDetailUpdateDTO {
    @JsonProperty("meter_type")
    private String meterType;
    
    @JsonProperty("present_reading")
    private BigDecimal presentReading;
    
    @JsonProperty("units")
    private BigDecimal units;
    
    @JsonProperty("rate")
    private BigDecimal rate;
    
    @JsonProperty("computed_charge")
    private BigDecimal computedCharge;
}