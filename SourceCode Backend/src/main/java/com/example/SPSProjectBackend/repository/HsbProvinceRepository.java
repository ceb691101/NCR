package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.HsbProvince;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HsbProvinceRepository extends JpaRepository<HsbProvince, String> {
    
    // ============ NEW TRIMMED QUERIES ============
    
    // Find province by code with trimming
    @Query("SELECT p FROM HsbProvince p WHERE TRIM(p.provCode) = TRIM(:provCode)")
    Optional<HsbProvince> findByProvCodeTrimmed(@Param("provCode") String provCode);
    
    // Find province by name with trimming
    @Query("SELECT p FROM HsbProvince p WHERE TRIM(p.provName) = TRIM(:provName)")
    Optional<HsbProvince> findByProvNameTrimmed(@Param("provName") String provName);
    
    // Check if province code exists with trimming
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM HsbProvince p WHERE TRIM(p.provCode) = TRIM(:provCode)")
    boolean existsByProvCodeTrimmed(@Param("provCode") String provCode);
    
    // ============ EXISTING QUERIES (Keep for backward compatibility) ============
    
    // Find province by code (original)
    @Query("SELECT p FROM HsbProvince p WHERE p.provCode = :provCode")
    Optional<HsbProvince> findByProvCode(@Param("provCode") String provCode);
    
    // Find province by name (original)
    @Query("SELECT p FROM HsbProvince p WHERE p.provName = :provName")
    Optional<HsbProvince> findByProvName(@Param("provName") String provName);
    
    // Find all provinces ordered by province code
    @Query("SELECT p FROM HsbProvince p ORDER BY p.provCode ASC")
    List<HsbProvince> findAllOrderByProvCode();
    
    // Get provinces by region code (derived from areas table)
    @Query("SELECT DISTINCT p FROM HsbProvince p " +
           "JOIN HsbArea a ON p.provCode = a.provCode " +
           "WHERE a.region = :regionCode " +
           "ORDER BY p.provCode ASC")
    List<HsbProvince> findByRegionCode(@Param("regionCode") String regionCode);
    
    // Check if province code exists (original)
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM HsbProvince p WHERE p.provCode = :provCode")
    boolean existsByProvCode(@Param("provCode") String provCode);
    
    // Get province with region information
    @Query("SELECT p.provCode, p.provName, a.region " +
           "FROM HsbProvince p " +
           "LEFT JOIN HsbArea a ON p.provCode = a.provCode " +
           "WHERE p.provCode = :provCode " +
           "GROUP BY p.provCode, p.provName, a.region")
    List<Object[]> findProvinceWithRegion(@Param("provCode") String provCode);
}