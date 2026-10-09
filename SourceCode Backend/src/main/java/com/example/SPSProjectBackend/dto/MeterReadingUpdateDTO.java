// FILE: src/main/java/com/example/SPSProjectBackend/dto/MeterReadingUpdateDTO.java
package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeterReadingUpdateDTO {
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("account_number")
    private String accountNumber;
    
    @JsonProperty("area_code")
    private String areaCode;
    
    @JsonProperty("bill_cycle")
    private String billCycle;
    
    @JsonProperty("reading_date")
    private Date readingDate;
    
    @JsonProperty("fixed_charge")
    private BigDecimal fixedCharge;
    
    @JsonProperty("monthly_charge")
    private BigDecimal monthlyCharge;
    
    @JsonProperty("vat_amount")
    private BigDecimal vatAmount;
    
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;
    
    @JsonProperty("meter_readings")
    private List<MeterReadingDetailUpdateDTO> meterReadings;
}