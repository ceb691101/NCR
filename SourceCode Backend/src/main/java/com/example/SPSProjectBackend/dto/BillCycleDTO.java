package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class BillCycleDTO {
    
    // DTO for individual bill cycle configuration
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillCycleConfigDTO {
        @JsonProperty("bill_cycle")
        private Integer billCycle;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("entered_date")
        private LocalDateTime enteredDate;
        
        @JsonProperty("cycle_stat")
        private Integer cycleStat;
        
        @JsonProperty("is_active")
        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentBillCycleDTO {
        @JsonProperty("bill_cycle")
        private Integer billCycle;

        @JsonProperty("bill_year")
        private Integer billYear;

        @JsonProperty("bill_month")
        private Integer billMonth;

        @JsonProperty("is_current")
        private Integer isCurrent;

        @JsonProperty("is_closed")
        private Integer isClosed;

        @JsonProperty("cycle_status")
        private String cycleStatus;

        @JsonProperty("next_bill_cycle")
        private Integer nextBillCycle;

        @JsonProperty("next_bill_year")
        private Integer nextBillYear;

        @JsonProperty("next_bill_month")
        private Integer nextBillMonth;
    }

    // DTO for the invoice creation summary of a bill cycle
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceCreationSummaryDTO {
        @JsonProperty("bill_cycle")
        private Integer billCycle;

        @JsonProperty("active_developers")
        private Long activeDevelopers;

        @JsonProperty("invoices_created")
        private Long invoicesCreated;

        @JsonProperty("invoices_pending")
        private Long invoicesPending;

        @JsonProperty("progress_percent")
        private Double progressPercent;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveDeveloperReconciliationDTO {
        @JsonProperty("bill_cycle")
        private Integer billCycle;

        @JsonProperty("added_folios")
        private List<Integer> addedFolios;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceCreateEntryDTO {
        @JsonProperty("folio_no")
        private Integer folioNo;

        @JsonProperty("developer_name")
        private String developerName;

        @JsonProperty("is_create")
        private Integer isCreate;

        @JsonProperty("remarks")
        private String remarks;
    }

    // DTO for an active developer that has no created invoice for a bill cycle
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingDeveloperDTO {
        @JsonProperty("folio_no")
        private String folioNo;

        @JsonProperty("developer_name")
        private String developerName;

        @JsonProperty("facility_name")
        private String facilityName;

        @JsonProperty("region")
        private String region;
    }

    // DTO describing the outcome of closing a bill cycle
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EndBillCycleResultDTO {
        @JsonProperty("closed_bill_cycle")
        private Integer closedBillCycle;

        @JsonProperty("closed_bill_year")
        private Integer closedBillYear;

        @JsonProperty("closed_bill_month")
        private Integer closedBillMonth;

        @JsonProperty("new_bill_cycle")
        private Integer newBillCycle;

        @JsonProperty("new_bill_year")
        private Integer newBillYear;

        @JsonProperty("new_bill_month")
        private Integer newBillMonth;

        @JsonProperty("closed_by")
        private String closedBy;

        @JsonProperty("closed_date")
        private LocalDateTime closedDate;

        @JsonProperty("invoice_create_records_created")
        private Long invoiceCreateRecordsCreated;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EndBillCycleRequestDTO {
        @JsonProperty("remarks")
        private String remarks;
    }
    
    // DTO for area with its active bill cycle
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AreaBillCycleDTO {
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
        
        @JsonProperty("has_bill_cycle")
        private Boolean hasBillCycle;
    }
    
    // DTO for login response with bill cycles
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserBillCycleInfo {
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("user_name")
        private String userName;
        
        @JsonProperty("user_category")
        private String userCategory;
        
        @JsonProperty("bill_cycles")
        private List<AreaBillCycleDTO> billCycles;
        
        @JsonProperty("total_areas")
        private Integer totalAreas;
        
        @JsonProperty("areas_with_cycles")
        private Integer areasWithCycles;
    }
    
    // DTO for bill cycle summary
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillCycleSummaryDTO {
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("area_name")
        private String areaName;
        
        @JsonProperty("active_bill_cycle")
        private Integer activeBillCycle;
        
        @JsonProperty("total_cycles")
        private Integer totalCycles;
        
        @JsonProperty("active_cycles")
        private Integer activeCycles;
        
        @JsonProperty("inactive_cycles")
        private Integer inactiveCycles;
    }
    
    // Request DTO for getting bill cycles
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillCycleRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("province_code")
        private String provinceCode;
        
        @JsonProperty("region_code")
        private String regionCode;
    }
    
    // Response DTO for bill cycles
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillCycleResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("user_category")
        private String userCategory;
        
        @JsonProperty("bill_cycles")
        private List<AreaBillCycleDTO> billCycles;
        
        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
    }
}