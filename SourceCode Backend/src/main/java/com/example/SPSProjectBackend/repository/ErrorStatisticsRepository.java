package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.TmpReadings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorStatisticsRepository extends JpaRepository<TmpReadings, String> {

    /**
     * Get distinct account numbers with errors for an area and bill cycle
     * Counts accounts, not individual error instances
     */
    @Query("SELECT DISTINCT t.accNbr FROM TmpReadings t " +
           "WHERE t.areaCd = TRIM(:areaCode) " +
           "AND t.addedBlcy = TRIM(:billCycle) " +
           "AND t.errStat IS NOT NULL AND t.errStat > 0 " +
           "ORDER BY t.accNbr")
    List<String> findDistinctAccountsWithErrors(@Param("areaCode") String areaCode,
                                               @Param("billCycle") String billCycle);

    /**
     * Get error statistics grouped by error code for an area and bill cycle
     * Returns count of distinct accounts for each error code
     */
    @Query("SELECT t.errStat, COUNT(DISTINCT t.accNbr) " +
           "FROM TmpReadings t " +
           "WHERE t.areaCd = TRIM(:areaCode) " +
           "AND t.addedBlcy = TRIM(:billCycle) " +
           "AND t.errStat IS NOT NULL AND t.errStat > 0 " +
           "GROUP BY t.errStat " +
           "ORDER BY t.errStat")
    List<Object[]> findErrorStatisticsByAreaAndBillCycle(@Param("areaCode") String areaCode,
                                                        @Param("billCycle") String billCycle);

    /**
     * Get all error instances for a specific error code in an area and bill cycle
     * Used for detailed error view
     */
    @Query("SELECT t FROM TmpReadings t " +
           "WHERE t.areaCd = TRIM(:areaCode) " +
           "AND t.addedBlcy = TRIM(:billCycle) " +
           "AND t.errStat = :errorCode " +
           "ORDER BY t.accNbr, t.mtrType")
    List<TmpReadings> findErrorInstancesByErrorCode(@Param("areaCode") String areaCode,
                                                   @Param("billCycle") String billCycle,
                                                   @Param("errorCode") Integer errorCode);

    @Query("SELECT t FROM TmpReadings t " +
           "WHERE t.areaCd IN :areaCodes " +
           "AND t.addedBlcy = TRIM(:billCycle) " +
           "AND EXISTS (SELECT e FROM TmpReadings e " +
           "WHERE e.areaCd = t.areaCd AND e.accNbr = t.accNbr " +
           "AND e.addedBlcy = t.addedBlcy AND e.errStat = :errorCode) " +
           "ORDER BY t.areaCd, t.accNbr, t.mtrType")
    List<TmpReadings> findReadingsForErrorAccountsByAreas(@Param("areaCodes") List<String> areaCodes,
                                                         @Param("billCycle") String billCycle,
                                                         @Param("errorCode") Integer errorCode);

    /**
     * Get accounts with specific error code including their error instances
     */
    @Query("SELECT t FROM TmpReadings t " +
           "WHERE t.areaCd = TRIM(:areaCode) " +
           "AND t.addedBlcy = TRIM(:billCycle) " +
           "AND t.errStat = :errorCode " +
           "ORDER BY t.accNbr, t.mtrType")
    List<TmpReadings> findAccountsWithErrorCode(@Param("areaCode") String areaCode,
                                               @Param("billCycle") String billCycle,
                                               @Param("errorCode") Integer errorCode);

    /**
     * Get total count of error instances (all meter readings with errors)
     */
    @Query("SELECT COUNT(t) FROM TmpReadings t " +
           "WHERE t.areaCd = TRIM(:areaCode) " +
           "AND t.addedBlcy = TRIM(:billCycle) " +
           "AND t.errStat IS NOT NULL AND t.errStat > 0")
    Long countTotalErrorInstances(@Param("areaCode") String areaCode,
                                 @Param("billCycle") String billCycle);

    /**
     * Get count of accounts without any readings for an area and bill cycle
     * REWRITTEN: Use LEFT JOIN for efficiency; added TRIM for consistency
     */
    @Query("SELECT COUNT(bc) FROM BulkCustomer bc " +
           "LEFT JOIN TmpReadings t ON bc.accNbr = t.accNbr " +
           "AND bc.areaCd = t.areaCd " +
           "AND t.addedBlcy = TRIM(:billCycle) " +
           "WHERE bc.areaCd = TRIM(:areaCode) " +
           "AND t.accNbr IS NULL")
    Long countUnreadAccounts(@Param("areaCode") String areaCode,
                            @Param("billCycle") String billCycle);

    /**
     * Get total number of accounts in an area
     */
    @Query("SELECT COUNT(bc) FROM BulkCustomer bc WHERE bc.areaCd = TRIM(:areaCode)")
    Long countTotalAccountsInArea(@Param("areaCode") String areaCode);
}