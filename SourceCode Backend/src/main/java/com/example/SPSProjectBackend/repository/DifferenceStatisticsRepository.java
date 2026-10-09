package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.model.NcreInvRdngs;
import com.example.SPSProjectBackend.model.NcreInvRdngsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DifferenceStatisticsRepository extends JpaRepository<NcreInvRdngs, NcreInvRdngsId> {

       /**
        * Retrieve kWh difference for all accounts grouped by account, folio number,
        * and facility name for an area and bill cycle.
        * Difference formula: SUM(COALESCE(kwh_tot, 0)) - (SUM(COALESCE(kwh_r1, 0)) +
        * SUM(COALESCE(kwh_r2, 0)) + SUM(COALESCE(kwh_r3, 0)))
        * Joined with NcreDeveloper by trimmed accNbr.
        */
       @Query("SELECT TRIM(r.accNbr), d.folioNo, d.facilityName, " +
                     "(SUM(COALESCE(r.kwhTot, 0)) - (SUM(COALESCE(r.kwhR1, 0)) + SUM(COALESCE(r.kwhR2, 0)) + SUM(COALESCE(r.kwhR3, 0)))) "
                     +
                     "FROM NcreInvRdngs r " +
                     "LEFT JOIN NcreDeveloper d ON TRIM(r.accNbr) = TRIM(d.accNbr) " +
                     "WHERE TRIM(r.areaCd) = TRIM(:areaCode) " +
                     "AND TRIM(r.addedBlcy) = TRIM(:billCycle) " +
                     "GROUP BY r.accNbr, d.folioNo, d.facilityName " +
                     "ORDER BY d.folioNo, d.facilityName")
       List<Object[]> findDifferencesByAreaAndBillCycle(@Param("areaCode") String areaCode,
                     @Param("billCycle") String billCycle);
}
