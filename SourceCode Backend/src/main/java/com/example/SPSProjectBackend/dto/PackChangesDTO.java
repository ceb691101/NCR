// PackChangesDTO.java
package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

public class PackChangesDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackChangesRequest {
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
        
        @JsonProperty("pack_changes")
        private PackChangesDataDTO packChanges;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackChangesDataDTO {
        @JsonProperty("new_reader_code")
        private String newReaderCode;
        
        @JsonProperty("new_daily_pack")
        private String newDailyPack;
        
        @JsonProperty("new_walk_order")
        private String newWalkOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackChangesResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("updated_pack_changes")
        private PackChangesDataDTO updatedPackChanges;
        
        @JsonProperty("timestamp")
        private String timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackChangesViewDTO {
        @JsonProperty("account_number")
        private String accountNumber;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("bill_cycle")
        private String billCycle;
        
        @JsonProperty("current_reader_code")
        private String currentReaderCode;
        
        @JsonProperty("current_daily_pack")
        private String currentDailyPack;
        
        @JsonProperty("current_walk_order")
        private String currentWalkOrder;
        
        @JsonProperty("new_reader_code")
        private String newReaderCode;
        
        @JsonProperty("new_daily_pack")
        private String newDailyPack;
        
        @JsonProperty("new_walk_order")
        private String newWalkOrder;
        
        @JsonProperty("has_pending_changes")
        private Boolean hasPendingChanges;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackChangesViewRequest {
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
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackChangesViewResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("pack_changes_info")
        private PackChangesViewDTO packChangesInfo;
        
        @JsonProperty("timestamp")
        private String timestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkPackChangesRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("bill_cycle")
        private String billCycle;
        
        @JsonProperty("pack_changes_list")
        private List<BulkPackChangeItemDTO> packChangesList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkPackChangeItemDTO {
        @JsonProperty("account_number")
        private String accountNumber;
        
        @JsonProperty("pack_changes")
        private PackChangesDataDTO packChanges;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkPackChangesResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("total_processed")
        private Integer totalProcessed;
        
        @JsonProperty("successful_updates")
        private Integer successfulUpdates;
        
        @JsonProperty("failed_updates")
        private Integer failedUpdates;
        
        @JsonProperty("failed_accounts")
        private List<String> failedAccounts;
        
        @JsonProperty("timestamp")
        private String timestamp;
    }
}