package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.MtrDetail;
import com.example.SPSProjectBackend.model.MtrDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MtrDetailRepository extends JpaRepository<MtrDetail, MtrDetailId> {
    
    // Find meter details by installation ID (latest records based on effect date)
    @Query("SELECT m FROM MtrDetail m WHERE m.instId = :instId AND m.effctDate = " +
           "(SELECT MAX(m2.effctDate) FROM MtrDetail m2 WHERE m2.instId = :instId AND m2.mtrType = m.mtrType) " +
           "ORDER BY m.mtrType")
    List<MtrDetail> findByInstId(@Param("instId") String instId);
    
    // Find meter details by installation ID - Fixed version that handles NULLs properly
    @Query(value = "SELECT m.* FROM mtr_detail m " +
           "WHERE m.inst_id = TRIM(:instId) " +
           "AND m.effct_date = (SELECT MAX(m2.effct_date) FROM mtr_detail m2 " +
           "WHERE m2.inst_id = TRIM(:instId) AND m2.mtr_type = m.mtr_type AND m2.effct_date IS NOT NULL) " +
           "ORDER BY m.mtr_type", nativeQuery = true)
    List<MtrDetail> findByInstIdFixed(@Param("instId") String instId);
    
    // Find distinct meter types by installation ID
    @Query("SELECT DISTINCT m.mtrType FROM MtrDetail m WHERE m.instId = :instId ORDER BY m.mtrType")
    List<String> findDistinctMeterTypesByInstId(@Param("instId") String instId);
    
    // Find meter details by installation ID and meter type (latest record)
    @Query("SELECT m FROM MtrDetail m WHERE m.instId = :instId AND m.mtrType = :mtrType AND m.effctDate = " +
           "(SELECT MAX(m2.effctDate) FROM MtrDetail m2 WHERE m2.instId = :instId AND m2.mtrType = :mtrType)")
    Optional<MtrDetail> findByInstIdAndMtrType(@Param("instId") String instId, @Param("mtrType") String mtrType);
    
    // Find meter details by installation ID and meter type - Fixed version that handles NULLs
    @Query(value = "SELECT m.* FROM mtr_detail m " +
           "WHERE m.inst_id = TRIM(:instId) AND TRIM(m.mtr_type) = TRIM(:mtrType) " +
           "AND m.effct_date = (SELECT MAX(m2.effct_date) FROM mtr_detail m2 " +
           "WHERE m2.inst_id = TRIM(:instId) AND TRIM(m2.mtr_type) = TRIM(:mtrType) AND m2.effct_date IS NOT NULL) " +
           "LIMIT 1", nativeQuery = true)
    Optional<MtrDetail> findByInstIdAndMtrTypeFixed(@Param("instId") String instId, @Param("mtrType") String mtrType);
    
    // Find all meter details by installation ID (without considering effect date)
    @Query("SELECT m FROM MtrDetail m WHERE m.instId = :instId ORDER BY m.mtrType")
    List<MtrDetail> findAllByInstId(@Param("instId") String instId);
    
    // Check if meter exists for installation ID
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MtrDetail m WHERE m.instId = :instId")
    boolean existsByInstId(@Param("instId") String instId);
    
    // Find meter details by multiple installation IDs
    @Query("SELECT m FROM MtrDetail m WHERE m.instId IN :instIds AND m.effctDate = " +
           "(SELECT MAX(m2.effctDate) FROM MtrDetail m2 WHERE m2.instId = m.instId AND m2.mtrType = m.mtrType) " +
           "ORDER BY m.instId, m.mtrType")
    List<MtrDetail> findByInstIds(@Param("instIds") List<String> instIds);
}
