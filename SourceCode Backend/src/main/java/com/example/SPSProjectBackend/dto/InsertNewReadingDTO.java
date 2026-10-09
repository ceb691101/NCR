// FILE: src/main/java/com/example/SPSProjectBackend/dto/InsertNewReadingDTO.java
package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class InsertNewReadingDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InsertNewReadingRequest {
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
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date readingDate;
        
        @JsonProperty("meter_readings")
        private List<MeterReadingInsertDTO> meterReadings;
        
        @JsonProperty("charges")
        private ChargesInsertDTO charges;
        
        @JsonProperty("meter_sequence")
        private Integer meterSequence;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterReadingInsertDTO {
        @JsonProperty("meter_type")
        private String meterType; // KWO, KWD, KWP, KVA, KVAH
        
        @JsonProperty("meter_number")
        private String meterNumber;
        
        @JsonProperty("present_reading")
        private BigDecimal presentReading;
        
        @JsonProperty("previous_reading")
        private BigDecimal previousReading;
        
        @JsonProperty("units")
        private BigDecimal units;
        
        @JsonProperty("assessed_code")
        private String assessedCode; // "A" for Assessed, null for Not-Assessed
        
        @JsonProperty("multiplied_by")
        private BigDecimal multipliedBy;
        
        @JsonProperty("rate")
        private BigDecimal rate;
        
        @JsonProperty("amount")
        private BigDecimal amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChargesInsertDTO {
        @JsonProperty("fixed_charge")
        private BigDecimal fixedCharge;
        
        @JsonProperty("monthly_charge")
        private BigDecimal monthlyCharge;
        
        @JsonProperty("vat_amount")
        private BigDecimal vatAmount;
        
        @JsonProperty("total_amount")
        private BigDecimal totalAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InsertNewReadingResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("inserted_count")
        private Integer insertedCount;
        
        @JsonProperty("meter_readings_inserted")
        private List<String> meterReadingsInserted; // List of meter types inserted
        
        @JsonProperty("timestamp")
        private String timestamp;
    }
}