package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class ConsumptionHistoryDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumptionCycleDTO {
        @JsonProperty("bill_cycle")
        private String billCycle;
        
        @JsonProperty("bill_cycle_date")
        private Date billCycleDate;
        
        @JsonProperty("off_peak_consumption")
        private BigDecimal offPeakConsumption; // tot_untskwo
        
        @JsonProperty("day_consumption")
        private BigDecimal dayConsumption; // tot_untskwd
        
        @JsonProperty("peak_consumption")
        private BigDecimal peakConsumption; // tot_untskwp
        
        @JsonProperty("kva_consumption")
        private BigDecimal kvaConsumption; // tot_kva
        
        @JsonProperty("total_charge")
        private BigDecimal totalCharge;
        
        @JsonProperty("fixed_charge")
        private BigDecimal fixedCharge;
        
        @JsonProperty("vat_amount")
        private BigDecimal vatAmount;
        
        @JsonProperty("total_amount")
        private BigDecimal totalAmount;
        
        @JsonProperty("bf_balance")
        private BigDecimal bfBalance;
        
        @JsonProperty("current_balance")
        private BigDecimal currentBalance;
        
        @JsonProperty("reading_date")
        private Date readingDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumptionSummaryDTO {
        @JsonProperty("account_number")
        private String accountNumber;
        
        @JsonProperty("customer_name")
        private String customerName;
        
        @JsonProperty("tariff")
        private String tariff;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("latest_bill_cycle")
        private String latestBillCycle;
        
        @JsonProperty("bill_cycle_count")
        private Integer billCycleCount;
        
        @JsonProperty("cycles")
        private List<ConsumptionCycleDTO> cycles;
        
        @JsonProperty("total_off_peak")
        private BigDecimal totalOffPeak;
        
        @JsonProperty("total_day")
        private BigDecimal totalDay;
        
        @JsonProperty("total_peak")
        private BigDecimal totalPeak;
        
        @JsonProperty("total_kva")
        private BigDecimal totalKva;
        
        @JsonProperty("average_off_peak")
        private BigDecimal averageOffPeak;
        
        @JsonProperty("average_day")
        private BigDecimal averageDay;
        
        @JsonProperty("average_peak")
        private BigDecimal averagePeak;
        
        @JsonProperty("average_kva")
        private BigDecimal averageKva;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumptionRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("account_number")
        private String accountNumber;
        
        @JsonProperty("cycle_count")
        private Integer cycleCount; // 3, 6, or 12
        
        @JsonProperty("area_code")
        private String areaCode; // Optional - for validation
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumptionResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("consumption_history")
        private ConsumptionSummaryDTO consumptionHistory;
        
        @JsonProperty("timestamp")
        private String timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MultipleAccountsRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("account_numbers")
        private List<String> accountNumbers;
        
        @JsonProperty("cycle_count")
        private Integer cycleCount;
        
        @JsonProperty("area_code")
        private String areaCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MultipleAccountsResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("total_accounts")
        private Integer totalAccounts;
        
        @JsonProperty("accounts_with_data")
        private Integer accountsWithData;
        
        @JsonProperty("accounts_without_data")
        private Integer accountsWithoutData;
        
        @JsonProperty("consumption_histories")
        private List<ConsumptionSummaryDTO> consumptionHistories;
        
        @JsonProperty("timestamp")
        private String timestamp;
    }
}