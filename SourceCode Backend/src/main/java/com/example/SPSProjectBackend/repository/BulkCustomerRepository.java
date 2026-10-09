package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.dto.MapDataDTO;
import com.example.SPSProjectBackend.model.BulkCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BulkCustomerRepository extends JpaRepository<BulkCustomer, String> {

       // ============ NEW TRIMMED QUERIES ============

       // Find by account number with trimming
       @Query("SELECT c FROM BulkCustomer c WHERE TRIM(c.accNbr) = TRIM(:accNbr)")
       Optional<BulkCustomer> findByAccNbrTrimmed(@Param("accNbr") String accNbr);

       // Find customers by area code with trimming
       @Query("SELECT c FROM BulkCustomer c WHERE TRIM(c.areaCd) = TRIM(:areaCd) AND TRIM(c.ncre) = '1' AND c.ncre_type IS NOT NULL ORDER BY c.accNbr")
       List<BulkCustomer> findByAreaCdTrimmed(@Param("areaCd") String areaCd);

       // Check if customer exists by account number with trimming
       @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM BulkCustomer c WHERE TRIM(c.accNbr) = TRIM(:accNbr)")
       boolean existsByAccNbrTrimmed(@Param("accNbr") String accNbr);

       // Find customers by mobile number with trimming
       @Query("SELECT c FROM BulkCustomer c WHERE TRIM(c.mobileNo) = TRIM(:mobileNo)")
       Optional<BulkCustomer> findByMobileNoTrimmed(@Param("mobileNo") String mobileNo);

       // Find customers by name with trimming (partial search)
       @Query("SELECT c FROM BulkCustomer c WHERE UPPER(TRIM(c.name)) LIKE UPPER(CONCAT('%', TRIM(:name), '%')) ORDER BY c.name")
       List<BulkCustomer> findByNameContainingTrimmed(@Param("name") String name);

       // Find customers by tariff with trimming
       @Query("SELECT c FROM BulkCustomer c WHERE TRIM(c.tariff) = TRIM(:tariff) ORDER BY c.accNbr")
       List<BulkCustomer> findByTariffTrimmed(@Param("tariff") String tariff);

       // Find customers by customer category with trimming
       @Query("SELECT c FROM BulkCustomer c WHERE TRIM(c.cusCat) = TRIM(:cusCat) ORDER BY c.accNbr")
       List<BulkCustomer> findByCusCatTrimmed(@Param("cusCat") String cusCat);

       // Find customers by operational status with trimming
       @Query("SELECT c FROM BulkCustomer c WHERE TRIM(c.opStat) = TRIM(:opStat) ORDER BY c.accNbr")
       List<BulkCustomer> findByOpStatTrimmed(@Param("opStat") String opStat);

       // Find customers by telephone number with trimming
       @Query("SELECT c FROM BulkCustomer c WHERE TRIM(c.telNbr) = TRIM(:telNbr)")
       List<BulkCustomer> findByTelNbrTrimmed(@Param("telNbr") String telNbr);

       // Find customers by city with trimming
       @Query("SELECT c FROM BulkCustomer c WHERE UPPER(TRIM(c.city)) = UPPER(TRIM(:city)) ORDER BY c.name")
       List<BulkCustomer> findByCityTrimmed(@Param("city") String city);

       // Find customers by job number with trimming
       @Query("SELECT c FROM BulkCustomer c WHERE TRIM(c.jobNbr) = TRIM(:jobNbr)")
       List<BulkCustomer> findByJobNbrTrimmed(@Param("jobNbr") String jobNbr);

       // Search customers by multiple criteria with trimming
       @Query("SELECT c FROM BulkCustomer c WHERE " +
                     "(:areaCd IS NULL OR TRIM(c.areaCd) = TRIM(:areaCd)) AND " +
                     "(:zone IS NULL OR TRIM(c.zone) = TRIM(:zone)) AND " +
                     "(:tariff IS NULL OR TRIM(c.tariff) = TRIM(:tariff)) AND " +
                     "(:opStat IS NULL OR TRIM(c.opStat) = TRIM(:opStat)) AND " +
                     "(:cusCat IS NULL OR TRIM(c.cusCat) = TRIM(:cusCat)) " +
                     "ORDER BY c.accNbr")
       List<BulkCustomer> findByMultipleCriteriaTrimmed(@Param("areaCd") String areaCd,
                     @Param("zone") String zone,
                     @Param("tariff") String tariff,
                     @Param("opStat") String opStat,
                     @Param("cusCat") String cusCat);

       // ============ EXISTING QUERIES (Keep for backward compatibility) ============

       // Find customer by account number (original)
       @Query("SELECT c FROM BulkCustomer c WHERE c.accNbr = :accNbr")
       Optional<BulkCustomer> findByAccNbr(@Param("accNbr") String accNbr);

       // Check if customer exists by account number (original)
       @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM BulkCustomer c WHERE c.accNbr = :accNbr")
       boolean existsByAccNbr(@Param("accNbr") String accNbr);

       // Find customers by area code (original)
       @Query("SELECT c FROM BulkCustomer c WHERE c.areaCd = :areaCd ORDER BY c.accNbr")
       List<BulkCustomer> findByAreaCd(@Param("areaCd") String areaCd);

       // Find customers by zone
       @Query("SELECT c FROM BulkCustomer c WHERE c.zone = :zone ORDER BY c.accNbr")
       List<BulkCustomer> findByZone(@Param("zone") String zone);

       // Find customers by zone and area
       @Query("SELECT c FROM BulkCustomer c WHERE c.zone = :zone AND c.areaCd = :areaCd ORDER BY c.accNbr")
       List<BulkCustomer> findByZoneAndAreaCd(@Param("zone") String zone, @Param("areaCd") String areaCd);

       // Find customers by name (partial search) (original)
       @Query("SELECT c FROM BulkCustomer c WHERE UPPER(c.name) LIKE UPPER(:name) ORDER BY c.name")
       List<BulkCustomer> findByNameContaining(@Param("name") String name);

       // Find customers by tariff (original)
       @Query("SELECT c FROM BulkCustomer c WHERE c.tariff = :tariff ORDER BY c.accNbr")
       List<BulkCustomer> findByTariff(@Param("tariff") String tariff);

       // Find customers by customer category (original)
       @Query("SELECT c FROM BulkCustomer c WHERE c.cusCat = :cusCat ORDER BY c.accNbr")
       List<BulkCustomer> findByCusCat(@Param("cusCat") String cusCat);

       // Find customers by operational status (original)
       @Query("SELECT c FROM BulkCustomer c WHERE c.opStat = :opStat ORDER BY c.accNbr")
       List<BulkCustomer> findByOpStat(@Param("opStat") String opStat);

       // Find customers by mobile number (original)
       @Query("SELECT c FROM BulkCustomer c WHERE c.mobileNo = :mobileNo")
       Optional<BulkCustomer> findByMobileNo(@Param("mobileNo") String mobileNo);

       // Find customers by telephone number (original)
       @Query("SELECT c FROM BulkCustomer c WHERE c.telNbr = :telNbr")
       List<BulkCustomer> findByTelNbr(@Param("telNbr") String telNbr);

       // Find customers by city (original)
       @Query("SELECT c FROM BulkCustomer c WHERE UPPER(c.city) = UPPER(:city) ORDER BY c.name")
       List<BulkCustomer> findByCity(@Param("city") String city);

       // Find customers by job number (original)
       @Query("SELECT c FROM BulkCustomer c WHERE c.jobNbr = :jobNbr")
       List<BulkCustomer> findByJobNbr(@Param("jobNbr") String jobNbr);

       // Find customers by connection date range
       @Query("SELECT c FROM BulkCustomer c WHERE c.cnectDate BETWEEN :startDate AND :endDate ORDER BY c.cnectDate DESC")
       List<BulkCustomer> findByCnectDateBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

       // Find customers by entered date range
       @Query("SELECT c FROM BulkCustomer c WHERE DATE(c.enteredDtime) BETWEEN :startDate AND :endDate ORDER BY c.enteredDtime DESC")
       List<BulkCustomer> findByEnteredDateBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

       // Find customers by user ID
       @Query("SELECT c FROM BulkCustomer c WHERE c.userId = :userId ORDER BY c.enteredDtime DESC")
       List<BulkCustomer> findByUserId(@Param("userId") String userId);

       // Find customers by edited user ID
       @Query("SELECT c FROM BulkCustomer c WHERE c.editedUserId = :editedUserId ORDER BY c.editedDtime DESC")
       List<BulkCustomer> findByEditedUserId(@Param("editedUserId") String editedUserId);

       // Find customers by bill cycle
       @Query("SELECT c FROM BulkCustomer c WHERE c.billCycle = :billCycle ORDER BY c.accNbr")
       List<BulkCustomer> findByBillCycle(@Param("billCycle") Integer billCycle);

       // Find customers with deposits greater than amount
       @Query("SELECT c FROM BulkCustomer c WHERE c.depositAmt > :amount ORDER BY c.depositAmt DESC")
       List<BulkCustomer> findByDepositAmtGreaterThan(@Param("amount") java.math.BigDecimal amount);

       // Find customers by customer type
       @Query("SELECT c FROM BulkCustomer c WHERE c.custType = :custType ORDER BY c.accNbr")
       List<BulkCustomer> findByCustType(@Param("custType") String custType);

       // Search customers by multiple criteria (original)
       @Query("SELECT c FROM BulkCustomer c WHERE " +
                     "(:areaCd IS NULL OR c.areaCd = :areaCd) AND " +
                     "(:zone IS NULL OR c.zone = :zone) AND " +
                     "(:tariff IS NULL OR c.tariff = :tariff) AND " +
                     "(:opStat IS NULL OR c.opStat = :opStat) AND " +
                     "(:cusCat IS NULL OR c.cusCat = :cusCat) " +
                     "ORDER BY c.accNbr")
       List<BulkCustomer> findByMultipleCriteria(@Param("areaCd") String areaCd,
                     @Param("zone") String zone,
                     @Param("tariff") String tariff,
                     @Param("opStat") String opStat,
                     @Param("cusCat") String cusCat);

       // Get distinct area codes
       @Query("SELECT DISTINCT c.areaCd FROM BulkCustomer c WHERE c.areaCd IS NOT NULL ORDER BY c.areaCd")
       List<String> findDistinctAreaCodes();

       // Get distinct zones
       @Query("SELECT DISTINCT c.zone FROM BulkCustomer c WHERE c.zone IS NOT NULL ORDER BY c.zone")
       List<String> findDistinctZones();

       // Get distinct tariffs
       @Query("SELECT DISTINCT c.tariff FROM BulkCustomer c ORDER BY c.tariff")
       List<String> findDistinctTariffs();

       // Get distinct customer categories
       @Query("SELECT DISTINCT c.cusCat FROM BulkCustomer c WHERE c.cusCat IS NOT NULL ORDER BY c.cusCat")
       List<String> findDistinctCusCategories();

       // Get distinct cities
       @Query("SELECT DISTINCT c.city FROM BulkCustomer c WHERE c.city IS NOT NULL ORDER BY c.city")
       List<String> findDistinctCities();

       // Count customers by area
       @Query("SELECT COUNT(c) FROM BulkCustomer c WHERE c.areaCd = :areaCd")
       Long countByAreaCd(@Param("areaCd") String areaCd);

       // Count customers by zone
       @Query("SELECT COUNT(c) FROM BulkCustomer c WHERE c.zone = :zone")
       Long countByZone(@Param("zone") String zone);

       // Count customers by operational status
       @Query("SELECT COUNT(c) FROM BulkCustomer c WHERE c.opStat = :opStat")
       Long countByOpStat(@Param("opStat") String opStat);

       // Advanced search with pagination support
       @Query("SELECT c FROM BulkCustomer c WHERE " +
                     "(:searchTerm IS NULL OR " +
                     "UPPER(c.name) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
                     "UPPER(c.accNbr) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
                     "UPPER(c.mobileNo) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR " +
                     "UPPER(c.city) LIKE UPPER(CONCAT('%', :searchTerm, '%'))) " +
                     "ORDER BY c.accNbr")
       List<BulkCustomer> searchCustomers(@Param("searchTerm") String searchTerm);

       // method to the existing repository
       @Query("SELECT c FROM BulkCustomer c WHERE c.accNbr IN :accNbrs AND c.areaCd = :areaCd ORDER BY c.accNbr")
       List<BulkCustomer> findByAccNbrInAndAreaCd(@Param("accNbrs") List<String> accNbrs,
                     @Param("areaCd") String areaCd);

       // Find all customers where TRIM(ncre) = '1' and ncre_type is not null (with
       // trimming)
       @Query("SELECT c FROM BulkCustomer c WHERE TRIM(c.ncre) = '1' AND c.ncre_type IS NOT NULL")
       List<BulkCustomer> findAllNcreCustomersTrimmed();

       // Update only latitude and longitude
       @Modifying
       @Transactional
       @Query("UPDATE BulkCustomer c SET c.latitude = :latitude, c.longitude = :longitude, c.editedDtime = :editedDtime WHERE TRIM(c.accNbr) = TRIM(:accNbr)")
       void updateLocationOnly(@Param("accNbr") String accNbr, @Param("latitude") Double latitude,
                     @Param("longitude") Double longitude, @Param("editedDtime") java.util.Date editedDtime);

       @Query("SELECT " +
              "COALESCE(nd.accNbr, c.accNbr, CONCAT('PEND_', nd.folioNo)) AS accNbr, " +
              "nd.folioNo AS folioNo, " + 
              "COALESCE(c.areaCd, nd.area) AS areaCd, " +
              "c.city AS city, " +
              "COALESCE(nd.developerName, c.name) AS name, " +
              "COALESCE(nd.latitude, c.latitude) AS latitude, " +
              "COALESCE(nd.longitude, c.longitude) AS longitude, " +
              "COALESCE(nd.type, c.ncre_type) AS ncreCategory, " +
              "nd.facilityName AS facilityName, " +
              "nd.developerName AS developerName, " +
              "COALESCE(nd.province, c.city, nd.area) AS area, " +
              "nd.addressLine1 AS addressLine1, " +
              "nd.addressLine2 AS addressLine2, " +
              "nd.addressLine3 AS addressLine3, " +
              "nd.responsibleEe AS responsibleEe " +
              "FROM NcreDeveloper nd " +
              "LEFT JOIN BulkCustomer c ON (nd.accNbr IS NOT NULL AND TRIM(c.accNbr) = TRIM(nd.accNbr)) OR (c.folioNo IS NOT NULL AND nd.folioNo IS NOT NULL AND c.folioNo = nd.folioNo) " +
              "WHERE (COALESCE(nd.latitude, c.latitude) IS NOT NULL AND COALESCE(nd.latitude, c.latitude) != 0 AND COALESCE(nd.longitude, c.longitude) IS NOT NULL AND COALESCE(nd.longitude, c.longitude) != 0)")
       List<MapDataDTO> findMapData();

}
