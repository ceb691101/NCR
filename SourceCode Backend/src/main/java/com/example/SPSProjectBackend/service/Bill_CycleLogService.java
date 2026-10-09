package com.example.SPSProjectBackend.service;

import com.example.SPSProjectBackend.dto.Bill_CycleLogDTO;
import com.example.SPSProjectBackend.dto.SecInfoLoginDTO;
import com.example.SPSProjectBackend.model.Bill_CycleLogFile;
import com.example.SPSProjectBackend.model.BillCycleConfig;
import com.example.SPSProjectBackend.model.HsbArea;
import com.example.SPSProjectBackend.repository.Bill_CycleLogRepository;
import com.example.SPSProjectBackend.repository.BillCycleConfigRepository;
import com.example.SPSProjectBackend.repository.HsbAreaRepository;
import com.example.SPSProjectBackend.repository.NcreBillCycleRepository;
import com.example.SPSProjectBackend.util.SessionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class Bill_CycleLogService {

    @Autowired
    private Bill_CycleLogRepository logRepository;

    @Autowired
    private BillCycleConfigRepository billCycleConfigRepository;

    @Autowired
    private NcreBillCycleRepository ncreBillCycleRepository;

    @Autowired
    private HsbAreaRepository areaRepository;

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private UserAreaPermissionService areaPermissionService;

    private static final String SPECIAL_PROCESS_CODE = "9.01";
    private static final String DEFAULT_END_TIME = "0:00:00";

    /**
     * Create a new log file entry - USING TRIMMED VERSIONS
     */
    @Transactional
    public Bill_CycleLogDTO.LogFileResponse createLogEntry(Bill_CycleLogDTO.LogFileEntryRequest request) {
        try {
            // Validate session and get user info
            Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(
                request.getSessionId(), request.getUserId());
            if (!userInfoOpt.isPresent()) {
                return createErrorResponse("Invalid session or user not found");
            }

            SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();

            // Validate area access
            if (!hasAreaAccess(userInfo, request.getAreaCode())) {
                return createErrorResponse("Access denied to area " + request.getAreaCode());
            }

            // Validate input
            String validationError = validateLogEntryRequest(request);
            if (validationError != null) {
                return createErrorResponse(validationError);
            }

            // The current bill cycle is global and comes from ncre_bill_cycle.
            Optional<Integer> activeBillCycleOpt = ncreBillCycleRepository.findCurrentBillCycleNumber();
            
            if (!activeBillCycleOpt.isPresent()) {
                return createErrorResponse("No current bill cycle found in ncre_bill_cycle");
            }

            Integer activeBillCycle = activeBillCycleOpt.get();

            // Check if the area can accept new log entries - USING TRIMMED VERSION
            if (!canCreateNewLogEntry(request.getAreaCode(), activeBillCycle, request.getProCode())) {
                return createErrorResponse("Cannot create new log entry. Area may have pending processes or cycle is complete.");
            }

            // Create new log entry
            Bill_CycleLogFile logEntry = new Bill_CycleLogFile();
            logEntry.setProCode(request.getProCode());
            logEntry.setAreaCode(request.getAreaCode());
            logEntry.setBillCycle(activeBillCycle);
            logEntry.setNoOfRecs(request.getNoOfRecs() != null ? request.getNoOfRecs() : 0);
            logEntry.setStartTime(request.getStartTime());
            logEntry.setEndTime(DEFAULT_END_TIME); // Default to 0:00:00
            logEntry.setDurationMin(0); // Will be calculated when end time is set
            logEntry.setUserId(request.getUserId());
            // dateTime is set automatically by @PrePersist

            // Save log entry
            Bill_CycleLogFile savedEntry = logRepository.save(logEntry);

            // Convert to DTO
            Bill_CycleLogDTO.LogFileEntryDTO entryDTO = convertToEntryDTO(savedEntry);

            return Bill_CycleLogDTO.LogFileResponse.builder()
                .success(true)
                .message("Log entry created successfully")
                .logEntry(entryDTO)
                .billCycleChanged(false)
                .newBillCycle(null)
                .oldBillCycle(null)
                .timestamp(LocalDateTime.now())
                .build();

        } catch (Exception e) {
            return createErrorResponse("Failed to create log entry: " + e.getMessage());
        }
    }

    /**
     * Update log entry with end time and calculate duration - USING TRIMMED VERSIONS
     */
    @Transactional
    public Bill_CycleLogDTO.LogFileResponse completeLogEntry(Bill_CycleLogDTO.LogFileEndRequest request) {
        try {
            // Validate session and get user info
            Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(
                request.getSessionId(), request.getUserId());
            if (!userInfoOpt.isPresent()) {
                return createErrorResponse("Invalid session or user not found");
            }

            SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();

            // Find log entry
            Optional<Bill_CycleLogFile> logEntryOpt = logRepository.findById(request.getLogId());
            if (!logEntryOpt.isPresent()) {
                return createErrorResponse("Log entry not found");
            }

            Bill_CycleLogFile logEntry = logEntryOpt.get();

            // Validate user access to this log entry - USING TRIMMED VERSION
            if (!hasAreaAccess(userInfo, logEntry.getAreaCode()) || 
                !request.getUserId().equals(logEntry.getUserId())) {
                return createErrorResponse("Access denied to this log entry");
            }

            // Check if log entry is already completed
            if (logEntry.getEndTime() != null && !DEFAULT_END_TIME.equals(logEntry.getEndTime())) {
                return createErrorResponse("Log entry is already completed");
            }

            // Validate end time
            if (request.getEndTime() == null || request.getEndTime().trim().isEmpty()) {
                return createErrorResponse("End time is required");
            }

            // Update log entry
            logEntry.setEndTime(request.getEndTime());
            if (request.getNoOfRecs() != null) {
                logEntry.setNoOfRecs(request.getNoOfRecs());
            }

            // Calculate duration in minutes
            Integer duration = calculateDurationInMinutes(logEntry.getStartTime(), request.getEndTime());
            logEntry.setDurationMin(duration);

            // Save updated entry
            Bill_CycleLogFile savedEntry = logRepository.save(logEntry);

            // Check if this is process 9.01 and trigger bill cycle change
            boolean billCycleChanged = false;
            Integer newBillCycle = null;
            Integer oldBillCycle = logEntry.getBillCycle();

            if (SPECIAL_PROCESS_CODE.equals(logEntry.getProCode())) {
                Bill_CycleLogDTO.BillCycleChangeDTO changeResult = triggerBillCycleChange(
                    logEntry.getAreaCode(), logEntry.getBillCycle(), 
                    logEntry.getProCode(), request.getUserId());
                
                if (changeResult != null) {
                    billCycleChanged = true;
                    newBillCycle = changeResult.getNewBillCycle();
                }
            }

            // Convert to DTO
            Bill_CycleLogDTO.LogFileEntryDTO entryDTO = convertToEntryDTO(savedEntry);

            return Bill_CycleLogDTO.LogFileResponse.builder()
                .success(true)
                .message("Log entry completed successfully" + 
                    (billCycleChanged ? " and bill cycle updated" : ""))
                .logEntry(entryDTO)
                .billCycleChanged(billCycleChanged)
                .newBillCycle(newBillCycle)
                .oldBillCycle(oldBillCycle)
                .timestamp(LocalDateTime.now())
                .build();

        } catch (Exception e) {
            return createErrorResponse("Failed to complete log entry: " + e.getMessage());
        }
    }

    /**
     * Get logs for a specific area and bill cycle - USING TRIMMED VERSIONS
     */
    @Transactional(readOnly = true)
    public Bill_CycleLogDTO.LogsListResponse getAreaLogs(Bill_CycleLogDTO.ActiveLogsRequest request) {
        try {
            // Validate session and get user info
            Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(
                request.getSessionId(), request.getUserId());
            if (!userInfoOpt.isPresent()) {
                return createErrorLogsResponse("Invalid session or user not found");
            }

            SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();

            // Validate area access
            if (!hasAreaAccess(userInfo, request.getAreaCode())) {
                return createErrorLogsResponse("Access denied to area " + request.getAreaCode());
            }

            // Get area information - USING TRIMMED VERSION
            Optional<HsbArea> areaOpt = areaRepository.findByAreaCodeTrimmed(request.getAreaCode());
            if (!areaOpt.isPresent()) {
                return createErrorLogsResponse("Area not found");
            }

            HsbArea area = areaOpt.get();

            // Determine bill cycle to use
            Integer billCycleToUse;
            if (request.getBillCycle() != null) {
                billCycleToUse = request.getBillCycle();
            } else {
                // Default to the global current cycle unless a historical cycle was requested.
                Optional<Integer> activeBillCycleOpt = ncreBillCycleRepository.findCurrentBillCycleNumber();
                if (!activeBillCycleOpt.isPresent()) {
                    return createErrorLogsResponse("No current bill cycle found in ncre_bill_cycle");
                }
                billCycleToUse = activeBillCycleOpt.get();
            }

            // Get logs - USING TRIMMED VERSION
            List<Bill_CycleLogFile> logs = logRepository.findByAreaCodeAndBillCycleTrimmed(
                request.getAreaCode(), billCycleToUse);

            // Convert to DTOs
            List<Bill_CycleLogDTO.LogFileEntryDTO> logDTOs = logs.stream()
                .map(this::convertToEntryDTO)
                .collect(Collectors.toList());

            // Calculate statistics
            int totalLogs = logDTOs.size();
            int completedLogs = (int) logDTOs.stream()
                .filter(Bill_CycleLogDTO.LogFileEntryDTO::getIsCompleted)
                .count();
            int pendingLogs = totalLogs - completedLogs;
            boolean hasProcess901 = logDTOs.stream()
                .anyMatch(log -> SPECIAL_PROCESS_CODE.equals(log.getProCode()));

            // Report the global current cycle even when logs were requested for a historical cycle.
            Optional<Integer> activeBillCycleOpt = ncreBillCycleRepository.findCurrentBillCycleNumber();
            Integer activeBillCycle = activeBillCycleOpt.orElse(null);

            return Bill_CycleLogDTO.LogsListResponse.builder()
                .success(true)
                .message("Logs retrieved successfully")
                .areaCode(request.getAreaCode())
                .areaName(area.getAreaName())
                .activeBillCycle(activeBillCycle)
                .requestedBillCycle(billCycleToUse)
                .logs(logDTOs)
                .totalLogs(totalLogs)
                .completedLogs(completedLogs)
                .pendingLogs(pendingLogs)
                .hasProcess901(hasProcess901)
                .timestamp(LocalDateTime.now())
                .build();

        } catch (Exception e) {
            return createErrorLogsResponse("Failed to retrieve logs: " + e.getMessage());
        }
    }

    /**
     * Get area status including bill cycle and log information - USING TRIMMED VERSIONS
     */
    @Transactional(readOnly = true)
    public Bill_CycleLogDTO.AreaStatusResponse getAreaStatus(String sessionId, String userId, String areaCode) {
        try {
            // Validate session and get user info
            Optional<SecInfoLoginDTO.UserInfo> userInfoOpt = sessionUtils.getUserLocationFromSession(sessionId, userId);
            if (!userInfoOpt.isPresent()) {
                return createErrorAreaStatusResponse("Invalid session or user not found");
            }

            SecInfoLoginDTO.UserInfo userInfo = userInfoOpt.get();

            // Validate area access
            if (!hasAreaAccess(userInfo, areaCode)) {
                return createErrorAreaStatusResponse("Access denied to area " + areaCode);
            }

            // Get area information - USING TRIMMED VERSION
            Optional<HsbArea> areaOpt = areaRepository.findByAreaCodeTrimmed(areaCode);
            if (!areaOpt.isPresent()) {
                return createErrorAreaStatusResponse("Area not found");
            }

            HsbArea area = areaOpt.get();

            // The current bill cycle is global and comes from ncre_bill_cycle.
            Optional<Integer> activeBillCycleOpt = ncreBillCycleRepository.findCurrentBillCycleNumber();
            if (!activeBillCycleOpt.isPresent()) {
                return createErrorAreaStatusResponse("No current bill cycle found in ncre_bill_cycle");
            }

            Integer activeBillCycle = activeBillCycleOpt.get();

            // Check for pending logs - USING TRIMMED VERSION
            Long pendingCount = logRepository.countPendingLogsByAreaAndBillCycleTrimmed(areaCode, activeBillCycle);
            boolean hasPendingLogs = pendingCount > 0;

            // Get last completed process - USING TRIMMED VERSION
            Optional<Bill_CycleLogFile> lastCompletedOpt = logRepository
                .findLastCompletedProcessTrimmed(areaCode, activeBillCycle);
            String lastCompletedProcess = lastCompletedOpt
                .map(Bill_CycleLogFile::getProCode)
                .orElse(null);

            // Check if process 9.01 is completed - USING TRIMMED VERSION
            Optional<Bill_CycleLogFile> completed901 = logRepository
                .findCompletedProcess901Trimmed(areaCode, activeBillCycle);
            
            // Determine cycle completion status
            String cycleStatus;
            boolean canEnterNewLog;

            if (completed901.isPresent()) {
                cycleStatus = "COMPLETED";
                canEnterNewLog = false; // Cycle is complete, new cycle should be created
            } else if (hasPendingLogs) {
                cycleStatus = "ACTIVE";
                canEnterNewLog = false; // Can't create new until pending are completed
            } else {
                cycleStatus = "ACTIVE";
                canEnterNewLog = true; // Can create new logs
            }

            Bill_CycleLogDTO.AreaStatusDTO areaStatus = Bill_CycleLogDTO.AreaStatusDTO.builder()
                .areaCode(areaCode)
                .areaName(area.getAreaName())
                .activeBillCycle(activeBillCycle)
                .hasPendingLogs(hasPendingLogs)
                .lastCompletedProcess(lastCompletedProcess)
                .canEnterNewLog(canEnterNewLog)
                .cycleCompletionStatus(cycleStatus)
                .build();

            return Bill_CycleLogDTO.AreaStatusResponse.builder()
                .success(true)
                .message("Area status retrieved successfully")
                .areaStatus(areaStatus)
                .timestamp(LocalDateTime.now())
                .build();

        } catch (Exception e) {
            return createErrorAreaStatusResponse("Failed to get area status: " + e.getMessage());
        }
    }

    /**
     * Get system statistics for dashboard - USING TRIMMED VERSIONS
     */
    @Transactional(readOnly = true)
    public Bill_CycleLogDTO.LogSystemStatsDTO getSystemStatistics() {
        try {
            // Get basic counts for today using LocalDateTime range
            LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime todayEnd = todayStart.plusDays(1);

            Long totalLogsToday = logRepository.countByDateTimeBetween(todayStart, todayEnd);
            Long completedLogsToday = logRepository.countCompletedByDateTimeBetween(todayStart, todayEnd);
            Long totalPendingLogs = logRepository.countAllPendingLogs();
            
            // Get area statistics
            List<String> areasWithPendingLogs = logRepository.findAreasWithPendingLogs();
            List<String> areasReadyForCycleChange = logRepository.findAreasReadyForBillCycleChange();
            
            // Get total active areas (areas with bill cycles) - USING TRIMMED VERSION
            List<String> allActiveAreas = billCycleConfigRepository.findAreaCodesWithActiveBillCycles();

            return Bill_CycleLogDTO.LogSystemStatsDTO.builder()
                .totalActiveAreas(allActiveAreas.size())
                .areasWithPendingLogs(areasWithPendingLogs.size())
                .areasReadyForCycleChange(areasReadyForCycleChange.size())
                .totalLogsToday(totalLogsToday.intValue())
                .completedLogsToday(completedLogsToday.intValue())
                .pendingLogsTotal(totalPendingLogs.intValue())
                .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get system statistics: " + e.getMessage(), e);
        }
    }

    // Helper Methods

    /**
     * Validate log entry request
     */
    private String validateLogEntryRequest(Bill_CycleLogDTO.LogFileEntryRequest request) {
        if (request.getAreaCode() == null || request.getAreaCode().trim().isEmpty()) {
            return "Area code is required";
        }
        if (request.getProCode() == null || request.getProCode().trim().isEmpty()) {
            return "Process code is required";
        }
        if (request.getStartTime() == null || request.getStartTime().trim().isEmpty()) {
            return "Start time is required";
        }
        if (!isValidTimeFormat(request.getStartTime())) {
            return "Invalid start time format. Use HH:mm:ss";
        }
        if (!isValidProcessCodeFormat(request.getProCode())) {
            return "Invalid process code format. Use X.XX format (e.g., 1.01, 2.05, 9.01)";
        }
        return null;
    }

    /**
     * Check if area can accept new log entries - USING TRIMMED VERSIONS
     */
    private boolean canCreateNewLogEntry(String areaCode, Integer billCycle, String proCode) {
        // Check if there are pending logs - USING TRIMMED VERSION
        Long pendingCount = logRepository.countPendingLogsByAreaAndBillCycleTrimmed(areaCode, billCycle);
        if (pendingCount > 0) {
            return false; // Can't create new while there are pending
        }

        // Check if process 9.01 is already completed (cycle is done) - USING TRIMMED VERSION
        Optional<Bill_CycleLogFile> completed901 = logRepository.findCompletedProcess901Trimmed(areaCode, billCycle);
        if (completed901.isPresent()) {
            return false; // Cycle is complete
        }

        // Check if trying to create duplicate process code - USING TRIMMED VERSION
        Optional<Bill_CycleLogFile> existingProcess = logRepository
            .findByAreaCodeAndBillCycleAndProCodeTrimmed(areaCode, billCycle, proCode);
        if (existingProcess.isPresent()) {
            return false; // Process code already exists
        }

        return true;
    }

    /**
     * Trigger bill cycle change when process 9.01 is completed - USING TRIMMED VERSIONS
     */
    @Transactional
    public Bill_CycleLogDTO.BillCycleChangeDTO triggerBillCycleChange(String areaCode, Integer currentBillCycle,
                                                                       String proCode, String userId) {
        try {
            // Update current bill cycle status to inactive (2) - USING TRIMMED VERSION
            Optional<BillCycleConfig> currentConfigOpt = billCycleConfigRepository
                .findByAreaCodeAndBillCycleTrimmed(areaCode, currentBillCycle);
            
            if (currentConfigOpt.isPresent()) {
                BillCycleConfig currentConfig = currentConfigOpt.get();
                currentConfig.setCycleStat(2); // Set to inactive
                billCycleConfigRepository.save(currentConfig);
            }

            // Create new bill cycle (increment by 1)
            Integer newBillCycle = currentBillCycle + 1;
            BillCycleConfig newConfig = new BillCycleConfig();
            newConfig.setBillCycle(newBillCycle);
            newConfig.setAreaCode(areaCode);
            newConfig.setUserId(userId);
            newConfig.setEnteredDate(LocalDateTime.now());
            newConfig.setCycleStat(1); // Set to active
            
            billCycleConfigRepository.save(newConfig);

            return Bill_CycleLogDTO.BillCycleChangeDTO.builder()
                .areaCode(areaCode)
                .oldBillCycle(currentBillCycle)
                .newBillCycle(newBillCycle)
                .triggeredByProcess(proCode)
                .triggeredByUser(userId)
                .changeTime(LocalDateTime.now())
                .reason("Process 9.01 completed")
                .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to trigger bill cycle change: " + e.getMessage(), e);
        }
    }

    /**
     * Check if user has access to an area, derived from the sec_info region/province/area
     * hierarchy: a NULL code widens the scope to the level above.
     */
    private boolean hasAreaAccess(SecInfoLoginDTO.UserInfo userInfo, String areaCode) {
        if (userInfo == null || areaCode == null || areaCode.trim().isEmpty()) {
            return false;
        }

        try {
            return areaPermissionService.isAreaPermitted(userInfo.getRegionCode(),
                    userInfo.getProvinceCode(), userInfo.getAreaCode(), areaCode);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Convert entity to DTO - USING TRIMMED VERSIONS
     */
    private Bill_CycleLogDTO.LogFileEntryDTO convertToEntryDTO(Bill_CycleLogFile logEntry) {
        boolean isCompleted = logEntry.getEndTime() != null && !DEFAULT_END_TIME.equals(logEntry.getEndTime());
        boolean isProcess901 = SPECIAL_PROCESS_CODE.equals(logEntry.getProCode());
        
        // Get area name - USING TRIMMED VERSION
        String areaName = "";
        try {
            Optional<HsbArea> areaOpt = areaRepository.findByAreaCodeTrimmed(logEntry.getAreaCode());
            if (areaOpt.isPresent()) {
                areaName = areaOpt.get().getAreaName();
            }
        } catch (Exception e) {
            // Ignore error, use empty name
        }

        return Bill_CycleLogDTO.LogFileEntryDTO.builder()
            .logId(logEntry.getLogId())
            .proCode(logEntry.getProCode())
            .areaCode(logEntry.getAreaCode())
            .areaName(areaName)
            .dateTime(logEntry.getDateTime())
            .billCycle(logEntry.getBillCycle())
            .noOfRecs(logEntry.getNoOfRecs())
            .startTime(logEntry.getStartTime())
            .endTime(logEntry.getEndTime())
            .durationMin(logEntry.getDurationMin())
            .userId(logEntry.getUserId())
            .isCompleted(isCompleted)
            .isProcess901(isProcess901)
            .build();
    }

    /**
     * Calculate duration in minutes between start and end time
     */
    private Integer calculateDurationInMinutes(String startTime, String endTime) {
        try {
            if (startTime == null || endTime == null || DEFAULT_END_TIME.equals(endTime)) {
                return 0;
            }

            // Parse time strings (format: "HH:mm:ss")
            String[] startParts = startTime.split(":");
            String[] endParts = endTime.split(":");

            int startMinutes = Integer.parseInt(startParts[0]) * 60 + Integer.parseInt(startParts[1]);
            int endMinutes = Integer.parseInt(endParts[0]) * 60 + Integer.parseInt(endParts[1]);

            int duration = endMinutes - startMinutes;
            
            // Handle case where end time is next day
            if (duration < 0) {
                duration += 24 * 60; // Add 24 hours
            }

            return duration;

        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Validate time format (HH:mm:ss)
     */
    private boolean isValidTimeFormat(String time) {
        try {
            String[] parts = time.split(":");
            if (parts.length != 3) return false;
            
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);
            
            return hours >= 0 && hours <= 23 && 
                   minutes >= 0 && minutes <= 59 && 
                   seconds >= 0 && seconds <= 59;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate process code format (X.XX)
     */
    private boolean isValidProcessCodeFormat(String proCode) {
        try {
            return proCode.matches("^\\d+\\.\\d+$");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate if process code can trigger bill cycle change
     */
    public boolean canTriggerBillCycleChange(String proCode) {
        return SPECIAL_PROCESS_CODE.equals(proCode);
    }

    /**
     * Get pending logs count for an area - USING TRIMMED VERSION
     */
    @Transactional(readOnly = true)
    public Long getPendingLogsCount(String areaCode, Integer billCycle) {
        return logRepository.countPendingLogsByAreaAndBillCycleTrimmed(areaCode, billCycle);
    }

    /**
     * Get completed logs count for an area - USING TRIMMED VERSION
     */
    @Transactional(readOnly = true)
    public Long getCompletedLogsCount(String areaCode, Integer billCycle) {
        return logRepository.countCompletedLogsByAreaAndBillCycleTrimmed(areaCode, billCycle);
    }

    /**
     * Check if specific process exists for area and bill cycle - USING TRIMMED VERSION
     */
    @Transactional(readOnly = true)
    public boolean processCodeExists(String areaCode, Integer billCycle, String proCode) {
        Optional<Bill_CycleLogFile> existing = logRepository
            .findByAreaCodeAndBillCycleAndProCodeTrimmed(areaCode, billCycle, proCode);
        return existing.isPresent();
    }

    /**
     * Get logs by user ID - USING TRIMMED VERSION
     */
    @Transactional(readOnly = true)
    public List<Bill_CycleLogDTO.LogFileEntryDTO> getLogsByUserId(String userId) {
        List<Bill_CycleLogFile> logs = logRepository.findByUserIdTrimmed(userId);
        return logs.stream()
            .map(this::convertToEntryDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get logs by date range - USING TRIMMED VERSION
     */
    @Transactional(readOnly = true)
    public List<Bill_CycleLogDTO.LogFileEntryDTO> getLogsByDateRange(
            String areaCode, LocalDateTime startDate, LocalDateTime endDate) {
        List<Bill_CycleLogFile> logs = logRepository
            .findByAreaCodeAndDateTimeBetweenTrimmed(areaCode, startDate, endDate);
        return logs.stream()
            .map(this::convertToEntryDTO)
            .collect(Collectors.toList());
    }

    // Error response helper methods
    private Bill_CycleLogDTO.LogFileResponse createErrorResponse(String message) {
        return Bill_CycleLogDTO.LogFileResponse.builder()
            .success(false)
            .message(message)
            .logEntry(null)
            .billCycleChanged(false)
            .timestamp(LocalDateTime.now())
            .build();
    }

    private Bill_CycleLogDTO.LogsListResponse createErrorLogsResponse(String message) {
        return Bill_CycleLogDTO.LogsListResponse.builder()
            .success(false)
            .message(message)
            .logs(new ArrayList<>())
            .timestamp(LocalDateTime.now())
            .build();
    }

    private Bill_CycleLogDTO.AreaStatusResponse createErrorAreaStatusResponse(String message) {
        return Bill_CycleLogDTO.AreaStatusResponse.builder()
            .success(false)
            .message(message)
            .areaStatus(null)
            .timestamp(LocalDateTime.now())
            .build();
    }
}