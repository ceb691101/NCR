package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.TmpReadings;
import com.example.SPSProjectBackend.model.TmpReadingsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TmpReadingsRepository extends JpaRepository<TmpReadings, TmpReadingsId> {
    
    // ============ FIXED QUERIES WITH PROPER TRIMMING AND DATA TYPE HANDLING ============
    
    // Find readings by account number with active bill cycle filter - FIXED WITH PROPER DATA TYPE HANDLING
    @Query("SELECT t FROM TmpReadings t " +
           "WHERE t.accNbr = :accNbr " +
           "AND TRIM(t.addedBlcy) IN (SELECT CAST(bc.billCycle AS string) FROM NcreBillCycle bc " +
           "                   WHERE bc.isCurrent = 1) " +
           "ORDER BY t.rdngDate DESC, t.mtrType")
    List<TmpReadings> findByAccNbrWithActiveBillCycle(@Param("accNbr") String accNbr);
    
    // Find readings by account number and reading date with active bill cycle filter - FIXED
    @Query("SELECT t FROM TmpReadings t " +
           "WHERE t.accNbr = :accNbr AND t.rdngDate = :rdngDate " +
           "AND TRIM(t.addedBlcy) IN (SELECT CAST(bc.billCycle AS string) FROM NcreBillCycle bc " +
           "                   WHERE bc.isCurrent = 1) " +
           "ORDER BY t.mtrType")
    List<TmpReadings> findByAccNbrAndRdngDateWithActiveBillCycle(@Param("accNbr") String accNbr, @Param("rdngDate") Date rdngDate);
    
    // Find readings by area code with active bill cycle filter - FIXED WITH PROPER DATA TYPE HANDLING
    @Query("SELECT t FROM TmpReadings t " +
           "WHERE t.areaCd = TRIM(:areaCd) " +
           "AND TRIM(t.addedBlcy) IN (SELECT CAST(bc.billCycle AS string) FROM NcreBillCycle bc " +
           "                   WHERE bc.isCurrent = 1) " +
           "ORDER BY t.accNbr, t.rdngDate DESC")
    List<TmpReadings> findByAreaCdWithActiveBillCycle(@Param("areaCd") String areaCd);
    
    // Find readings by area code and specific bill cycle - FIXED WITH TRIMMING AND NATIVE QUERY
@Query(value = """
    SELECT * FROM tmp_rdngs t 
    WHERE t.area_cd = TRIM(:areaCd) 
    AND t.added_blcy = TRIM(:billCycle)
    ORDER BY t.acc_nbr, t.mtr_type
    """, nativeQuery = true)
List<TmpReadings> findByAreaCdAndBillCycle(@Param("areaCd") String areaCd, @Param("billCycle") String billCycle);

    
    // Find readings by meter number with active bill cycle filter - FIXED
    @Query("SELECT t FROM TmpReadings t " +
           "WHERE TRIM(t.mtrNbr) = TRIM(:mtrNbr) " +
           "AND TRIM(t.addedBlcy) IN (SELECT CAST(bc.billCycle AS string) FROM NcreBillCycle bc " +
           "                   WHERE bc.isCurrent = 1) " +
           "ORDER BY t.rdngDate DESC")
    List<TmpReadings> findByMtrNbrWithActiveBillCycle(@Param("mtrNbr") String mtrNbr);
    
    // Find readings by date range with active bill cycle filter - FIXED
    @Query("SELECT t FROM TmpReadings t " +
           "WHERE t.rdngDate BETWEEN :startDate AND :endDate " +
           "AND TRIM(t.addedBlcy) IN (SELECT CAST(bc.billCycle AS string) FROM NcreBillCycle bc " +
           "                   WHERE bc.isCurrent = 1) " +
           "ORDER BY t.accNbr, t.rdngDate")
    List<TmpReadings> findByDateRangeWithActiveBillCycle(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Find readings by account and date range with active bill cycle filter - FIXED
    @Query("SELECT t FROM TmpReadings t " +
           "WHERE t.accNbr = :accNbr AND t.rdngDate BETWEEN :startDate AND :endDate " +
           "AND TRIM(t.addedBlcy) IN (SELECT CAST(bc.billCycle AS string) FROM NcreBillCycle bc " +
           "                   WHERE bc.isCurrent = 1) " +
           "ORDER BY t.rdngDate DESC, t.mtrType")
    List<TmpReadings> findByAccNbrAndDateRangeWithActiveBillCycle(@Param("accNbr") String accNbr, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Find latest readings for account with active bill cycle filter - FIXED
    @Query("SELECT t FROM TmpReadings t " +
           "WHERE t.accNbr = :accNbr " +
           "AND TRIM(t.addedBlcy) IN (SELECT CAST(bc.billCycle AS string) FROM NcreBillCycle bc " +
           "                   WHERE bc.isCurrent = 1) " +
           "AND t.rdngDate = (SELECT MAX(t2.rdngDate) FROM TmpReadings t2 " +
           "                  WHERE t2.accNbr = :accNbr " +
           "                  AND TRIM(t2.addedBlcy) IN (SELECT CAST(bc2.billCycle AS string) FROM NcreBillCycle bc2 " +
           "                                       WHERE bc2.isCurrent = 1))")
    List<TmpReadings> findLatestReadingsByAccNbrWithActiveBillCycle(@Param("accNbr") String accNbr);
    
    // Find readings by user ID with active bill cycle filter - FIXED
    @Query("SELECT t FROM TmpReadings t " +
           "WHERE TRIM(t.userId) = TRIM(:userId) " +
           "AND TRIM(t.addedBlcy) IN (SELECT CAST(bc.billCycle AS string) FROM NcreBillCycle bc " +
           "                   WHERE bc.isCurrent = 1) " +
           "ORDER BY t.enteredDtime DESC")
    List<TmpReadings> findByUserIdWithActiveBillCycle(@Param("userId") String userId);
    
    // Find readings by bill status with active bill cycle filter - FIXED
    @Query("SELECT t FROM TmpReadings t " +
           "WHERE t.billStat = :billStat " +
           "AND TRIM(t.addedBlcy) IN (SELECT CAST(bc.billCycle AS string) FROM NcreBillCycle bc " +
           "                   WHERE bc.isCurrent = 1) " +
           "ORDER BY t.accNbr, t.rdngDate DESC")
    List<TmpReadings> findByBillStatWithActiveBillCycle(@Param("billStat") String billStat);
    
    // Find readings with errors with active bill cycle filter - FIXED
    @Query("SELECT t FROM TmpReadings t " +
           "WHERE t.errStat > 0 " +
           "AND TRIM(t.addedBlcy) IN (SELECT CAST(bc.billCycle AS string) FROM NcreBillCycle bc " +
           "                   WHERE bc.isCurrent = 1) " +
           "ORDER BY t.accNbr, t.rdngDate DESC")
    List<TmpReadings> findReadingsWithErrorsWithActiveBillCycle();
    
    // Get all readings with active bill cycle filter (main method for default behavior) - FIXED
    @Query("SELECT t FROM TmpReadings t " +
           "WHERE TRIM(t.addedBlcy) IN (SELECT CAST(bc.billCycle AS string) FROM NcreBillCycle bc " +
           "                     WHERE bc.isCurrent = 1) " +
           "ORDER BY t.areaCd, t.accNbr, t.rdngDate DESC")
    List<TmpReadings> findAllWithActiveBillCycle();
    
    // ============ EXISTING METHODS (KEEP FOR BACKWARD COMPATIBILITY) ============
    
    // Original methods kept for backward compatibility and historical data access
    @Query("SELECT t FROM TmpReadings t WHERE t.accNbr = :accNbr ORDER BY t.rdngDate DESC, t.mtrType")
    List<TmpReadings> findByAccNbr(@Param("accNbr") String accNbr);
    
    @Query("SELECT t FROM TmpReadings t WHERE t.accNbr = :accNbr AND t.rdngDate = :rdngDate ORDER BY t.mtrType")
    List<TmpReadings> findByAccNbrAndRdngDate(@Param("accNbr") String accNbr, @Param("rdngDate") Date rdngDate);
    
    @Query("SELECT t FROM TmpReadings t WHERE t.areaCd = :areaCd ORDER BY t.accNbr, t.rdngDate DESC")
    List<TmpReadings> findByAreaCd(@Param("areaCd") String areaCd);
    
    @Query("SELECT t FROM TmpReadings t WHERE t.mtrNbr = :mtrNbr ORDER BY t.rdngDate DESC")
    List<TmpReadings> findByMtrNbr(@Param("mtrNbr") String mtrNbr);
    
    @Query("SELECT t FROM TmpReadings t WHERE t.rdngDate BETWEEN :startDate AND :endDate ORDER BY t.accNbr, t.rdngDate")
    List<TmpReadings> findByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("SELECT t FROM TmpReadings t WHERE t.accNbr = :accNbr AND t.rdngDate BETWEEN :startDate AND :endDate ORDER BY t.rdngDate DESC, t.mtrType")
    List<TmpReadings> findByAccNbrAndDateRange(@Param("accNbr") String accNbr, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("SELECT t FROM TmpReadings t WHERE t.accNbr = :accNbr AND t.rdngDate = (SELECT MAX(t2.rdngDate) FROM TmpReadings t2 WHERE t2.accNbr = :accNbr)")
    List<TmpReadings> findLatestReadingsByAccNbr(@Param("accNbr") String accNbr);
    
    @Query("SELECT t FROM TmpReadings t WHERE t.userId = :userId ORDER BY t.enteredDtime DESC")
    List<TmpReadings> findByUserId(@Param("userId") String userId);
    
    @Query("SELECT t FROM TmpReadings t WHERE t.billStat = :billStat ORDER BY t.accNbr, t.rdngDate DESC")
    List<TmpReadings> findByBillStat(@Param("billStat") String billStat);
    
    @Query("SELECT t FROM TmpReadings t WHERE t.errStat > 0 ORDER BY t.accNbr, t.rdngDate DESC")
    List<TmpReadings> findReadingsWithErrors();
    
    // Check if reading exists
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TmpReadings t WHERE t.accNbr = :accNbr AND t.areaCd = :areaCd AND t.addedBlcy = :addedBlcy AND t.mtrSeq = :mtrSeq AND t.mtrType = :mtrType AND t.rdngDate = :rdngDate")
    boolean existsReading(@Param("accNbr") String accNbr, @Param("areaCd") String areaCd, 
                         @Param("addedBlcy") String addedBlcy, @Param("mtrSeq") Integer mtrSeq,
                         @Param("mtrType") String mtrType, @Param("rdngDate") Date rdngDate);
    
    // Find specific reading
    @Query("SELECT t FROM TmpReadings t WHERE t.accNbr = :accNbr AND t.areaCd = :areaCd AND t.addedBlcy = :addedBlcy AND t.mtrSeq = :mtrSeq AND t.mtrType = :mtrType AND t.rdngDate = :rdngDate")
    Optional<TmpReadings> findSpecificReading(@Param("accNbr") String accNbr, @Param("areaCd") String areaCd, 
                                             @Param("addedBlcy") String addedBlcy, @Param("mtrSeq") Integer mtrSeq,
                                             @Param("mtrType") String mtrType, @Param("rdngDate") Date rdngDate);
    
    // Get distinct account numbers
    @Query("SELECT DISTINCT t.accNbr FROM TmpReadings t ORDER BY t.accNbr")
    List<String> findDistinctAccNbrs();
    
    // Get distinct meter types
    @Query("SELECT DISTINCT t.mtrType FROM TmpReadings t ORDER BY t.mtrType")
    List<String> findDistinctMtrTypes();
    
    // ============ NEW METHODS FOR METER READING INFO FUNCTIONALITY ============
    
    /**
     * Find readings by account number, area code, and bill cycle
     * This is CRITICAL for the MeterReadingInfoService
     */
    @Query("SELECT t FROM TmpReadings t WHERE t.accNbr = :accNbr AND TRIM(t.areaCd) = TRIM(:areaCd) AND TRIM(t.addedBlcy) = TRIM(:billCycle) ORDER BY t.mtrType")
    List<TmpReadings> findByAccNbrAndAreaCdAndAddedBlcy(@Param("accNbr") String accNbr, 
                                                       @Param("areaCd") String areaCd, 
                                                       @Param("billCycle") String billCycle);
    
    /**
     * Find readings with errors without active bill cycle filter
     */
    @Query("SELECT t FROM TmpReadings t WHERE t.errStat IS NOT NULL AND t.errStat > 0 ORDER BY t.accNbr, t.rdngDate DESC")
    List<TmpReadings> findReadingsWithErrorsWithoutFilter();
    
    /**
     * Find latest readings for account with active bill cycle filter (alternative implementation)
     */
    @Query("SELECT t FROM TmpReadings t WHERE t.accNbr = :accNbr " +
           "AND TRIM(t.addedBlcy) IN (SELECT CAST(bc.billCycle AS string) FROM NcreBillCycle bc " +
           "                   WHERE bc.isCurrent = 1) " +
           "AND t.rdngDate = (SELECT MAX(t2.rdngDate) FROM TmpReadings t2 WHERE t2.accNbr = :accNbr " +
           "                  AND TRIM(t2.addedBlcy) IN (SELECT CAST(bc2.billCycle AS string) FROM NcreBillCycle bc2 " +
           "                                       WHERE bc2.isCurrent = 1)) " +
           "ORDER BY t.mtrType")
    List<TmpReadings> findLatestReadingsByAccNbrWithActiveBillCycleOrdered(@Param("accNbr") String accNbr);
    
    /**
     * Find readings by account number and area code (without bill cycle filter)
     */
    @Query("SELECT t FROM TmpReadings t WHERE t.accNbr = :accNbr AND TRIM(t.areaCd) = TRIM(:areaCd) ORDER BY t.rdngDate DESC, t.mtrType")
    List<TmpReadings> findByAccNbrAndAreaCd(@Param("accNbr") String accNbr, @Param("areaCd") String areaCd);
    
    /**
     * Find readings by account number, area code, and date range
     */
    @Query("SELECT t FROM TmpReadings t WHERE t.accNbr = :accNbr AND TRIM(t.areaCd) = TRIM(:areaCd) " +
           "AND t.rdngDate BETWEEN :startDate AND :endDate ORDER BY t.rdngDate DESC, t.mtrType")
    List<TmpReadings> findByAccNbrAndAreaCdAndDateRange(@Param("accNbr") String accNbr, @Param("areaCd") String areaCd,
                                                       @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * Count readings by account number and bill cycle
     */
    @Query("SELECT COUNT(t) FROM TmpReadings t WHERE t.accNbr = :accNbr AND TRIM(t.areaCd) = TRIM(:areaCd) AND TRIM(t.addedBlcy) = TRIM(:billCycle)")
    Long countByAccNbrAndAreaCdAndBillCycle(@Param("accNbr") String accNbr, @Param("areaCd") String areaCd, 
                                           @Param("billCycle") String billCycle);
    
    /**
     * Check if customer has readings for specific bill cycle
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TmpReadings t " +
           "WHERE t.accNbr = :accNbr AND TRIM(t.areaCd) = TRIM(:areaCd) AND TRIM(t.addedBlcy) = TRIM(:billCycle)")
    boolean hasReadingsForBillCycle(@Param("accNbr") String accNbr, @Param("areaCd") String areaCd, 
                                   @Param("billCycle") String billCycle);
    
    /**
     * Get distinct bill cycles for an account in an area
     */
    @Query("SELECT DISTINCT t.addedBlcy FROM TmpReadings t WHERE t.accNbr = :accNbr AND TRIM(t.areaCd) = TRIM(:areaCd) ORDER BY t.addedBlcy DESC")
    List<String> findDistinctBillCyclesByAccNbrAndAreaCd(@Param("accNbr") String accNbr, @Param("areaCd") String areaCd);
    
    /**
     * Get readings by multiple account numbers and bill cycle (for bulk operations)
     */
    @Query("SELECT t FROM TmpReadings t WHERE t.accNbr IN :accNbrs AND TRIM(t.areaCd) = TRIM(:areaCd) AND TRIM(t.addedBlcy) = TRIM(:billCycle) ORDER BY t.accNbr, t.mtrType")
    List<TmpReadings> findByAccNbrsAndAreaCdAndBillCycle(@Param("accNbrs") List<String> accNbrs, 
                                                        @Param("areaCd") String areaCd, 
                                                        @Param("billCycle") String billCycle);
    
    /**
     * Get the latest reading date for an account in an area for a specific bill cycle
     */
    @Query("SELECT MAX(t.rdngDate) FROM TmpReadings t WHERE t.accNbr = :accNbr AND TRIM(t.areaCd) = TRIM(:areaCd) AND TRIM(t.addedBlcy) = TRIM(:billCycle)")
    Optional<Date> findLatestReadingDateByAccNbrAndAreaCdAndBillCycle(@Param("accNbr") String accNbr, 
                                                                     @Param("areaCd") String areaCd, 
                                                                     @Param("billCycle") String billCycle);

    /**
     * Execute native update query for reading date update
     */
    @Modifying
    @Query(value = "UPDATE tmp_rdngs SET rdng_date = ?1, edited_dtime = ?2, edited_user_id = ?3 " +
                  "WHERE acc_nbr = ?4 AND area_cd = ?5 AND added_blcy = ?6 AND mtr_seq = ?7 AND rdng_date = ?8", 
          nativeQuery = true)
    int executeNativeUpdate(Date newRdngDate, Date editedDtime, String editedUserId,
                         String accNbr, String areaCd, String addedBlcy, 
                         Integer mtrSeq, Date oldRdngDate);
    
    /**
     * Debug query to check data directly - NEW ADDED
     */
    @Query(value = "SELECT COUNT(*) FROM tmp_rdngs t " +
                   "WHERE TRIM(t.area_cd) = TRIM(:areaCd) " +
                   "AND TRIM(t.added_blcy) = TRIM(:billCycle)", 
           nativeQuery = true)
    Long countByAreaCdAndBillCycleNative(@Param("areaCd") String areaCd, @Param("billCycle") String billCycle);

    /**
 * Find readings by account number, area code, bill cycle, and meter type
 */
@Query("SELECT t FROM TmpReadings t WHERE t.accNbr = :accNbr AND TRIM(t.areaCd) = TRIM(:areaCd) AND TRIM(t.addedBlcy) = TRIM(:billCycle) AND TRIM(t.mtrType) = TRIM(:mtrType)")
List<TmpReadings> findByAccNbrAndAreaCdAndAddedBlcyAndMtrType(
    @Param("accNbr") String accNbr, 
    @Param("areaCd") String areaCd, 
    @Param("billCycle") String billCycle, 
    @Param("mtrType") String mtrType);

/**
 * Find all readings by account number, area code, and bill cycle for all meter types
 */
@Query("SELECT t FROM TmpReadings t WHERE t.accNbr = :accNbr AND TRIM(t.areaCd) = TRIM(:areaCd) AND TRIM(t.addedBlcy) = TRIM(:billCycle)")
List<TmpReadings> findAllByAccNbrAndAreaCdAndAddedBlcy(
    @Param("accNbr") String accNbr, 
    @Param("areaCd") String areaCd, 
    @Param("billCycle") String billCycle);
}