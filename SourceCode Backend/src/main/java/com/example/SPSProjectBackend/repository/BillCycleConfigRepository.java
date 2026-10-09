package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.BillCycleConfig;
import com.example.SPSProjectBackend.model.BillCycleConfigId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillCycleConfigRepository extends JpaRepository<BillCycleConfig, BillCycleConfigId> {
    
    // ============ NEW TRIMMED QUERIES ============
    
    // Find all bill cycles for a specific area with trimming
    @Query("SELECT bc FROM BillCycleConfig bc WHERE TRIM(bc.areaCode) = TRIM(:areaCode) ORDER BY bc.billCycle DESC")
    List<BillCycleConfig> findByAreaCodeTrimmed(@Param("areaCode") String areaCode);
    
    // Find active bill cycles for a specific area with trimming
    @Query("SELECT bc FROM BillCycleConfig bc WHERE TRIM(bc.areaCode) = TRIM(:areaCode) AND bc.cycleStat = 1 ORDER BY bc.billCycle DESC")
    List<BillCycleConfig> findActiveByAreaCodeTrimmed(@Param("areaCode") String areaCode);
    
    // Find the maximum active bill cycle for a specific area with trimming
    @Query("SELECT bc FROM BillCycleConfig bc WHERE TRIM(bc.areaCode) = TRIM(:areaCode) AND bc.cycleStat = 1 ORDER BY bc.billCycle DESC")
    Optional<BillCycleConfig> findMaxActiveBillCycleByAreaCodeTrimmed(@Param("areaCode") String areaCode);
    
    // Get active bill cycle number for an area with trimming
    @Query("SELECT MAX(bc.billCycle) FROM BillCycleConfig bc WHERE TRIM(bc.areaCode) = TRIM(:areaCode)")
    Optional<Integer> findMaxActiveBillCycleNumberByAreaCodeTrimmed(@Param("areaCode") String areaCode);

    // Get max active bill cycles grouped by area for all areas in a single bulk query
    @Query("SELECT bc.areaCode, MAX(bc.billCycle) FROM BillCycleConfig bc GROUP BY bc.areaCode")
    List<Object[]> findAllMaxActiveBillCyclesGrouped();
    
    // Find all bill cycles for multiple areas with trimming
    @Query("SELECT bc FROM BillCycleConfig bc WHERE bc.areaCode IN :areaCodes ORDER BY bc.areaCode, bc.billCycle DESC")
    List<BillCycleConfig> findByAreaCodeIn(@Param("areaCodes") List<String> areaCodes);
    
    // Find active bill cycles for multiple areas with trimming
    @Query("SELECT bc FROM BillCycleConfig bc WHERE bc.areaCode IN :areaCodes AND bc.cycleStat = 1 ORDER BY bc.areaCode, bc.billCycle DESC")
    List<BillCycleConfig> findActiveByAreaCodeIn(@Param("areaCodes") List<String> areaCodes);
    
    // Get max active bill cycles for multiple areas with trimming
    @Query("SELECT bc.areaCode, MAX(bc.billCycle) FROM BillCycleConfig bc " +
           "WHERE bc.areaCode IN :areaCodes " +
           "GROUP BY bc.areaCode")
    List<Object[]> findMaxActiveBillCyclesByAreaCodes(@Param("areaCodes") List<String> areaCodes);
    
    // Check if area has any bill cycles with trimming
    @Query("SELECT CASE WHEN COUNT(bc) > 0 THEN true ELSE false END FROM BillCycleConfig bc WHERE TRIM(bc.areaCode) = TRIM(:areaCode)")
    boolean existsByAreaCodeTrimmed(@Param("areaCode") String areaCode);
    
    // Check if area has active bill cycles with trimming
    @Query("SELECT CASE WHEN COUNT(bc) > 0 THEN true ELSE false END FROM BillCycleConfig bc WHERE TRIM(bc.areaCode) = TRIM(:areaCode) AND bc.cycleStat = 1")
    boolean hasActiveBillCycleTrimmed(@Param("areaCode") String areaCode);
    
    // Find bill cycles by user ID with trimming
    @Query("SELECT bc FROM BillCycleConfig bc WHERE TRIM(bc.userId) = TRIM(:userId) ORDER BY bc.areaCode, bc.billCycle DESC")
    List<BillCycleConfig> findByUserIdTrimmed(@Param("userId") String userId);
    
    // Find specific bill cycle for an area with trimming
    @Query("SELECT bc FROM BillCycleConfig bc WHERE TRIM(bc.areaCode) = TRIM(:areaCode) AND bc.billCycle = :billCycle")
    Optional<BillCycleConfig> findByAreaCodeAndBillCycleTrimmed(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // ============ EXISTING QUERIES (Keep for backward compatibility) ============
    
    // Find all bill cycles for a specific area (original)
    @Query("SELECT bc FROM BillCycleConfig bc WHERE bc.areaCode = :areaCode ORDER BY bc.billCycle DESC")
    List<BillCycleConfig> findByAreaCode(@Param("areaCode") String areaCode);
    
    // Find active bill cycles for a specific area (original)
    @Query("SELECT bc FROM BillCycleConfig bc WHERE bc.areaCode = :areaCode AND bc.cycleStat = 1 ORDER BY bc.billCycle DESC")
    List<BillCycleConfig> findActiveByAreaCode(@Param("areaCode") String areaCode);
    
    // Find the maximum active bill cycle for a specific area (original)
    @Query("SELECT bc FROM BillCycleConfig bc WHERE bc.areaCode = :areaCode AND bc.cycleStat = 1 ORDER BY bc.billCycle DESC")
    Optional<BillCycleConfig> findMaxActiveBillCycleByAreaCode(@Param("areaCode") String areaCode);
    
    // Get active bill cycle number for an area (original)
    @Query("SELECT MAX(bc.billCycle) FROM BillCycleConfig bc WHERE bc.areaCode = :areaCode AND bc.cycleStat = 1")
    Optional<Integer> findMaxActiveBillCycleNumberByAreaCode(@Param("areaCode") String areaCode);
    
    // Find all areas that have bill cycles
    @Query("SELECT DISTINCT bc.areaCode FROM BillCycleConfig bc ORDER BY bc.areaCode")
    List<String> findAllAreaCodesWithBillCycles();
    
    // Find areas with active bill cycles
    @Query("SELECT DISTINCT bc.areaCode FROM BillCycleConfig bc WHERE bc.cycleStat = 1 ORDER BY bc.areaCode")
    List<String> findAreaCodesWithActiveBillCycles();
    
    // Check if area has any bill cycles (original)
    @Query("SELECT CASE WHEN COUNT(bc) > 0 THEN true ELSE false END FROM BillCycleConfig bc WHERE bc.areaCode = :areaCode")
    boolean existsByAreaCode(@Param("areaCode") String areaCode);
    
    // Check if area has active bill cycles (original)
    @Query("SELECT CASE WHEN COUNT(bc) > 0 THEN true ELSE false END FROM BillCycleConfig bc WHERE bc.areaCode = :areaCode AND bc.cycleStat = 1")
    boolean hasActiveBillCycle(@Param("areaCode") String areaCode);
    
    // Find bill cycles by user ID (original)
    @Query("SELECT bc FROM BillCycleConfig bc WHERE bc.userId = :userId ORDER BY bc.areaCode, bc.billCycle DESC")
    List<BillCycleConfig> findByUserId(@Param("userId") String userId);
    
    // Count bill cycles by status for an area
    @Query("SELECT bc.cycleStat, COUNT(bc) FROM BillCycleConfig bc WHERE bc.areaCode = :areaCode GROUP BY bc.cycleStat")
    List<Object[]> countBillCyclesByStatusForArea(@Param("areaCode") String areaCode);
    
    // Get bill cycle summary for areas
    @Query("SELECT bc.areaCode, COUNT(bc), " +
           "SUM(CASE WHEN bc.cycleStat = 1 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN bc.cycleStat = 2 THEN 1 ELSE 0 END) " +
           "FROM BillCycleConfig bc " +
           "WHERE bc.areaCode IN :areaCodes " +
           "GROUP BY bc.areaCode")
    List<Object[]> getBillCycleSummaryForAreas(@Param("areaCodes") List<String> areaCodes);
    
    // Find specific bill cycle for an area (original)
    @Query("SELECT bc FROM BillCycleConfig bc WHERE bc.areaCode = :areaCode AND bc.billCycle = :billCycle")
    Optional<BillCycleConfig> findByAreaCodeAndBillCycle(@Param("areaCode") String areaCode, @Param("billCycle") Integer billCycle);
    
    // Get all bill cycles with area information
    @Query("SELECT bc.areaCode, bc.billCycle, bc.cycleStat, bc.userId, bc.enteredDate, " +
           "a.areaName, a.provCode, a.region " +
           "FROM BillCycleConfig bc " +
           "JOIN HsbArea a ON bc.areaCode = a.areaCode " +
           "WHERE bc.areaCode IN :areaCodes " +
           "ORDER BY bc.areaCode, bc.billCycle DESC")
    List<Object[]> findBillCyclesWithAreaInfo(@Param("areaCodes") List<String> areaCodes);
    
    // Get active bill cycles with full area details
    @Query("SELECT bc.areaCode, MAX(bc.billCycle), a.areaName, a.provCode, a.region, p.provName " +
           "FROM BillCycleConfig bc " +
           "JOIN HsbArea a ON bc.areaCode = a.areaCode " +
           "JOIN HsbProvince p ON a.provCode = p.provCode " +
           "WHERE bc.areaCode IN :areaCodes " +
           "GROUP BY bc.areaCode, a.areaName, a.provCode, a.region, p.provName " +
           "ORDER BY bc.areaCode")
    List<Object[]> findActiveBillCyclesWithFullAreaDetails(@Param("areaCodes") List<String> areaCodes);
}