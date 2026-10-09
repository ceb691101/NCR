package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// Main DTO class - now only contains the nested static classes
public class ErrorStatisticsDTO {

    // No fields in the outer class, only nested static classes

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorStatsResponse {
        @JsonProperty("success")
        private Boolean success;

        @JsonProperty("message")
        private String message;

        @JsonProperty("error_statistics")
        private ErrorStatisticsData errorStatistics;

        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorStatisticsData {
        @JsonProperty("area_code")
        private String areaCode;

        @JsonProperty("area_name")
        private String areaName;

        @JsonProperty("active_bill_cycle")
        private String activeBillCycle;

        @JsonProperty("total_accounts_with_errors")
        private Integer totalAccountsWithErrors;

        @JsonProperty("total_error_instances")
        private Integer totalErrorInstances;

        @JsonProperty("error_counts")
        private Map<String, ErrorCountDTO> errorCounts;

        @JsonProperty("unread_accounts_count")
        private Integer unreadAccountsCount;

        @JsonProperty("total_accounts_in_area")
        private Integer totalAccountsInArea;

        @JsonProperty("error_percentage")
        private Double errorPercentage;

        /** Areas covered by this payload. More than one when the user has not narrowed the selection. */
        @JsonProperty("area_codes")
        private List<String> areaCodes;

        /** Active bill cycle resolved per area; cycles can differ between areas. */
        @JsonProperty("bill_cycles")
        private List<String> billCycles;

        public ErrorStatisticsData(String areaCode, String areaName, String activeBillCycle,
                                   Integer totalAccountsWithErrors, Integer totalErrorInstances,
                                   Map<String, ErrorCountDTO> errorCounts, Integer unreadAccountsCount,
                                   Integer totalAccountsInArea, Double errorPercentage) {
            this.areaCode = areaCode;
            this.areaName = areaName;
            this.activeBillCycle = activeBillCycle;
            this.totalAccountsWithErrors = totalAccountsWithErrors;
            this.totalErrorInstances = totalErrorInstances;
            this.errorCounts = errorCounts;
            this.unreadAccountsCount = unreadAccountsCount;
            this.totalAccountsInArea = totalAccountsInArea;
            this.errorPercentage = errorPercentage;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorCountDTO {
        @JsonProperty("error_code")
        private Integer errorCode;

        @JsonProperty("error_name")
        private String errorName;

        @JsonProperty("account_count")
        private Integer accountCount;

        @JsonProperty("instance_count")
        private Integer instanceCount;

        @JsonProperty("percentage")
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorStatsRequest {
        @JsonProperty("session_id")
        private String sessionId;

        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("area_code")
        private String areaCode;

        @JsonProperty("bill_cycle")
        private String billCycle;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetailsResponse {
        @JsonProperty("success")
        private Boolean success;

        @JsonProperty("message")
        private String message;

        @JsonProperty("error_code")
        private Integer errorCode;

        @JsonProperty("error_name")
        private String errorName;

        @JsonProperty("accounts_with_error")
        private List<AccountErrorDetailsDTO> accountsWithError;

        @JsonProperty("total_accounts")
        private Integer totalAccounts;

        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountErrorDetailsDTO {
        @JsonProperty("account_number")
        private String accountNumber;

        @JsonProperty("customer_name")
        private String customerName;

        @JsonProperty("area_code")
        private String areaCode;

        /**
         * Bill cycle this account's readings belong to. Areas can sit on different cycles,
         * so it travels with the account rather than being a single value for the response.
         */
        @JsonProperty("bill_cycle")
        private String billCycle;

        @JsonProperty("error_instances")
        private List<ErrorInstanceDTO> errorInstances;

        @JsonProperty("total_error_instances")
        private Integer totalErrorInstances;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorInstanceDTO {
        @JsonProperty("meter_type")
        private String meterType;

        @JsonProperty("error_code")
        private Integer errorCode;

        @JsonProperty("error_name")
        private String errorName;

        @JsonProperty("reading_date")
        private String readingDate;

        @JsonProperty("present_reading")
        private Integer presentReading;

        @JsonProperty("previous_reading")
        private Integer previousReading;

        @JsonProperty("units")
        private Integer units;

        @JsonProperty("has_error")
        private Boolean hasError = false;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetailsWithReadingsResponse {
        @JsonProperty("success")
        private Boolean success;

        @JsonProperty("message")
        private String message;

        @JsonProperty("error_code")
        private Integer errorCode;

        @JsonProperty("error_name")
        private String errorName;

        @JsonProperty("area_code")
        private String areaCode;

        @JsonProperty("area_name")
        private String areaName;

        @JsonProperty("active_bill_cycle")
        private String activeBillCycle;

        @JsonProperty("accounts_with_error")
        private List<AccountErrorWithReadingsDTO> accountsWithError;

        @JsonProperty("total_accounts")
        private Integer totalAccounts;

        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountErrorWithReadingsDTO {
        @JsonProperty("account_number")
        private String accountNumber;

        @JsonProperty("customer_name")
        private String customerName;

        @JsonProperty("area_code")
        private String areaCode;

        @JsonProperty("customer_category")
        private String customerCategory;

        @JsonProperty("tariff")
        private String tariff;

        @JsonProperty("reader_code")
        private String readerCode;

        @JsonProperty("daily_pack")
        private String dailyPack;

        @JsonProperty("walk_order")
        private String walkOrder;

        @JsonProperty("installation_id")
        private String installationId;

        @JsonProperty("error_instances")
        private List<ErrorInstanceDTO> errorInstances;

        @JsonProperty("all_meter_readings")
        private List<MeterReadingWithErrorDTO> allMeterReadings;

        @JsonProperty("total_error_instances")
        private Integer totalErrorInstances;

        @JsonProperty("total_meter_readings")
        private Integer totalMeterReadings;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeterReadingWithErrorDTO {
        @JsonProperty("meter_type")
        private String meterType;

        @JsonProperty("meter_number")
        private String meterNumber;

        @JsonProperty("reading_date")
        private String readingDate;

        @JsonProperty("previous_reading_date")
        private String previousReadingDate;

        @JsonProperty("present_reading")
        private Integer presentReading;

        @JsonProperty("previous_reading")
        private Integer previousReading;

        @JsonProperty("units")
        private Integer units;

        @JsonProperty("assessed_code")
        private String assessedCode;

        @JsonProperty("multiplied_by")
        private BigDecimal multipliedBy;

        @JsonProperty("rate")
        private BigDecimal rate;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("has_error")
        private Boolean hasError;

        @JsonProperty("error_code")
        private Integer errorCode;

        @JsonProperty("error_message")
        private String errorMessage;
    }
}