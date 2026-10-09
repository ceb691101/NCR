// MeterReadingInfoDto 
package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class MeterReadingInfoDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterReadingInfoResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("meter_reading_info")
        private MeterReadingInfoDetailsDTO meterReadingInfo;
        
        @JsonProperty("timestamp")
        private String timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterReadingInfoDetailsDTO {
        @JsonProperty("account_number")
        private String accountNumber;
        
        @JsonProperty("folio_no")
        private Short folioNo;
        
        @JsonProperty("tariff_type")
        private String tariffType;
        
        @JsonProperty("tariff_value")
        private String tariffValue;
        
        @JsonProperty("meter_number")
        private String meterNumber;
        
        @JsonProperty("customer_category")
        private String customerCategory;
        
        // Area Information
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("area_name")
        private String areaName;
        
        // Bill Cycle Information
        @JsonProperty("current_bill_cycle")
        private String currentBillCycle;
        
        @JsonProperty("bill_cycle_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date billCycleDate;
        
        // Reading Dates
        @JsonProperty("reading_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date readingDate;
        
        @JsonProperty("previous_reading_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date previousReadingDate;
        
        // Meter Types and their readings
        @JsonProperty("meter_types")
        private List<MeterTypeDetailsDTO> meterTypes;
        
        // Status Information
        @JsonProperty("has_reading")
        private Boolean hasReading;
        
        @JsonProperty("reading_status")
        private String readingStatus; // "RECEIVED" or "PENDING"

        @JsonProperty("accept_ru")
        private Short acceptRu;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterTypeDetailsDTO {
        @JsonProperty("meter_type")
        private String meterType; // KWD, KWP, KWO, KWT
        
        @JsonProperty("meter_number")
        private String meterNumber;
        
        @JsonProperty("present_reading")
        private BigDecimal presentReading;
        
        @JsonProperty("previous_reading")
        private BigDecimal previousReading;
        
        @JsonProperty("units")
        private BigDecimal units; // Energy Sent to Grid
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterReadingRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("account_number")
        private String accountNumber;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("bill_cycle")
        private String billCycle; // Optional - if not provided, uses active bill cycle
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkMeterReadingRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("account_numbers")
        private List<String> accountNumbers;
        
        @JsonProperty("bill_cycle")
        private String billCycle; // Optional - if not provided, uses active bill cycle
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkMeterReadingResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("active_bill_cycle")
        private String activeBillCycle;
        
        @JsonProperty("total_customers")
        private Integer totalCustomers;
        
        @JsonProperty("customers_with_readings")
        private Integer customersWithReadings;
        
        @JsonProperty("customers_without_readings")
        private Integer customersWithoutReadings;
        
        @JsonProperty("meter_readings")
        private List<MeterReadingInfoDetailsDTO> meterReadings;
        
        @JsonProperty("timestamp")
        private String timestamp;
    }

    // Edit
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterReadingEditRequest {
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
        private List<MeterReadingEditDTO> meterReadings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterReadingEditDTO {
        @JsonProperty("meter_type")
        private String meterType;
        
        @JsonProperty("present_reading")
        private Integer presentReading;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterReadingEditResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("updated_meter_reading_info")
        private MeterReadingInfoDetailsDTO updatedMeterReadingInfo;
        
        @JsonProperty("timestamp")
        private String timestamp;
    }
}