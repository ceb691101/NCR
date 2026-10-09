package com.example.SPSProjectBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class Bill_CycleLogDTO {
    
    // DTO for creating log file entry
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogFileEntryRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("pro_code")
        private String proCode;
        
        @JsonProperty("no_of_recs")
        private Integer noOfRecs;
        
        @JsonProperty("start_time")
        private String startTime; // Format: "HH:mm:ss"
    }
    
    // DTO for updating log file entry with end time
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogFileEndRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("log_id")
        private Long logId;
        
        @JsonProperty("end_time")
        private String endTime; // Format: "HH:mm:ss"
        
        @JsonProperty("no_of_recs")
        private Integer noOfRecs; // Can be updated when ending
    }
    
    // DTO for log file entry response
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.Builder
    public static class LogFileEntryDTO {
        @JsonProperty("log_id")
        private Long logId;
        
        @JsonProperty("pro_code")
        private String proCode;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("area_name")
        private String areaName;
        
        @JsonProperty("date_time")
        private LocalDateTime dateTime;
        
        @JsonProperty("bill_cycle")
        private Integer billCycle;
        
        @JsonProperty("no_of_recs")
        private Integer noOfRecs;
        
        @JsonProperty("start_time")
        private String startTime;
        
        @JsonProperty("end_time")
        private String endTime;
        
        @JsonProperty("duration_min")
        private Integer durationMin;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("is_completed")
        private Boolean isCompleted;
        
        @JsonProperty("is_process_901")
        private Boolean isProcess901; // Special flag for pro_code 9.01
    }
    
    // DTO for log file entry response
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.Builder
    public static class LogFileResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("log_entry")
        private LogFileEntryDTO logEntry;
        
        @JsonProperty("bill_cycle_changed")
        private Boolean billCycleChanged;
        
        @JsonProperty("new_bill_cycle")
        private Integer newBillCycle;
        
        @JsonProperty("old_bill_cycle")
        private Integer oldBillCycle;
        
        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
    }
    
    // DTO for getting active logs for an area
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveLogsRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("bill_cycle")
        private Integer billCycle; // Optional - if not provided, use active bill cycle
    }
    
    // DTO for logs list response
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.Builder
    public static class LogsListResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("area_name")
        private String areaName;
        
        @JsonProperty("active_bill_cycle")
        private Integer activeBillCycle;
        
        @JsonProperty("requested_bill_cycle")
        private Integer requestedBillCycle;
        
        @JsonProperty("logs")
        private List<LogFileEntryDTO> logs;
        
        @JsonProperty("total_logs")
        private Integer totalLogs;
        
        @JsonProperty("completed_logs")
        private Integer completedLogs;
        
        @JsonProperty("pending_logs")
        private Integer pendingLogs;
        
        @JsonProperty("has_process_901")
        private Boolean hasProcess901;
        
        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
    }
    
    // DTO for area current status
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.Builder
    public static class AreaStatusDTO {
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("area_name")
        private String areaName;
        
        @JsonProperty("active_bill_cycle")
        private Integer activeBillCycle;
        
        @JsonProperty("has_pending_logs")
        private Boolean hasPendingLogs;
        
        @JsonProperty("last_completed_process")
        private String lastCompletedProcess;
        
        @JsonProperty("can_enter_new_log")
        private Boolean canEnterNewLog;
        
        @JsonProperty("cycle_completion_status")
        private String cycleCompletionStatus; // "ACTIVE", "READY_FOR_901", "COMPLETED"
    }
    
    // DTO for area status response
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.Builder
    public static class AreaStatusResponse {
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("area_status")
        private AreaStatusDTO areaStatus;
        
        @JsonProperty("timestamp")
        private LocalDateTime timestamp;
    }
    
    // DTO for bill cycle change notification
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.Builder
    public static class BillCycleChangeDTO {
        @JsonProperty("area_code")
        private String areaCode;
        
        @JsonProperty("old_bill_cycle")
        private Integer oldBillCycle;
        
        @JsonProperty("new_bill_cycle")
        private Integer newBillCycle;
        
        @JsonProperty("triggered_by_process")
        private String triggeredByProcess;
        
        @JsonProperty("triggered_by_user")
        private String triggeredByUser;
        
        @JsonProperty("change_time")
        private LocalDateTime changeTime;
        
        @JsonProperty("reason")
        private String reason;
    }
    
    // DTO for process code validation
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.Builder
    public static class ProcessCodeValidationDTO {
        @JsonProperty("pro_code")
        private String proCode;
        
        @JsonProperty("is_valid")
        private Boolean isValid;
        
        @JsonProperty("is_special_process")
        private Boolean isSpecialProcess; // For 9.01
        
        @JsonProperty("requires_end_time")
        private Boolean requiresEndTime;
        
        @JsonProperty("can_trigger_bill_cycle_change")
        private Boolean canTriggerBillCycleChange;
        
        @JsonProperty("validation_message")
        private String validationMessage;
    }
    
    // DTO for bulk log operations
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkLogRequest {
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("area_codes")
        private List<String> areaCodes;
        
        @JsonProperty("bill_cycle")
        private Integer billCycle; // Optional
        
        @JsonProperty("include_completed")
        private Boolean includeCompleted;
        
        @JsonProperty("include_pending")
        private Boolean includePending;
    }
    
    // DTO for system statistics
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.Builder
    public static class LogSystemStatsDTO {
        @JsonProperty("total_active_areas")
        private Integer totalActiveAreas;
        
        @JsonProperty("areas_with_pending_logs")
        private Integer areasWithPendingLogs;
        
        @JsonProperty("areas_ready_for_cycle_change")
        private Integer areasReadyForCycleChange;
        
        @JsonProperty("total_logs_today")
        private Integer totalLogsToday;
        
        @JsonProperty("completed_logs_today")
        private Integer completedLogsToday;
        
        @JsonProperty("pending_logs_total")
        private Integer pendingLogsTotal;
    }
}