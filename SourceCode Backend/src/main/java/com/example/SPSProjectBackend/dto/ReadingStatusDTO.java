package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ReadingStatusDTO {

    // DTO for customer reading status
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerReadingStatusDTO {
        @JsonProperty("ncre_type")
        private String ncreType;

        @JsonProperty("acc_nbr")
        private String accNbr;
        
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("address_l1")
        private String addressL1;
        
        @JsonProperty("area_cd")
        private String areaCd;
        
        @JsonProperty("bill_cycle")
        private Integer billCycle;
        
        @JsonProperty("folio_no")
        private Short folioNo;
        
        @JsonProperty("tariff")
        private String tariff;

        @JsonProperty("tariff_desc")
        private String tariffDesc;

        @JsonProperty("tariff_type")
        private String tariffType;
        
        @JsonProperty("mobile_no")
        private String mobileNo;
        
        @JsonProperty("tel_nbr")
        private String telNbr;

        @JsonProperty("facility_name")
        private String facilityName;

        @JsonProperty("responsible_ee")
        private String responsibleEe;
        
        @JsonProperty("has_reading")
        private Boolean hasReading;
        
        @JsonProperty("readings")
        private List<TmpReadingsDTO> readings;
        
        @JsonProperty("reading_count")
        private Integer readingCount;
    }

    // DTO for area reading status summary
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AreaReadingStatusDTO {
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("area_name")
        private String areaName;
        
        @JsonProperty("province_code")
        private String provinceCode;
        
        @JsonProperty("province_name")
        private String provinceName;
        
        @JsonProperty("region_code")
        private String regionCode;
        
        @JsonProperty("active_bill_cycle")
        private Integer activeBillCycle;
        
        @JsonProperty("total_customers")
        private Integer totalCustomers;
        
        @JsonProperty("customers_with_readings")
        private Integer customersWithReadings;
        
        @JsonProperty("customers_without_readings")
        private Integer customersWithoutReadings;
        
        @JsonProperty("reading_percentage")
        private Double readingPercentage;
        
        @JsonProperty("customers_with_readings_list")
        private List<CustomerReadingStatusDTO> customersWithReadingsList;
        
        @JsonProperty("customers_without_readings_list")
        private List<CustomerReadingStatusDTO> customersWithoutReadingsList;
    }

    // DTO for reading status request
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReadingStatusRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("include_customer_details")
        private Boolean includeCustomerDetails = true;
        
        @JsonProperty("include_reading_details")
        private Boolean includeReadingDetails = false;
    }

    // DTO for reading status response
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReadingStatusResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("user_category")
        private String userCategory;
        
        @JsonProperty("area_reading_status")
        private List<AreaReadingStatusDTO> areaReadingStatus;
        
        @JsonProperty("summary")
        private ReadingStatusSummaryDTO summary;
        
        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
    }

    // DTO for overall summary
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReadingStatusSummaryDTO {
        @JsonProperty("total_areas")
        private Integer totalAreas;
        
        @JsonProperty("total_customers")
        private Integer totalCustomers;
        
        @JsonProperty("total_customers_with_readings")
        private Integer totalCustomersWithReadings;
        
        @JsonProperty("total_customers_without_readings")
        private Integer totalCustomersWithoutReadings;
        
        @JsonProperty("overall_reading_percentage")
        private Double overallReadingPercentage;
        
        @JsonProperty("areas_with_active_bill_cycles")
        private Integer areasWithActiveBillCycles;
        
        @JsonProperty("areas_without_active_bill_cycles")
        private Integer areasWithoutActiveBillCycles;
    }

    // DTO for pending readings (customers without readings)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingReadingsDTO {
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("area_name")
        private String areaName;
        
        @JsonProperty("active_bill_cycle")
        private Integer activeBillCycle;
        
        @JsonProperty("pending_customers")
        private List<CustomerReadingStatusDTO> pendingCustomers;
        
        @JsonProperty("pending_count")
        private Integer pendingCount;
    }

    // DTO for completed readings (customers with readings)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletedReadingsDTO {
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("area_name")
        private String areaName;
        
        @JsonProperty("active_bill_cycle")
        private Integer activeBillCycle;
        
        @JsonProperty("completed_customers")
        private List<CustomerReadingStatusDTO> completedCustomers;
        
        @JsonProperty("completed_count")
        private Integer completedCount;
    }
}