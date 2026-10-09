package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class PendingMeterReadingsDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingMeterReadingRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("bill_cycle")
        private String billCycle; // Optional - if not provided, uses active bill cycle
        
        @JsonProperty("account_number")
        private String accountNumber; // For single customer requests
        
        @JsonProperty("folio_no")
        private Short folioNo; // For lookup by folio number
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingMeterReadingResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("pending_readings")
        private List<PendingMeterReadingDetailsDTO> pendingReadings;
        
        @JsonProperty("timestamp")
        private String timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingMeterReadingDetailsDTO {
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
        
        @JsonProperty("developer_name")
        private String developerName;
        
        @JsonProperty("facility_name")
        private String facilityName;

        // Area Information
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("area_name")
        private String areaName;
        
        // Bill Cycle Information
        @JsonProperty("current_bill_cycle")
        private String currentBillCycle;
        
        @JsonProperty("bill_month")
        private Short billMonth;
        
        @JsonProperty("bill_year")
        private Short billYear;
        
        @JsonProperty("bill_cycle_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date billCycleDate;

        @JsonProperty("responsible_ee")
        private String responsibleEe;
        
        // Reading Dates
        @JsonProperty("reading_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date readingDate; // Will be null - user will select
        
        @JsonProperty("previous_reading_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date previousReadingDate;
        
        // Meter Types and Details
        @JsonProperty("meter_types")
        private List<MeterTypePendingDTO> meterTypes;
        
        // Status Information
        @JsonProperty("has_reading")
        private Boolean hasReading;
        
        @JsonProperty("reading_status")
        private String readingStatus; // "PENDING"

        @JsonProperty("accept_ru")
        private Short acceptRu;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterTypePendingDTO {
        @JsonProperty("meter_type")
        private String meterType; // KWO, KWD, KWP, KWT
        
        @JsonProperty("meter_number")
        private String meterNumber;
        
        @JsonProperty("present_reading")
        private BigDecimal presentReading; // Will be null - user will enter
        
        @JsonProperty("previous_reading")
        private BigDecimal previousReading;
        
        @JsonProperty("units")
        private BigDecimal units; // Energy Sent to Grid (default 0 for pending)
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SinglePendingReadingResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("pending_reading")
        private PendingMeterReadingDetailsDTO pendingReading;
        
        @JsonProperty("timestamp")
        private String timestamp;
    }
}