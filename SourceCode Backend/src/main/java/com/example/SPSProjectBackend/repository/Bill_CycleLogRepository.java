package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.Bill_CycleLogFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface Bill_CycleLogRepository extends JpaRepository<Bill_CycleLogFile, Long> {
    
    // ============ NEW TRIMMED QUERIES ============
    
    // Find all logs for a specific area and bill cycle with trimming
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE TRIM(l.areaCode) = TRIM(:areaCode) AND l.billCycle = :billCycle ORDER BY l.dateTime DESC")
    List<Bill_CycleLogFile> findByAreaCodeAndBillCycleTrimmed(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Find all logs for a specific area with trimming
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE TRIM(l.areaCode) = TRIM(:areaCode) ORDER BY l.billCycle DESC, l.dateTime DESC")
    List<Bill_CycleLogFile> findByAreaCodeTrimmed(@Param("areaCode") String areaCode);
    
    // Find pending logs for an area and bill cycle with trimming
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE TRIM(l.areaCode) = TRIM(:areaCode) AND l.billCycle = :billCycle AND (l.endTime IS NULL OR TRIM(l.endTime) = '0:00:00') ORDER BY l.dateTime DESC")
    List<Bill_CycleLogFile> findPendingLogsByAreaAndBillCycleTrimmed(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Find completed logs for an area and bill cycle with trimming
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE TRIM(l.areaCode) = TRIM(:areaCode) AND l.billCycle = :billCycle AND l.endTime IS NOT NULL AND TRIM(l.endTime) != '0:00:00' ORDER BY l.dateTime DESC")
    List<Bill_CycleLogFile> findCompletedLogsByAreaAndBillCycleTrimmed(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Check if process code 9.01 exists and is completed with trimming
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE TRIM(l.areaCode) = TRIM(:areaCode) AND l.billCycle = :billCycle AND TRIM(l.proCode) = '9.01' AND l.endTime IS NOT NULL AND TRIM(l.endTime) != '0:00:00'")
    Optional<Bill_CycleLogFile> findCompletedProcess901Trimmed(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Check if process code 9.01 exists with trimming
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE TRIM(l.areaCode) = TRIM(:areaCode) AND l.billCycle = :billCycle AND TRIM(l.proCode) = '9.01'")
    Optional<Bill_CycleLogFile> findProcess901Trimmed(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Find logs by user ID with trimming
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE TRIM(l.userId) = TRIM(:userId) ORDER BY l.dateTime DESC")
    List<Bill_CycleLogFile> findByUserIdTrimmed(@Param("userId") String userId);
    
    // Find logs by user ID for a specific area with trimming
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE TRIM(l.userId) = TRIM(:userId) AND TRIM(l.areaCode) = TRIM(:areaCode) ORDER BY l.dateTime DESC")
    List<Bill_CycleLogFile> findByUserIdAndAreaCodeTrimmed(@Param("userId") String userId, @Param("areaCode") String areaCode);
    
    // Find latest log for an area and bill cycle with trimming
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE TRIM(l.areaCode) = TRIM(:areaCode) AND l.billCycle = :billCycle ORDER BY l.dateTime DESC")
    Optional<Bill_CycleLogFile> findLatestLogByAreaAndBillCycleTrimmed(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Find logs by area and date range with trimming
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE TRIM(l.areaCode) = TRIM(:areaCode) AND l.dateTime BETWEEN :startDate AND :endDate ORDER BY l.dateTime DESC")
    List<Bill_CycleLogFile> findByAreaCodeAndDateTimeBetweenTrimmed(@Param("areaCode") String areaCode, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Count pending logs for an area and bill cycle with trimming
    @Query("SELECT COUNT(l) FROM Bill_CycleLogFile l WHERE TRIM(l.areaCode) = TRIM(:areaCode) AND l.billCycle = :billCycle AND (l.endTime IS NULL OR TRIM(l.endTime) = '0:00:00')")
    Long countPendingLogsByAreaAndBillCycleTrimmed(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Count completed logs for an area and bill cycle with trimming
    @Query("SELECT COUNT(l) FROM Bill_CycleLogFile l WHERE TRIM(l.areaCode) = TRIM(:areaCode) AND l.billCycle = :billCycle AND l.endTime IS NOT NULL AND TRIM(l.endTime) != '0:00:00'")
    Long countCompletedLogsByAreaAndBillCycleTrimmed(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Find last completed process for an area and bill cycle with trimming
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE TRIM(l.areaCode) = TRIM(:areaCode) AND l.billCycle = :billCycle AND l.endTime IS NOT NULL AND TRIM(l.endTime) != '0:00:00' ORDER BY l.dateTime DESC")
    Optional<Bill_CycleLogFile> findLastCompletedProcessTrimmed(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Check if specific process code exists for area and bill cycle with trimming
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE TRIM(l.areaCode) = TRIM(:areaCode) AND l.billCycle = :billCycle AND TRIM(l.proCode) = TRIM(:proCode)")
    Optional<Bill_CycleLogFile> findByAreaCodeAndBillCycleAndProCodeTrimmed(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle, @Param("proCode") String proCode);
    
    // Find specific log by ID and verify user access with trimming
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.logId = :logId AND TRIM(l.userId) = TRIM(:userId)")
    Optional<Bill_CycleLogFile> findByLogIdAndUserIdTrimmed(@Param("logId") Long logId, @Param("userId") String userId);
    
    // Find specific log by ID and area with trimming
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.logId = :logId AND TRIM(l.areaCode) = TRIM(:areaCode)")
    Optional<Bill_CycleLogFile> findByLogIdAndAreaCodeTrimmed(@Param("logId") Long logId, @Param("areaCode") String areaCode);
    
    // Check if area has any logs for a bill cycle with trimming
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Bill_CycleLogFile l WHERE TRIM(l.areaCode) = TRIM(:areaCode) AND l.billCycle = :billCycle")
    boolean existsByAreaCodeAndBillCycleTrimmed(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // ============ EXISTING QUERIES (Keep for backward compatibility) ============
    
    // Find all logs for a specific area and bill cycle (original)
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.areaCode = :areaCode AND l.billCycle = :billCycle ORDER BY l.dateTime DESC")
    List<Bill_CycleLogFile> findByAreaCodeAndBillCycle(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Find all logs for a specific area (original)
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.areaCode = :areaCode ORDER BY l.billCycle DESC, l.dateTime DESC")
    List<Bill_CycleLogFile> findByAreaCode(@Param("areaCode") String areaCode);
    
    // Find pending logs (no end time) for an area and bill cycle (original)
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.areaCode = :areaCode AND l.billCycle = :billCycle AND (l.endTime IS NULL OR l.endTime = '0:00:00') ORDER BY l.dateTime DESC")
    List<Bill_CycleLogFile> findPendingLogsByAreaAndBillCycle(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Find completed logs (has end time) for an area and bill cycle (original)
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.areaCode = :areaCode AND l.billCycle = :billCycle AND l.endTime IS NOT NULL AND l.endTime != '0:00:00' ORDER BY l.dateTime DESC")
    List<Bill_CycleLogFile> findCompletedLogsByAreaAndBillCycle(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Check if process code 9.01 exists and is completed for an area and bill cycle (original)
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.areaCode = :areaCode AND l.billCycle = :billCycle AND l.proCode = '9.01' AND l.endTime IS NOT NULL AND l.endTime != '0:00:00'")
    Optional<Bill_CycleLogFile> findCompletedProcess901(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Check if process code 9.01 exists for an area and bill cycle (completed or pending) (original)
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.areaCode = :areaCode AND l.billCycle = :billCycle AND l.proCode = '9.01'")
    Optional<Bill_CycleLogFile> findProcess901(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Find logs by user ID (original)
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.userId = :userId ORDER BY l.dateTime DESC")
    List<Bill_CycleLogFile> findByUserId(@Param("userId") String userId);
    
    // Find logs by user ID for a specific area (original)
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.userId = :userId AND l.areaCode = :areaCode ORDER BY l.dateTime DESC")
    List<Bill_CycleLogFile> findByUserIdAndAreaCode(@Param("userId") String userId, @Param("areaCode") String areaCode);
    
    // Find latest log for an area and bill cycle (original)
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.areaCode = :areaCode AND l.billCycle = :billCycle ORDER BY l.dateTime DESC")
    Optional<Bill_CycleLogFile> findLatestLogByAreaAndBillCycle(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Find logs for multiple areas
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.areaCode IN :areaCodes ORDER BY l.areaCode, l.billCycle DESC, l.dateTime DESC")
    List<Bill_CycleLogFile> findByAreaCodeIn(@Param("areaCodes") List<String> areaCodes);
    
    // Find logs by date range
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.dateTime BETWEEN :startDate AND :endDate ORDER BY l.dateTime DESC")
    List<Bill_CycleLogFile> findByDateTimeBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find logs by area and date range (original)
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.areaCode = :areaCode AND l.dateTime BETWEEN :startDate AND :endDate ORDER BY l.dateTime DESC")
    List<Bill_CycleLogFile> findByAreaCodeAndDateTimeBetween(@Param("areaCode") String areaCode, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Count pending logs for an area and bill cycle (original)
    @Query("SELECT COUNT(l) FROM Bill_CycleLogFile l WHERE l.areaCode = :areaCode AND l.billCycle = :billCycle AND (l.endTime IS NULL OR l.endTime = '0:00:00')")
    Long countPendingLogsByAreaAndBillCycle(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Count completed logs for an area and bill cycle (original)
    @Query("SELECT COUNT(l) FROM Bill_CycleLogFile l WHERE l.areaCode = :areaCode AND l.billCycle = :billCycle AND l.endTime IS NOT NULL AND l.endTime != '0:00:00'")
    Long countCompletedLogsByAreaAndBillCycle(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Find last completed process for an area and bill cycle (original)
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.areaCode = :areaCode AND l.billCycle = :billCycle AND l.endTime IS NOT NULL AND l.endTime != '0:00:00' ORDER BY l.dateTime DESC")
    Optional<Bill_CycleLogFile> findLastCompletedProcess(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Check if specific process code exists for area and bill cycle (original)
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.areaCode = :areaCode AND l.billCycle = :billCycle AND l.proCode = :proCode")
    Optional<Bill_CycleLogFile> findByAreaCodeAndBillCycleAndProCode(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle, @Param("proCode") String proCode);
    
    // Get statistics for logs in a date/time range
    @Query("SELECT COUNT(l) FROM Bill_CycleLogFile l WHERE l.dateTime >= :start AND l.dateTime < :end")
    Long countByDateTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Get completed logs count in a date/time range
    @Query("SELECT COUNT(l) FROM Bill_CycleLogFile l WHERE l.dateTime >= :start AND l.dateTime < :end AND l.endTime IS NOT NULL AND l.endTime != '0:00:00'")
    Long countCompletedByDateTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Get pending logs count (all time)
    @Query("SELECT COUNT(l) FROM Bill_CycleLogFile l WHERE l.endTime IS NULL OR l.endTime = '0:00:00'")
    Long countAllPendingLogs();
    
    // Get areas with pending logs
    @Query("SELECT DISTINCT l.areaCode FROM Bill_CycleLogFile l WHERE l.endTime IS NULL OR l.endTime = '0:00:00'")
    List<String> findAreasWithPendingLogs();
    
    // Find areas that have completed 9.01 process (ready for bill cycle change)
    @Query("SELECT DISTINCT l.areaCode FROM Bill_CycleLogFile l WHERE l.proCode = '9.01' AND l.endTime IS NOT NULL AND l.endTime != '0:00:00'")
    List<String> findAreasReadyForBillCycleChange();
    
    // Find specific log by ID and verify user access (original)
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.logId = :logId AND l.userId = :userId")
    Optional<Bill_CycleLogFile> findByLogIdAndUserId(@Param("logId") Long logId, @Param("userId") String userId);
    
    // Find specific log by ID and area (for access control) (original)
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.logId = :logId AND l.areaCode = :areaCode")
    Optional<Bill_CycleLogFile> findByLogIdAndAreaCode(@Param("logId") Long logId, @Param("areaCode") String areaCode);
    
    // Get log summary by area codes
    @Query("SELECT l.areaCode, l.billCycle, COUNT(l), " +
           "SUM(CASE WHEN l.endTime IS NOT NULL AND l.endTime != '0:00:00' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN l.endTime IS NULL OR l.endTime = '0:00:00' THEN 1 ELSE 0 END) " +
           "FROM Bill_CycleLogFile l WHERE l.areaCode IN :areaCodes " +
           "GROUP BY l.areaCode, l.billCycle ORDER BY l.areaCode, l.billCycle DESC")
    List<Object[]> getLogSummaryByAreaCodes(@Param("areaCodes") List<String> areaCodes);
    
    // Find logs with specific process codes
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.areaCode = :areaCode AND l.billCycle = :billCycle AND l.proCode IN :proCodes ORDER BY l.dateTime DESC")
    List<Bill_CycleLogFile> findByAreaCodeAndBillCycleAndProCodeIn(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle, @Param("proCodes") List<String> proCodes);
    
    // Check if area has any logs for a bill cycle (original)
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Bill_CycleLogFile l WHERE l.areaCode = :areaCode AND l.billCycle = :billCycle")
    boolean existsByAreaCodeAndBillCycle(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Find logs by process code pattern (like 1.*, 2.*, etc.)
    @Query("SELECT l FROM Bill_CycleLogFile l WHERE l.areaCode = :areaCode AND l.billCycle = :billCycle AND l.proCode LIKE :proCodePattern ORDER BY l.dateTime DESC")
    List<Bill_CycleLogFile> findByProCodePattern(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle, @Param("proCodePattern") String proCodePattern);
}