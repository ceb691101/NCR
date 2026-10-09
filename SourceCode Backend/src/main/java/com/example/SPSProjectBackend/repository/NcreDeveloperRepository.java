package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.NcreDeveloper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

@Repository
public interface NcreDeveloperRepository extends JpaRepository<NcreDeveloper, String> {

    // ============ NEW TRIMMED QUERIES ============

    // Find by account number with trimming
    @Query("SELECT nd FROM NcreDeveloper nd WHERE TRIM(nd.accNbr) = TRIM(:accNbr)")
    Optional<NcreDeveloper> findByAccNbrTrimmed(@Param("accNbr") String accNbr);

    Optional<NcreDeveloper> findByAccNbr(String accNbr);
    Optional<NcreDeveloper> findByFacilityName(String facilityName);
    Optional<NcreDeveloper> findByFileNo(Short fileNo);
    
    @Query("SELECT nd FROM NcreDeveloper nd WHERE nd.folioNo = :folioNo")
    Optional<NcreDeveloper> findByFolioNo(@Param("folioNo") Short folioNo);

    @Query("SELECT nd FROM NcreDeveloper nd WHERE nd.folioNo IN :folioNos")
    List<NcreDeveloper> findByFolioNoIn(@Param("folioNos") List<Short> folioNos);

       List<NcreDeveloper> findAllByOrderByFolioNoAsc();
    
    @Query("SELECT nd FROM NcreDeveloper nd WHERE TRIM(nd.area) = TRIM(:areaCode)")
    List<NcreDeveloper> findByAreaCodeTrimmed(@Param("areaCode") String areaCode);

       @Query("SELECT COUNT(nd) FROM NcreDeveloper nd WHERE TRIM(nd.area) = TRIM(:areaCode)")
       Long countByAreaCodeTrimmed(@Param("areaCode") String areaCode);
    
    @Query("SELECT nd FROM NcreDeveloper nd WHERE TRIM(nd.accNbr) = TRIM(:accNbr) AND nd.folioNo = :folioNo")
    Optional<NcreDeveloper> findByAccNbrAndFolioNo(@Param("accNbr") String accNbr, @Param("folioNo") Short folioNo);
    /**
     * Database-side query to find all active NCRE developers (status = '2') with non-null folio numbers
     */
    @Query("SELECT d FROM NcreDeveloper d WHERE TRIM(d.status) = '2' AND d.folioNo IS NOT NULL")
    List<NcreDeveloper> findActiveDevelopers();

    /**
     * Count distinct active folios for a specific REP role (status = '2')
     */
    @Query("SELECT COUNT(DISTINCT d.folioNo) FROM NcreDeveloper d " +
           "WHERE UPPER(TRIM(d.responsibleEe)) = UPPER(TRIM(:role)) " +
           "  AND TRIM(d.status) = '2' " +
           "  AND d.folioNo IS NOT NULL")
    Long countTotalActiveFoliosByRole(@Param("role") String role);

    /**
     * Count distinct active folios across all active developers (status = '2')
     */
    @Query("SELECT COUNT(DISTINCT d.folioNo) FROM NcreDeveloper d " +
           "WHERE TRIM(d.status) = '2' " +
           "  AND d.folioNo IS NOT NULL")
    Long countTotalActiveFolios();

    /**
     * Find available checklist developers for a bill cycle and REP role (active developers not yet added/checked for that bill cycle)
     */
    @Query("SELECT d FROM NcreDeveloper d " +
           "WHERE UPPER(TRIM(d.responsibleEe)) = UPPER(TRIM(:role)) " +
           "  AND TRIM(d.status) = '2' " +
           "  AND d.folioNo IS NOT NULL " +
           "  AND NOT EXISTS (" +
           "      SELECT c FROM NcreInvoiceCreate c " +
           "      WHERE c.folioNo = d.folioNo " +
           "        AND c.billCycle = :billCycle " +
           "        AND c.isCreate = 1" +
           "  ) " +
           "ORDER BY d.folioNo ASC")
    List<NcreDeveloper> findAvailableChecklistDevelopersByBillCycleAndRole(@Param("billCycle") Integer billCycle, @Param("role") String role);

    /**
     * Database-side JOIN between ncre_inv_rdngs and ncre_developers on acc_nbr for a given bill cycle (added_blcy)
     */
    @Query("SELECT DISTINCT d FROM NcreDeveloper d, NcreInvRdngs r " +
           "WHERE TRIM(d.accNbr) = TRIM(r.accNbr) " +
           "  AND TRIM(r.addedBlcy) = TRIM(:billCycle) " +
           "  AND d.folioNo IS NOT NULL")
    List<NcreDeveloper> findEligibleChecklistDevelopersByBillCycle(@Param("billCycle") String billCycle);

    @Query(value = "SELECT area, responsble_ee, COUNT(*) " +
                   "FROM dbadmin.ncre_developers WHERE status = '2' " +
                   "GROUP BY area, responsble_ee", nativeQuery = true)
    List<Object[]> countActiveDevelopersGroupedByAreaAndResponsibleEe();

    @Query(value = "SELECT acc_nbr, folio_no, area, type, developer_name, facility_name, " +
                   "tariff_type, responsble_ee, address_line1, telephone " +
                   "FROM dbadmin.ncre_developers WHERE status = '2' " +
                   "AND TRIM(area) = TRIM(:areaCode) ORDER BY acc_nbr, folio_no", nativeQuery = true)
    List<Object[]> findActiveDeveloperReadingRowsByArea(@Param("areaCode") String areaCode);

    @Query(value = "SELECT acc_nbr, folio_no, area, type, developer_name, facility_name, " +
                   "tariff_type, responsble_ee, address_line1, telephone " +
                   "FROM dbadmin.ncre_developers WHERE status = '2' ORDER BY area, acc_nbr, folio_no",
           nativeQuery = true)
    List<Object[]> findAllActiveDeveloperReadingRows();

    @Query(value = "SELECT TRIM(d.acc_nbr) AS acc_nbr, " +
                   "d.folio_no AS folio_no, " +
                   "COALESCE(TRIM(t.tariff_desc), TRIM(d.tariff_type)) AS tariff_desc " +
                   "FROM dbadmin.ncre_developers d " +
                   "LEFT JOIN dbadmin.ncre_tariff_desc t " +
                   "ON (TRIM(d.tariff_type) = TRIM(t.tariff_code) OR TRIM(d.tariff_type) = TRIM(t.tariff_desc))",
           nativeQuery = true)
    List<Object[]> findAllDeveloperTariffDescriptions();
}
