package com.example.SPSProjectBackend.repository;

import com.example.SPSProjectBackend.dto.JournalSummaryDTO;
import com.example.SPSProjectBackend.model.Journal;
import com.example.SPSProjectBackend.model.JournalId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal; 
import java.util.List;

@Repository
public interface JournalRepository extends JpaRepository<Journal, JournalId> {

    // =================== SUMMARY DTO QUERY - SELECTED AREA AND BILL CYCLE ===================
    @Query("SELECT new com.example.SPSProjectBackend.dto.JournalSummaryDTO(" +
           "j.id.accNbr, " +
           "j.id.jnlType, " +
           "j.id.jnlNo, " +
           "j.adjustAmt) " +
           "FROM Journal j " +
           "WHERE j.id.areaCd = :areaCode " +
           "AND j.id.addedBlcy = :billCycle " +
           "ORDER BY j.id.jnlNo")
    List<JournalSummaryDTO> findFilteredJournalsByAreaAndBillCycle(
            @Param("areaCode") String areaCode,
            @Param("billCycle") Integer billCycle);

    // =================== DETAIL QUERY - SELECTED AREA AND BILL CYCLE ===================
    @Query("SELECT j " +
           "FROM Journal j " +
           "WHERE j.id.accNbr = :accNbr " +
           "AND j.id.jnlType = :jnlType " +
           "AND j.id.jnlNo = :jnlNo " +
           "AND j.adjustAmt = :adjustAmt " +
           "AND j.id.areaCd = :areaCode " +
           "AND j.id.addedBlcy = :billCycle")
    List<Journal> findJournalDetailsByCompositeKeyAndArea(
            @Param("accNbr") String accNbr,
            @Param("jnlType") String jnlType,
            @Param("jnlNo") Integer jnlNo,
            @Param("adjustAmt") BigDecimal adjustAmt,
            @Param("areaCode") String areaCode,
            @Param("billCycle") Integer billCycle);

    // =================== REPORT QUERY - USER'S ACCESSIBLE AREAS ===================
    @Query("SELECT j " +
           "FROM Journal j " +
           "WHERE j.id.areaCd IN :areaCodes " +
           "AND j.id.addedBlcy IN :billCycles " +
           "ORDER BY j.id.areaCd, j.id.addedBlcy, j.id.jnlNo")
    List<Journal> findJournalsForUserReport(
            @Param("areaCodes") List<String> areaCodes,
            @Param("billCycles") List<Integer> billCycles);
}